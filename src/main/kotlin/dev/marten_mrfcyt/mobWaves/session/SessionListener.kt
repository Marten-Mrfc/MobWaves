package dev.marten_mrfcyt.mobWaves.session

import dev.marten_mrfcyt.mobWaves.MobWaves
import dev.marten_mrfcyt.mobWaves.waves.handler.WaveManager
import dev.marten_mrfcyt.mobWaves.zones.ZoneHandler
import dev.marten_mrfcyt.mobWaves.zones.ZoneUtil
import dev.marten_mrfcyt.mobWaves.zones.xp.XPZoneManager
import mlib.api.utilities.message
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.event.server.PluginDisableEvent

class SessionListener(private val plugin: MobWaves) : Listener {
    init {
        plugin.server.scheduler.runTaskTimerAsynchronously(plugin, Runnable { SessionManager.cleanup() }, 1200L, 1200L)
        plugin.server.scheduler.runTaskTimerAsynchronously(plugin, Runnable { ZoneHandler.updateActionBars() }, 0L, 20L)
    }

    private fun pauseSession(player: Player) {
        // Instead of clearing the session on death/quit, we now save it
        val session = SessionManager.getSession(player) ?: return

        if (session.isInXPZone) {
            ZoneHandler.notifyXPZoneLeave(player)
            WaveManager.removeActiveWave(player)
            XPZoneManager.removePlayer(player)
        }

        // No longer removing the session, just saving state
        PersistentSessionManager.saveSession(session)
    }

    private fun isPlayerExempt(player: Player): Boolean =
        player.gameMode == GameMode.CREATIVE || player.gameMode == GameMode.SPECTATOR || player.isFlying || player.isInvulnerable || player.isDead || player.isInvisible

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        pauseSession(event.player)
        SessionManager.removeSession(event.player)
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        // Only pause the session, don't remove it
        pauseSession(event.player)
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player

        if (!ZoneHandler.isLocationValid(player.location)) return

        // Always create/load the session
        val session = SessionManager.createSession(player)
        if (isPlayerExempt(player)) {
            player.message("Je kan niet deelnemen aan waves of XP verdienen in admin-modus.")
            return
        }
        // Only handle wave joining if no active wave AND it's allowed play time
        if (session.currentWave == null && PersistentSessionManager.isPlayTimeAllowed()) {
            ZoneUtil.getWaveName(player.location)?.let { waveName ->
                if (waveName == "NotSet") return
                ZoneHandler.notifyJoin(player, waveName)
                WaveManager.getWaveByString(waveName)?.let {
                    WaveManager.addActiveWave(player, it)
                }
            }
        } else if (session.currentWave != null && !PersistentSessionManager.isPlayTimeAllowed()) {
            // Player has an active wave but it's outside allowed hours
            player.message("Helaas, je kan alleen tussen 18:00 en 00:00 waves spelen.")
            WaveManager.removeActiveWave(player)
        }

        // Only handle XP zone joining if it's allowed play time
        if (PersistentSessionManager.isPlayTimeAllowed()) {
            handleXPZone(player, player.location, true)
        } else if (XPZoneManager.isXPZone(player.location)) {
            player.message("Helaas, je kan alleen tussen 18:00 en 00:00 XP verdienen.")
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onPlayerTeleport(event: PlayerTeleportEvent) {
        if (event.isCancelled) return

        if (!ZoneHandler.isLocationValid(event.to)) {
            // Only pause, don't remove
            pauseSession(event.player)
            return
        }

        plugin.server.scheduler.runTask(plugin) { _ ->
            if (event.player.isOnline) handleLocationChange(event.player, event.from, event.to)
        }
    }

    @EventHandler
    fun onPlayerRespawn(event: PlayerRespawnEvent) {
        if (!ZoneHandler.isLocationValid(event.respawnLocation)) {
            pauseSession(event.player)
            return
        }
        handleLocationChange(event.player, event.player.location, event.respawnLocation)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerMove(event: PlayerMoveEvent) {
        if (event.from.x == event.to.x && event.from.y == event.to.y && event.from.z == event.to.z) return
        handleLocationChange(event.player, event.from, event.to)
    }

    @EventHandler
    fun onPluginDisable(event: PluginDisableEvent) {
        if (event.plugin.name == plugin.name) {
            // Save all sessions when plugin is disabled
            SessionManager.saveAllSessions()
        }
    }

    private fun handleLocationChange(player: Player, from: Location, to: Location) {
        // Create session if needed
        if (SessionManager.getSession(player) == null && ZoneHandler.isLocationValid(to)) {
            if (isPlayerExempt(player)) {
                player.message("Je kan niet deelnemen aan waves of XP verdienen in admin-modus.")
                return
            }
            SessionManager.createSession(player)
        }

        // Handle wave region changes
        handleWaveRegionChange(player, from, to)

        // Handle XP zone changes
        handleXPZoneChange(player, from, to)
    }

    private fun handleWaveRegionChange(player: Player, from: Location, to: Location) {
        val oldRegion = ZoneUtil.getWaveName(from)
        val newRegion = ZoneUtil.getWaveName(to)
        if (oldRegion == newRegion) return

        oldRegion?.let {
            WaveManager.removeActiveWave(player)
            if (it != "NotSet") ZoneHandler.notifyLeave(player, it)
        }
        newRegion?.let {
            if (it == "NotSet") return
            // Only proceed if it's within allowed play time
            if (!PersistentSessionManager.isPlayTimeAllowed()) {
                player.message("Helaas, je kan alleen tussen 18:00 en 00:00 waves spelen.")
                return
            }
            if (isPlayerExempt(player)) {
                player.message("Je kan niet deelnemen aan waves of XP verdienen in admin-modus.")
                return
            }
            ZoneHandler.notifyJoin(player, it)
            plugin.server.scheduler.runTask(plugin) { _ ->
                if (player.isOnline) {
                    WaveManager.getWaveByString(it)?.let { wave ->
                        WaveManager.addActiveWave(player, wave)
                    }
                }
            }
        }
    }

    private fun handleXPZoneChange(player: Player, from: Location, to: Location) {
        val isOldXPZone = XPZoneManager.isXPZone(from)
        val isNewXPZone = XPZoneManager.isXPZone(to)

        if (isOldXPZone == isNewXPZone) return

        if (isNewXPZone) {
            // Check if it's within allowed play time
            if (!PersistentSessionManager.isPlayTimeAllowed()) {
                player.message("Helaas, je kan alleen tussen 18:00 en 00:00 XP verdienen.")
                return
            }
            handleXPZone(player, to, false)
        } else {
            SessionManager.updateXPZoneStatus(player, false)
            ZoneHandler.notifyXPZoneLeave(player)
            XPZoneManager.removePlayer(player)
        }
    }

    private fun handleXPZone(player: Player, location: Location, isJoining: Boolean) {
        if (!XPZoneManager.isXPZone(location)) return
        if (isJoining && isPlayerExempt(player)) return

        // Check if it's within allowed play time
        if (!PersistentSessionManager.isPlayTimeAllowed()) {
            player.message("Helaas, je kan alleen tussen 18:00 en 00:00 XP verdienen.")
            return
        }

        if (!SessionManager.updateXPZoneStatus(player, true)) {
            return // Failed to update XP zone status due to time restrictions
        }

        ZoneHandler.notifyXPZoneJoin(player)

        val regionName = ZoneUtil.getRegionNameOfXpZone(location)
        if (regionName == null) {
            plugin.logger.warning("Region name is null for XP zone at ${location.blockX}, ${location.blockY}, ${location.blockZ}")
            return
        }

        XPZoneManager.addPlayer(player, regionName)
    }
}