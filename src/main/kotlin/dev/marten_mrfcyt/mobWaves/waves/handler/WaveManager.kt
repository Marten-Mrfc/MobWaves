package dev.marten_mrfcyt.mobWaves.waves.handler

import dev.marten_mrfcyt.mobWaves.utils.message
import dev.marten_mrfcyt.mobWaves.waves.Wave
import io.lumine.mythic.core.mobs.ActiveMob
import org.bukkit.Location
import org.bukkit.entity.Player

object WaveManager {
    private val activeWaves = mutableMapOf<Player, Wave>()
    private val playerRounds = mutableMapOf<Player, Int>()
    private val waveCenters = mutableMapOf<Wave, Location>()

    fun addActiveWave(player: Player, wave: Wave) {
        activeWaves[player] = wave
        playerRounds[player] = 1
        waveCenters[wave] = player.location.clone()
        WaveSessionManager.startWave(player, wave)
    }

    fun getPlayerByWave(wave: Wave): Player? = activeWaves.entries.find { it.value == wave }?.key
    fun getWaveCenter(wave: Wave): Location? = waveCenters[wave]
    fun getWaveByPlayer(player: Player): Wave? = activeWaves[player]
    fun incrementPlayerRound(player: Player) {
        playerRounds[player] = (playerRounds[player] ?: 0) + 1
    }

    fun getPlayerRound(player: Player): Int? = playerRounds[player]

    fun removeWave(player: Player) {
        val wave = activeWaves.remove(player) ?: return
        player.message("You have completed the wave: ${wave.name}")
        playerRounds.remove(player)
        waveCenters.remove(wave)
    }
}