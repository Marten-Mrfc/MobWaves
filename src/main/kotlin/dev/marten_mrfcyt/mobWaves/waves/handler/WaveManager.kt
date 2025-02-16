package dev.marten_mrfcyt.mobWaves.waves.handler

import dev.marten_mrfcyt.mobWaves.utils.error
import dev.marten_mrfcyt.mobWaves.utils.message
import dev.marten_mrfcyt.mobWaves.waves.Wave
import dev.marten_mrfcyt.mobWaves.waves.WaveModifier
import dev.marten_mrfcyt.mobWaves.zones.ZoneManager
import dev.marten_mrfcyt.mobWaves.zones.playersInRegions
import org.bukkit.Location
import org.bukkit.entity.Player

object WaveManager {
    private val activeWaves = mutableMapOf<Player, Wave>()
    private val playerRounds = mutableMapOf<Player, Int>()
    private val waveCenters = mutableMapOf<HashMap<Player, Wave>, Location>()

    fun addActiveWave(player: Player, wave: Wave) {
        if (activeWaves.containsKey(player)) {
            player.error("You are already in an active wave.")
            println("${player.name} is already in an active wave: ${activeWaves[player]?.name}")
            return
        }
        activeWaves[player] = wave
        playerRounds[player] = 1
        waveCenters.put(hashMapOf(player to wave), player.location)
        WaveSessionManager.startWave(player, wave)
    }

    fun removeActiveWave(player: Player) {
        val wave = activeWaves[player]
        if (wave == null) {
            return
        }
        val mobs = WaveMobManager.getWaveMobs(player, wave)
        if (mobs == null) {
            player.error("No mobs found for the wave.")
            println("No mobs found for the wave: ${wave.name}")
        }
        mobs?.forEach { mob ->
            mob.entity.bukkitEntity.remove()
        }
        playerRounds.remove(player)
        WaveMobManager.clearWaveMobs(player, wave)
        activeWaves.remove(player)
        waveCenters.remove(hashMapOf(player to wave))
        println("${player.name} has removed the active wave: ${wave.name}")
        player.message("Active wave removed successfully.")
    }

    fun getPlayerByWave(wave: Wave): Player? = activeWaves.entries.first { it.value == wave }.key
    fun getWaveCenter(wave: Wave, player: Player): Location? = waveCenters[hashMapOf(player to wave)]
    fun getWaveByPlayer(player: Player): Wave? = activeWaves[player]
    fun getWaveByString(name: String): Wave? = WaveModifier().listWaves().first { it.name == name }
    fun incrementPlayerRound(player: Player) {
        playerRounds[player] = (playerRounds[player] ?: 0) + 1
    }
    fun getPlayerRound(player: Player): Int? = playerRounds[player]

    fun removeWave(player: Player) {
        val wave = activeWaves.remove(player) ?: return
        player.message("You have completed the wave: ${wave.name}")
        println("${player.name} has completed the wave: ${wave.name}")
        playerRounds.remove(player)
        waveCenters.remove(hashMapOf(player to wave))
    }
}