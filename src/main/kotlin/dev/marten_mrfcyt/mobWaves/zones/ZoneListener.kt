package dev.marten_mrfcyt.mobWaves.zones

import dev.marten_mrfcyt.mobWaves.MobWaves
import dev.marten_mrfcyt.mobWaves.utils.external.Discord
import dev.marten_mrfcyt.mobWaves.utils.action
import dev.marten_mrfcyt.mobWaves.waves.handler.WaveManager
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.scheduler.BukkitRunnable
val playersInRegions = mutableMapOf<String, MutableList<Player>>()

class ZoneListener(plugin: MobWaves) : Listener {

    init {
        object : BukkitRunnable() {
            override fun run() {
                updateActionBars()
            }
        }.runTaskTimer(plugin, 0L, 20L)
    }

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player
        val toLocation = event.to
        val fromLocation = event.from

        if (toLocation.blockX == fromLocation.blockX && toLocation.blockY == fromLocation.blockY && toLocation.blockZ == fromLocation.blockZ) {
            return
        }

        val regionName = ZoneUtil.getWaveName(toLocation)
        val fromRegionName = ZoneUtil.getWaveName(fromLocation)

        if (regionName != null && regionName != "NotSet") {
            onPlayerJoinZone(player, regionName)
        }

        if (fromRegionName != null && fromRegionName != "NotSet" && fromRegionName != regionName) {
            onPlayerLeaveZone(player, fromRegionName)
        }
    }

    private fun onPlayerJoinZone(player: Player, regionName: String) {
        playersInRegions.getOrPut(regionName) { mutableListOf() }.apply {
            if (!contains(player)) {
                add(player)
                WaveManager.addActiveWave(player, WaveManager.getWaveByString(regionName) ?: return)
                player.action("You have entered the $regionName MobWave region!")
                Discord().sendPlayerJoinNotification(player.name, playersInRegions[regionName]?.size ?: 0, regionName, WaveManager.getPlayerRound(player) ?: 0)
                println("${player.name} has entered the $regionName MobWave region!")
            }
        }
    }

    private fun onPlayerLeaveZone(player: Player, fromRegionName: String) {
        playersInRegions[fromRegionName]?.remove(player)
        Discord().sendPlayerLeaveNotification(player.name, playersInRegions[fromRegionName]?.size ?: 0, fromRegionName, WaveManager.getPlayerRound(player) ?: 0)
        WaveManager.removeActiveWave(player)
        player.action("You have left the $fromRegionName MobWave region!")
        println("${player.name} has left the $fromRegionName MobWave region!")
    }

    private fun updateActionBars() {
        playersInRegions.forEach { (_, players) ->
            players.forEach { player ->
                val wave = WaveManager.getWaveByPlayer(player)
                val round = WaveManager.getPlayerRound(player)
                if (wave != null && round != null) {
                    player.action("Playing wave: ${wave.name} round: $round")
                }
            }
        }
    }

    fun getPlayersInRegion(regionName: String): List<Player> {
        return playersInRegions[regionName]?.toList() ?: emptyList()
    }
}