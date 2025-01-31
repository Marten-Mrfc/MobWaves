package dev.marten_mrfcyt.mobWaves.waves

import org.bukkit.entity.Player

data class Wave(
    val name: String,
    var mobs: MutableList<String>,
    var bosses: MutableList<String> = mutableListOf(),
    var radius: Int = 10,
    var waveAmount: Int = 1,
    var waveDelay: Int = 10,
    var leaveRadius: Int = 50,
) {
    fun addMob(mobName: String) {
        mobs.add(mobName)
    }

    fun removeMob(mobName: String) {
        mobs.remove(mobName)
    }

    fun addBoss(bossName: String) {
        bosses.add(bossName)
    }

    fun removeBoss(bossName: String) {
        bosses.remove(bossName)
    }

    fun changeRadius(radius: Int) {
        this.radius = radius
    }

    fun changeWaveAmount(waveAmount: Int) {
        this.waveAmount = waveAmount
    }

    fun changeWaveDelay(waveDelay: Int) {
        this.waveDelay = waveDelay
    }

    fun changeLeaveRadius(leaveRadius: Int) {
        this.leaveRadius = leaveRadius
    }
}