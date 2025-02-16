// src/main/kotlin/dev/marten_mrfcyt/mobWaves/waves/handler/WaveSessionManager.kt
package dev.marten_mrfcyt.mobWaves.waves.handler

import dev.marten_mrfcyt.mobWaves.utils.external.getWaveMobs
import dev.marten_mrfcyt.mobWaves.utils.external.spawnMythicMob
import dev.marten_mrfcyt.mobWaves.waves.Wave
import dev.marten_mrfcyt.mobWaves.waves.handler.WaveManager.getPlayerByWave
import io.lumine.mythic.core.mobs.ActiveMob
import org.bukkit.Location
import org.bukkit.entity.Creature
import org.bukkit.entity.Player

object WaveSessionManager {
    fun startWave(player: Player, wave: Wave) {
        val currentRound = WaveManager.getPlayerRound(player) ?: return
        if (currentRound > wave.rounds.size) {
            closeWaveSession(player)
            println("Wave session closed for ${player.name}")
            return
        }
        println("Starting wave ${wave.name} round $currentRound for ${player.name}")
        val round = wave.rounds[currentRound - 1]
        val center = WaveManager.getWaveCenter(wave, player) as Location
        val mobs = getWaveMobs(round)
        val spawnedMobs = mutableListOf<ActiveMob>()
        mobs.forEach { mob ->
            val location = center.clone()
            val spawnedMob = spawnMythicMob(mob, location)
            spawnedMobs.add(spawnedMob)
            if (spawnedMob.entity.bukkitEntity is Creature) {
                (spawnedMob.entity.bukkitEntity as Creature).target = player
            }
        }
        WaveMobManager.addWaveMobs(player, wave, spawnedMobs)
    }

    fun onMobDeath(player: Player, wave: Wave, mob: ActiveMob) {
        WaveMobManager.removeWaveMob(player, wave, mob)
        val mobs = WaveMobManager.getWaveMobs(player, wave) ?: return
        if (mobs.isEmpty()) {
            println("Wave ${wave.name} round ${WaveManager.getPlayerRound(player)} completed for ${player.name}")
            WaveManager.incrementPlayerRound(player)
            println("Incremented round to ${WaveManager.getPlayerRound(player)} for ${player.name}")
            startWave(player, wave)
        }
        println("Mob ${mob.type.internalName} died")
    }

    fun onMobMove(wave: Wave, entity: ActiveMob) {
        val currentRound = WaveManager.getPlayerRound(getPlayerByWave(wave) ?: return) ?: return
        val round = wave.rounds[currentRound - 1]
        val center = WaveManager.getWaveCenter(wave, getPlayerByWave(wave)?: return) ?: return
        val leaveRadiusSquared = round.leaveRadius * round.leaveRadius
        val player = getPlayerByWave(wave) ?: return
        val mobs = WaveMobManager.getWaveMobs(player, wave) ?: return
        val location = center.clone().add((-round.radius..round.radius).random().toDouble(), 0.0, (-round.radius..round.radius).random().toDouble())
        val livingEntity = entity.entity.bukkitEntity as? org.bukkit.entity.LivingEntity ?: return

        if (mobs.contains(entity) && livingEntity.location.distanceSquared(center) > leaveRadiusSquared) {
            (livingEntity as? Creature)?.pathfinder?.moveTo(location, 1.0)
        }
        if (mobs.contains(entity)) {
            (livingEntity as? Creature)?.target = getPlayerByWave(wave)
        }
    }

    private fun closeWaveSession(player: Player) {
        WaveManager.removeWave(player)
        println("Wave session closed for ${player.name}")
    }
}