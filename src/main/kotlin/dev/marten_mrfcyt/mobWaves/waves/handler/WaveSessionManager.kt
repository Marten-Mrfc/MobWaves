// WaveSessionManager.kt
package dev.marten_mrfcyt.mobWaves.waves.handler

import dev.marten_mrfcyt.mobWaves.utils.getWaveMobs
import dev.marten_mrfcyt.mobWaves.utils.spawnMythicMob
import dev.marten_mrfcyt.mobWaves.waves.Wave
import io.lumine.mythic.core.mobs.ActiveMob
import org.bukkit.Location
import org.bukkit.entity.Creature
import org.bukkit.entity.Player

object WaveSessionManager {
    fun startWave(player: Player, wave: Wave) {
        val currentRound = WaveManager.getPlayerRound(player) ?: return
        if (currentRound > wave.waveAmount) {
            closeWaveSession(player)
            return
        }

        val center = WaveManager.getWaveCenter(wave) as Location
        val mobs = getWaveMobs(wave)
        val spawnedMobs = mutableListOf<ActiveMob>()
        val radius = wave.radius
        mobs.forEach { mob ->
            val location = center.clone().add((-radius..radius).random().toDouble(), 0.0, (-radius..radius).random().toDouble())
            val spawnedMob = spawnMythicMob(mob, location) ?: return
            spawnedMobs.add(spawnedMob)
            if (spawnedMob.entity.bukkitEntity is Creature) {
                (spawnedMob.entity.bukkitEntity as Creature).target = player
            }
        }
        WaveMobManager.addWaveMobs(wave, spawnedMobs)
    }

    fun onMobDeath(wave: Wave, mob: ActiveMob) {
        WaveMobManager.removeWaveMob(wave, mob)
        val mobs = WaveMobManager.getWaveMobs(wave) ?: return
        if (mobs.isEmpty()) {
            val player = WaveManager.getPlayerByWave(wave) ?: return
            WaveManager.incrementPlayerRound(player)
            startWave(player, wave)
        }
    }

    fun onMobMove(wave: Wave, entity: ActiveMob) {
        val center = WaveManager.getWaveCenter(wave) ?: return
        val leaveRadiusSquared = wave.leaveRadius * wave.leaveRadius
        val mobs = WaveMobManager.getWaveMobs(wave) ?: return
        val location = center.clone().add((-wave.radius..wave.radius).random().toDouble(), 0.0, (-wave.radius..wave.radius).random().toDouble())
        val livingEntity = entity.entity.bukkitEntity as? org.bukkit.entity.LivingEntity ?: return

        if (mobs.contains(entity) && livingEntity.location.distanceSquared(center) > leaveRadiusSquared) {
            (livingEntity as? Creature)?.pathfinder?.moveTo(location, 1.0)
        }
        if (mobs.contains(entity)) {
            (livingEntity as? Creature)?.target = WaveManager.getPlayerByWave(wave)
        }
    }
    private fun closeWaveSession(player: Player) {
        WaveManager.removeWave(player)
    }

}