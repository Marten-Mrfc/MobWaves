package dev.marten_mrfcyt.mobWaves.waves.handler

import dev.marten_mrfcyt.mobWaves.MobWaves
import dev.marten_mrfcyt.mobWaves.session.PersistentSessionManager
import dev.marten_mrfcyt.mobWaves.session.SessionManager
import dev.marten_mrfcyt.mobWaves.waves.Wave
import dev.marten_mrfcyt.mobWaves.waves.WaveModifier
import mlib.api.utilities.error
import mlib.api.utilities.message
import org.bukkit.entity.Player

object WaveManager {
    fun addActiveWave(player: Player, wave: Wave) {
        // First check time restrictions
        if (!PersistentSessionManager.isPlayTimeAllowed()) {
            player.message("Helaas, je kan alleen tussen 18:00 en 00:00 waves spelen.")
            return
        }

        val session = SessionManager.getSession(player)
        if (session?.currentWave != null) {
            player.error("Je bent al in een actieve wave: ${session.currentWave?.name}. Dit is een bug, maak een bug-report.")
            MobWaves.instance.logger.warning("${player.name} is already in an active wave: ${session.currentWave?.name}")
            return
        }

        if (!SessionManager.setWave(player, wave, player.location)) {
            return // Time restriction check in SessionManager failed
        }

        WaveSessionManager.startWave(player, wave)
    }

    fun removeActiveWave(player: Player) {
        val session = SessionManager.getSession(player) ?: return
        val wave = session.currentWave ?: return

        session.waveMobs.forEach { mob ->
            mob.entity.bukkitEntity.remove()
        }
        SessionManager.clearWave(player)
        player.message("Actieve wave: ${wave.name} is gestopt.")
    }

    fun getWaveByString(name: String): Wave? =
        WaveModifier().listWaves().firstOrNull { it.name == name }
}