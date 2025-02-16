package dev.marten_mrfcyt.mobWaves.waves.handler

import dev.marten_mrfcyt.mobWaves.waves.Wave
import io.lumine.mythic.core.mobs.ActiveMob
import org.bukkit.entity.Player

object WaveMobManager {
    private val activeWaveMobs = mutableMapOf<Player, MutableMap<Wave, MutableList<ActiveMob>>>()

    fun addWaveMobs(player: Player, wave: Wave, mobs: List<ActiveMob>) {
        val playerMobs = activeWaveMobs.getOrPut(player) { mutableMapOf() }
        playerMobs[wave] = mobs.toMutableList()
    }

    fun getWaveMobs(player: Player, wave: Wave): MutableList<ActiveMob>? = activeWaveMobs[player]?.get(wave)
    fun getWaveMob(player: Player?, mob: ActiveMob): Wave? = activeWaveMobs[player]?.entries?.find { mob in it.value }?.key
    fun getPlayerFromMob(mob: ActiveMob): Player? = activeWaveMobs.entries.find { mob in it.value.values.flatten() }?.key
    fun removeWaveMob(player: Player, wave: Wave, mob: ActiveMob) {
        activeWaveMobs[player]?.get(wave)?.remove(mob)
    }

    fun clearWaveMobs(player: Player, wave: Wave) {
        activeWaveMobs[player]?.remove(wave)
    }
}