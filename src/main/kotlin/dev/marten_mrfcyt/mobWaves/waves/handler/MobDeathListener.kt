// MobDeathListener.kt
package dev.marten_mrfcyt.mobWaves.waves.handler
import dev.marten_mrfcyt.mobWaves.waves.handler.WaveMobManager.getPlayerFromMob
import io.lumine.mythic.bukkit.MythicBukkit
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent
import io.papermc.paper.event.entity.EntityMoveEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class MobDeathListener : Listener {
    @EventHandler
    fun onMythicMobDeath(event: MythicMobDeathEvent) {
        val mob = event.mob
        val player = getPlayerFromMob(mob) ?: return
        val wave = WaveMobManager.getWaveMob(player, mob) ?: return
        println("Mob ${mob.name} died, removing from wave ${wave.name}, player ${player.name}")
        WaveSessionManager.onMobDeath(player, wave, mob)
    }
    @EventHandler
    fun onMythicMobMove(event: EntityMoveEvent) {
        val entity = event.entity
        val activeMob = MythicBukkit.inst().mobManager.getActiveMob(entity.uniqueId).orElse(null) ?: return
        val wave = WaveMobManager.getWaveMob(getPlayerFromMob(activeMob), activeMob) ?: return
        WaveSessionManager.onMobMove(wave, activeMob)
    }
}