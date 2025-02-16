package dev.marten_mrfcyt.mobWaves.waves

import java.util.UUID

data class WaveRound(
    val id: UUID = UUID.randomUUID(),
    var mobs: MutableList<String> = mutableListOf(),
    var bosses: MutableList<String> = mutableListOf(),
    var radius: Int = 10,
    var waveDelay: Int = 10,
    var leaveRadius: Int = 50
) {
    fun addMob(mob: String) {
        mobs.add(mob)
    }

    fun removeMob(mob: String) {
        mobs.remove(mob)
    }

    fun addBoss(boss: String) {
        bosses.add(boss)
    }

    fun removeBoss(boss: String) {
        bosses.remove(boss)
    }
}