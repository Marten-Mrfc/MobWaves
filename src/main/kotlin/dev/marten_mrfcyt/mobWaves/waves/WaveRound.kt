package dev.marten_mrfcyt.mobWaves.waves

import java.util.UUID

data class WaveRound(
    val id: UUID = UUID.randomUUID(),
    var mobs: MutableList<Mob> = mutableListOf(),
    var bosses: MutableList<String> = mutableListOf(),
    var radius: Int = 10,
    var waveDelay: Int = 10,
    var leaveRadius: Int = 50
) {
    fun addMob(mob: String, amount: Int) {
        val existingMob = mobs.find { it.name == mob }
        if (existingMob != null) {
            existingMob.amount += amount
        } else {
            mobs.add(Mob(mob, amount))
        }
    }

    fun removeMob(mob: String, amount: Int) {
        val existingMob = mobs.find { it.name == mob }
        if (existingMob != null) {
            if (existingMob.amount > amount) {
                existingMob.amount -= amount
            } else {
                mobs.remove(existingMob)
            }
        }
    }

    fun addBoss(boss: String) {
        bosses.add(boss)
    }

    fun removeBoss(boss: String) {
        bosses.remove(boss)
    }
}