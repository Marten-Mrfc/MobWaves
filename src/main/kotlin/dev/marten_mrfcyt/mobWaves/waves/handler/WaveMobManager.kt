package dev.marten_mrfcyt.mobWaves.waves.handler

import dev.marten_mrfcyt.mobWaves.waves.Wave
import io.lumine.mythic.core.mobs.ActiveMob

object WaveMobManager {
    private val activeWaveMobs = mutableMapOf<Wave, MutableList<ActiveMob>>()

    fun addWaveMobs(wave: Wave, mobs: List<ActiveMob>) {
        activeWaveMobs[wave] = mobs.toMutableList()
    }

    fun getWaveMobs(wave: Wave): MutableList<ActiveMob>? = activeWaveMobs[wave]
    fun getWaveMob(mob: ActiveMob): Wave? = activeWaveMobs.entries.find { mob in it.value }?.key
    fun removeWaveMob(wave: Wave, mob: ActiveMob) {
        activeWaveMobs[wave]?.remove(mob)
    }

    fun clearWaveMobs(wave: Wave) {
        activeWaveMobs.remove(wave)
    }
}