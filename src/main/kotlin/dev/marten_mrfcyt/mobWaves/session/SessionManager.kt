// kotlin/dev/marten_mrfcyt/mobWaves/session/SessionManager.kt
package dev.marten_mrfcyt.mobWaves.session

import dev.marten_mrfcyt.mobWaves.waves.Wave
import io.lumine.mythic.core.mobs.ActiveMob
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.concurrent.ConcurrentHashMap

object SessionManager {
    private val sessions = ConcurrentHashMap<Player, PlayerSession>()

    fun createSession(player: Player): PlayerSession {
        return PlayerSession(player).also { sessions[player] = it }
    }

    fun getSession(player: Player): PlayerSession? = sessions[player]
    fun getActiveSessions(): Collection<PlayerSession> = sessions.values
    fun removeSession(player: Player) {
        sessions.remove(player)
    }

    fun setWave(player: Player, wave: Wave, center: Location) {
        getOrCreateSession(player).apply {
            currentWave = wave
            currentRound = 1
            waveCenter = center
        }
    }

    fun clearWave(player: Player) {
        getOrCreateSession(player).apply {
            currentWave = null
            currentRound = 0
            waveCenter = null
            waveMobs.clear()
        }
    }

    fun updateXPZoneStatus(player: Player, inZone: Boolean) {
        getOrCreateSession(player).isInXPZone = inZone
    }

    fun updateMobs(player: Player, mobs: List<ActiveMob>) {
        getOrCreateSession(player).waveMobs = mobs.toMutableList()
    }

    private fun getOrCreateSession(player: Player): PlayerSession {
        return sessions.getOrPut(player) { PlayerSession(player) }
    }

    fun cleanup() {
        sessions.keys.removeIf { !it.isOnline }
    }
}