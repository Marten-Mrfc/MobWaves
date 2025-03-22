package dev.marten_mrfcyt.mobWaves.waves.handler

import dev.marten_mrfcyt.mobWaves.session.SessionManager
import dev.marten_mrfcyt.mobWaves.utils.external.getWaveMobs
import dev.marten_mrfcyt.mobWaves.utils.external.spawnMythicMob
import dev.marten_mrfcyt.mobWaves.waves.Wave
import dev.marten_mrfcyt.mobWaves.zones.ZoneUtil
import dev.marten_mrfcyt.mobWaves.zones.xp.XPZoneManager
import io.lumine.mythic.core.mobs.ActiveMob
import mlib.api.utilities.message
import org.bukkit.entity.Creature
import org.bukkit.entity.Player

object WaveSessionManager {
    fun startWave(player: Player, wave: Wave) {
        val session = SessionManager.getSession(player) ?: return
        val currentRound = session.currentRound

        if (currentRound > wave.rounds.size) {
            closeWaveSession(player)
            return
        }

        val round = wave.rounds[currentRound - 1]
        val center = session.player.location
        val spawnedMobs = mutableListOf<ActiveMob>()

        getWaveMobs(round).forEach { (mob, amount) ->
            repeat(amount) {
                val location = center.clone()
                val spawnedMob = spawnMythicMob(mob, location, wave)
                spawnedMobs.add(spawnedMob)
                if (spawnedMob.entity.bukkitEntity is Creature) {
                    (spawnedMob.entity.bukkitEntity as Creature).target = player
                }
            }
        }

        session.waveMobs = spawnedMobs
    }

    fun onMobDeath(player: Player, mob: ActiveMob) {
        val session = SessionManager.getSession(player) ?: return
        session.waveMobs.remove(mob)
        println("Mobs over: ${session.waveMobs.size} voor ${player.name}")
        if (session.waveMobs.isEmpty()) {
            val wave = session.currentWave ?: return

            if (session.currentRound >= wave.rounds.size && XPZoneManager.isXPZone(player.location)) {
                player.message("Wave ${wave.name} is voltooid! Herstart wave...")
                session.currentRound = 1
                startWave(player, wave)
            } else {
                player.message("Ronde ${session.currentRound} is voltooid.")
                println("Ronde ${session.currentRound} is voltooid voor ${player.name}")
                session.currentRound++
                startWave(player, wave)
            }
        }
    }

    fun onMobMove(player: Player, entity: ActiveMob) {
        val session = SessionManager.getSession(player) ?: return
        val wave = session.currentWave ?: return
        val round = wave.rounds[session.currentRound - 1]
        val center = session.player.location
        val leaveRadiusSquared = round.leaveRadius * round.leaveRadius

        val location = center.clone().add(
            (-round.radius..round.radius).random().toDouble(),
            0.0,
            (-round.radius..round.radius).random().toDouble()
        )

        val livingEntity = entity.entity.bukkitEntity as? org.bukkit.entity.LivingEntity ?: return

        if (session.waveMobs.contains(entity) && livingEntity.location.distanceSquared(center) > leaveRadiusSquared) {
            (livingEntity as? Creature)?.pathfinder?.moveTo(location, 1.0)
        }
        if (session.waveMobs.contains(entity)) {
            (livingEntity as? Creature)?.target = player
        }
    }

    private fun closeWaveSession(player: Player) {
        SessionManager.getSession(player)?.let { session ->
            session.waveMobs.forEach { mob ->
                mob.entity.bukkitEntity.remove()
            }
            session.waveMobs.clear()
            session.currentWave = null
            session.currentRound = 0
            session.waveCenter = null
        }
    }
}