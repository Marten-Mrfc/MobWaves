package dev.marten_mrfcyt.mobWaves.session

import dev.marten_mrfcyt.mobWaves.MobWaves
import dev.marten_mrfcyt.mobWaves.waves.handler.WaveManager
import dev.marten_mrfcyt.mobWaves.zones.ZoneHandler
import dev.marten_mrfcyt.mobWaves.zones.ZoneUtil
import dev.marten_mrfcyt.mobWaves.zones.xp.XPZoneManager
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
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
            currentWave?.name?.let { WaveManager.removeActiveWave(player); ZoneHandler.notifyLeave(player, it) }
            if (isInXPZone) {
                ZoneHandler.notifyXPZoneLeave(player)
                XPZoneManager.removePlayer(player)
            }
            SessionManager.removeSession(player)
        }
    }

    @EventHandler fun onPlayerQuit(event: PlayerQuitEvent) = cleanupSession(event.player)
    @EventHandler fun onPlayerDeath(event: PlayerDeathEvent) = cleanupSession(event.player)

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        cleanupSession(player)

        if (ZoneHandler.isLocationValid(player.location)) {
            SessionManager.createSession(player)

            ZoneUtil.getWaveName(player.location)?.let { waveName ->
                ZoneHandler.notifyJoin(player, waveName)
                WaveManager.getWaveByString(waveName)?.let { WaveManager.addActiveWave(player, it) }
            }

            if (XPZoneManager.isXPZone(player.location)) {
                println("Player ${player.name} is in XP Zone")
                SessionManager.updateXPZoneStatus(player, true)
                ZoneHandler.notifyXPZoneJoin(player)
                val regionName = ZoneUtil.getRegionNameOfXpZone(player.location) ?: return println("Region name is null")
                XPZoneManager.addPlayer(player, regionName)
            }
        }
    }

    @EventHandler
    fun onPlayerTeleport(event: PlayerTeleportEvent) {
        if (event.isCancelled) return
        if (!ZoneHandler.isLocationValid(event.to)) return cleanupSession(event.player)

        plugin.server.scheduler.runTask(plugin) { _ ->
            if (event.player.isOnline) handleLocationChange(event.player, event.from, event.to)
        }
    }

    @EventHandler
    fun onPlayerRespawn(event: PlayerRespawnEvent) {
        if (!ZoneHandler.isLocationValid(event.respawnLocation)) return cleanupSession(event.player)
        handleLocationChange(event.player, event.player.location, event.respawnLocation)
    }

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        if (event.from.x == event.to.x && event.from.y == event.to.y && event.from.z == event.to.z) return
        handleLocationChange(event.player, event.from, event.to)
    }

    private fun handleLocationChange(player: Player, from: Location, to: Location) {
        val newRegion = ZoneUtil.getWaveName(to)
        val oldRegion = ZoneUtil.getWaveName(from)
        val isNewXPZone = XPZoneManager.isXPZone(to)
        val isOldXPZone = XPZoneManager.isXPZone(from)

        if (SessionManager.getSession(player) == null && ZoneHandler.isLocationValid(to)) {
            SessionManager.createSession(player)
        }

        if (oldRegion != newRegion) {
            oldRegion?.let {
                ZoneHandler.notifyLeave(player, it)
                WaveManager.removeActiveWave(player)
            }

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

        when {
            isNewXPZone && !isOldXPZone -> {
                SessionManager.updateXPZoneStatus(player, true)
                ZoneHandler.notifyXPZoneJoin(player)
                XPZoneManager.addPlayer(player, ZoneUtil.getRegionNameOfXpZone(to) ?: return println("Region name is null"))
            }
            !isNewXPZone && isOldXPZone -> {
                SessionManager.updateXPZoneStatus(player, false)
                ZoneHandler.notifyXPZoneLeave(player)
                XPZoneManager.removePlayer(player)
            }
        }
    }
}