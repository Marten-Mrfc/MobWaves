package dev.marten_mrfcyt.mobWaves.session

import dev.marten_mrfcyt.mobWaves.MobWaves
import dev.marten_mrfcyt.mobWaves.waves.Wave
import io.lumine.mythic.core.mobs.ActiveMob
import org.bukkit.Location
import org.bukkit.entity.Player

data class PlayerSession(
    val player: Player,
    var currentWave: Wave? = null,
    var currentRound: Int = 0,
    var waveCenter: Location? = null,
    var waveMobs: MutableList<ActiveMob> = mutableListOf(),
    var totalXPGained: Int = 0,
    var xpPerZone: MutableMap<String, Int> = mutableMapOf(),
    var xpAccumulator: Double = 0.0,
    var isInXPZone: Boolean = false,
    var currentXPZone: String? = null,
    var maxXPTotal: Int = 100
) {
    init {
        MobWaves.instance.logger.info(this.toString())
    }

    override fun toString(): String {
        return "PlayerSession(player=$player, currentWave=$currentWave, currentRound=$currentRound, waveCenter=$waveCenter, waveMobs=$waveMobs, totalXPGained=$totalXPGained, xpPerZone=$xpPerZone, xpAccumulator=$xpAccumulator, isInXPZone=$isInXPZone, currentXPZone=$currentXPZone, maxXPTotal=$maxXPTotal)"
    }
}