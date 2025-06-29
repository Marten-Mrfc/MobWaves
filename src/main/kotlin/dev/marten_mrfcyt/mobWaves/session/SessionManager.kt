package dev.marten_mrfcyt.mobWaves.session

import dev.marten_mrfcyt.mobWaves.waves.Wave
import io.lumine.mythic.core.mobs.ActiveMob
import org.bukkit.Location
import org.bukkit.entity.Player
import mlib.api.utilities.message
import java.util.concurrent.ConcurrentHashMap

object SessionManager {
    private val sessions = ConcurrentHashMap<Player, PlayerSession>()

    fun createSession(player: Player): PlayerSession {
        // Try to get existing session from persistent storage first
        val persistentSession = PersistentSessionManager.getSession(player)

        // Always create a session object regardless of time
        val session = persistentSession?.also { sessions[player] = it }
            ?: PlayerSession(player).also { sessions[player] = it }

        // Return the session object regardless of time restrictions
        return session
    }

    fun getSession(player: Player): PlayerSession? = sessions[player]
    fun getActiveSessions(): Collection<PlayerSession> = sessions.values

    fun removeSession(player: Player) {
        sessions[player]?.let { PersistentSessionManager.saveSession(it) }
        sessions.remove(player)
    }

    fun setWave(player: Player, wave: Wave, center: Location): Boolean {
        // Check if it's allowed play time before setting the wave
        if (!PersistentSessionManager.isPlayTimeAllowed()) {
            player.message("Helaas, je kan alleen tussen 18:00 en 00:00 waves spelen.")
            return false
        }

        getOrCreateSession(player).apply {
            currentWave = wave
            currentRound = 1
            waveCenter = center
        }
        return true
    }

    fun clearWave(player: Player) {
        getOrCreateSession(player).apply {
            currentWave = null
            currentRound = 0
            waveCenter = null
            waveMobs.clear()
        }
    }

    fun resetSession(player: Player) {
        getOrCreateSession(player).apply {
            currentWave = null
            currentRound = 0
            waveCenter = null
            waveMobs.clear()
            isInXPZone = false
            // Reset any other session properties that should be reset
        }
    }

    fun updateXPZoneStatus(player: Player, inZone: Boolean): Boolean {
        // Check if it's allowed play time before allowing XP zones
        if (inZone && !PersistentSessionManager.isPlayTimeAllowed()) {
            player.message("Helaas, je kan alleen tussen 18:00 en 00:00 XP verdienen.")
            return false
        }

        getOrCreateSession(player).isInXPZone = inZone
        return true
    }

    fun updateMobs(player: Player, mobs: List<ActiveMob>) {
        getOrCreateSession(player).waveMobs = mobs.toMutableList()
    }

    private fun getOrCreateSession(player: Player): PlayerSession {
        return sessions.getOrPut(player) { createSession(player) }
    }

    fun cleanup() {
        // Save sessions for offline players before removing them
        sessions.entries.removeIf { (player, session) ->
            if (!player.isOnline) {
                PersistentSessionManager.saveSession(session)
                true
            } else {
                false
            }
        }
    }

    // Call this when server is shutting down
    fun saveAllSessions() {
        sessions.values.forEach { PersistentSessionManager.saveSession(it) }
    }
}