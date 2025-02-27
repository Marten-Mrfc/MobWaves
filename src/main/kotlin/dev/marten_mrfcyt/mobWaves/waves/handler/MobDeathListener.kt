package dev.marten_mrfcyt.mobWaves.waves.handler

import dev.marten_mrfcyt.mobWaves.session.SessionManager
import io.lumine.mythic.bukkit.MythicBukkit
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent
import io.papermc.paper.event.entity.EntityMoveEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class MobDeathListener : Listener {
    @EventHandler
    fun onMythicMobDeath(event: MythicMobDeathEvent) {
        val mob = event.mob
        SessionManager.getActiveSessions().find { session ->
            session.waveMobs.contains(mob)
        }?.let { session ->
            WaveSessionManager.onMobDeath(session.player, mob)
        }
    }

    @EventHandler
    fun onMythicMobMove(event: EntityMoveEvent) {
        val entity = event.entity
        val activeMob = MythicBukkit.inst().mobManager.getActiveMob(entity.uniqueId).orElse(null) ?: return
        SessionManager.getActiveSessions().find { session ->
            session.waveMobs.contains(activeMob)
        }?.let { session ->
            WaveSessionManager.onMobMove(session.player, activeMob)
        }
    }
}