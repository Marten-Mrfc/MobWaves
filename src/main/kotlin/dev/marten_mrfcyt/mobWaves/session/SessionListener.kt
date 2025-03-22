package dev.marten_mrfcyt.mobWaves.session

import dev.marten_mrfcyt.mobWaves.MobWaves
import dev.marten_mrfcyt.mobWaves.waves.handler.WaveManager
import dev.marten_mrfcyt.mobWaves.zones.ZoneHandler
import dev.marten_mrfcyt.mobWaves.zones.ZoneUtil
import dev.marten_mrfcyt.mobWaves.zones.xp.XPZoneManager
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

class SessionListener(private val plugin: MobWaves) : Listener {
    init {
        plugin.server.scheduler.runTaskTimerAsynchronously(plugin, Runnable { SessionManager.cleanup() }, 1200L, 1200L)
        plugin.server.scheduler.runTaskTimerAsynchronously(plugin, Runnable { ZoneHandler.updateActionBars() }, 0L, 20L)
    }

    private fun cleanupSession(player: Player) {
        SessionManager.getSession(player)?.apply {
            currentWave?.name?.let {
                WaveManager.removeActiveWave(player)
                ZoneHandler.notifyLeave(player, it)
            }
            if (isInXPZone) {
                ZoneHandler.notifyXPZoneLeave(player)
                XPZoneManager.removePlayer(player)
            }
            SessionManager.removeSession(player)
        }
    }

    private fun isPlayerExempt(player: Player): Boolean =
        player.gameMode == GameMode.CREATIVE || player.gameMode == GameMode.SPECTATOR || player.isFlying

    @EventHandler fun onPlayerQuit(event: PlayerQuitEvent) = cleanupSession(event.player)
    @EventHandler fun onPlayerDeath(event: PlayerDeathEvent) = cleanupSession(event.player)

    @EventHandler(priority = EventPriority.HIGH)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        cleanupSession(player)

        if (!ZoneHandler.isLocationValid(player.location)) return
        SessionManager.createSession(player)

        ZoneUtil.getWaveName(player.location)?.let { waveName ->
            ZoneHandler.notifyJoin(player, waveName)
            WaveManager.getWaveByString(waveName)?.let {
                WaveManager.addActiveWave(player, it)
            }
        }

        handleXPZone(player, player.location, true)
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onPlayerTeleport(event: PlayerTeleportEvent) {
        if (event.isCancelled) return

        if (!ZoneHandler.isLocationValid(event.to)) {
            cleanupSession(event.player)
            return
        }

        plugin.server.scheduler.runTask(plugin) { _ ->
            if (event.player.isOnline) handleLocationChange(event.player, event.from, event.to)
        }
    }

    @EventHandler
    fun onPlayerRespawn(event: PlayerRespawnEvent) {
        if (!ZoneHandler.isLocationValid(event.respawnLocation)) {
            cleanupSession(event.player)
            return
        }
        handleLocationChange(event.player, event.player.location, event.respawnLocation)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerMove(event: PlayerMoveEvent) {
        if (event.from.x == event.to.x && event.from.y == event.to.y && event.from.z == event.to.z) return
        handleLocationChange(event.player, event.from, event.to)
    }

    private fun handleLocationChange(player: Player, from: Location, to: Location) {
        // Create session if needed
        if (SessionManager.getSession(player) == null && ZoneHandler.isLocationValid(to)) {
            if (isPlayerExempt(player)) return
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
            ZoneHandler.notifyLeave(player, it)
            WaveManager.removeActiveWave(player)
        }

        if (isPlayerExempt(player)) return

        newRegion?.let {
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

        SessionManager.updateXPZoneStatus(player, true)
        ZoneHandler.notifyXPZoneJoin(player)

        val regionName = ZoneUtil.getRegionNameOfXpZone(location)
        if (regionName == null) {
            println("Region name is null")
            return
        }

        XPZoneManager.addPlayer(player, regionName)
    }
}