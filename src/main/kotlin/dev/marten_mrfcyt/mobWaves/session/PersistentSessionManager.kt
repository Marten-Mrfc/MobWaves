package dev.marten_mrfcyt.mobWaves.session

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import dev.marten_mrfcyt.mobWaves.MobWaves
import org.bukkit.entity.Player
import java.io.File
import java.nio.file.Files
import java.time.LocalTime
import java.time.ZoneId
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.locks.ReentrantLock

object PersistentSessionManager {
    private val plugin = MobWaves.instance
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val sessionsFile = File(plugin.dataFolder, "sessions.json")
    private val playerSessionMap = ConcurrentHashMap<UUID, SerializablePlayerSession>()

    private val serverTimeZone: ZoneId = ZoneId.of("Europe/Amsterdam")

    private val sessionStartTime = LocalTime.of(18, 0)
    private val sessionEndTime = LocalTime.of(0, 0)

    init {
        if (!plugin.dataFolder.exists()) {
            plugin.dataFolder.mkdirs()
        }

        loadSessions()
        setupResetTask()
    }

    private fun isResetAllowed(): Boolean {
        val currentTime = LocalTime.now(serverTimeZone)

        // We want to prevent resets between 18:00 and 00:00, which crosses midnight
        if (sessionStartTime.isAfter(sessionEndTime)) {
            // Time period crosses midnight
            // Do NOT reset if current time is after start time OR before end time
            return !(currentTime.isAfter(sessionStartTime) || currentTime.isBefore(sessionEndTime))
        } else {
            // Simple case when start time is before end time
            return !(currentTime.isAfter(sessionStartTime) && currentTime.isBefore(sessionEndTime))
        }
    }


    private fun setupResetTask() {
        // Check more frequently - every 15 minutes
        plugin.server.scheduler.runTaskTimerAsynchronously(plugin, Runnable {
            if (isResetAllowed()) {
                resetAllSessions()
            }
        }, 18000L, 18000L) // Check every 15 minutes (15min * 60s * 20 ticks)
    }

    fun isPlayTimeAllowed(): Boolean {
        val currentTime = LocalTime.now(serverTimeZone)
        return if (sessionStartTime.isBefore(sessionEndTime)) {
            // Simple case: when start time is before end time
            currentTime.isAfter(sessionStartTime) && currentTime.isBefore(sessionEndTime)
        } else {
            // Complex case: when time period crosses midnight (e.g., 18:00 to 00:00)
            currentTime.isAfter(sessionStartTime) || currentTime.isBefore(sessionEndTime)
        }
    }

    fun getSession(player: Player): PlayerSession? {
        val uuid = player.uniqueId
        val storedSession = playerSessionMap[uuid] ?: return null
        return storedSession.toPlayerSession(player)
    }

    fun resetSession(player: Player) {
        playerSessionMap.remove(player.uniqueId)
        saveSessions()
    }

    fun resetAllSessions() {
        // Clear in-memory sessions
        playerSessionMap.clear()

        // Also delete the sessions file to ensure offline players' sessions are reset too
        try {
            if (sessionsFile.exists()) {
                sessionsFile.delete()
                // Create a new empty sessions file
                sessionsFile.createNewFile()
                Files.write(sessionsFile.toPath(), "{}".toByteArray())
            }
            plugin.logger.info("Successfully reset all player sessions (including offline players)")
        } catch (e: Exception) {
            plugin.logger.severe("Failed to reset sessions file: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun loadSessions() {
        if (!sessionsFile.exists()) return

        try {
            val json = String(Files.readAllBytes(sessionsFile.toPath()))
            val type = object : TypeToken<Map<UUID, SerializablePlayerSession>>() {}.type
            val loadedSessions: Map<UUID, SerializablePlayerSession> = gson.fromJson(json, type)
            playerSessionMap.putAll(loadedSessions)
            plugin.logger.info("Loaded ${playerSessionMap.size} player sessions")
        } catch (e: Exception) {
            plugin.logger.severe("Failed to load player sessions: ${e.message}")
            e.printStackTrace()
        }
    }

    private val saveQueue = ConcurrentLinkedQueue<UUID>()
    private var isSaving = false
    private val saveLock = ReentrantLock()

    fun saveSession(playerSession: PlayerSession) {
        val serializableSession = SerializablePlayerSession.fromPlayerSession(playerSession)
        playerSessionMap[playerSession.player.uniqueId] = serializableSession
        saveQueue.add(playerSession.player.uniqueId)

        // Trigger save process if not already running
        if (!isSaving) {
            scheduleSave()
        }
    }

    fun saveSessions() {
        playerSessionMap.forEach { (uuid, _) ->
            saveQueue.add(uuid)
        }
        if (!isSaving) {
            scheduleSave()
        }
    }

    private fun scheduleSave() {
        if (saveLock.tryLock()) {
            try {
                if (!isSaving) {
                    isSaving = true
                    plugin.server.scheduler.runTaskAsynchronously(plugin, Runnable {
                        processSaveQueue()
                    })
                }
            } finally {
                saveLock.unlock()
            }
        }
    }

    private fun processSaveQueue() {
        try {
            // Create a temporary map of sessions that need saving
            val sessionsToSave = HashMap<UUID, SerializablePlayerSession>()

            // Process up to 20 saves at once
            var processCount = 0
            while (processCount < 20 && saveQueue.isNotEmpty()) {
                val uuid = saveQueue.poll() ?: break
                playerSessionMap[uuid]?.let {
                    sessionsToSave[uuid] = it
                }
                processCount++
            }

            if (sessionsToSave.isNotEmpty()) {
                // Only write changed sessions to disk
                synchronized(sessionsFile) {
                    // If file exists, merge with existing data
                    val existingMap = if (sessionsFile.exists()) {
                        try {
                            val json = String(Files.readAllBytes(sessionsFile.toPath()))
                            val type = object : TypeToken<Map<UUID, SerializablePlayerSession>>() {}.type
                            gson.fromJson<Map<UUID, SerializablePlayerSession>>(json, type) ?: emptyMap()
                        } catch (_: Exception) {
                            plugin.logger.warning("Could not read existing sessions file, creating new one")
                            emptyMap()
                        }
                    } else emptyMap()

                    val mergedMap = HashMap(existingMap)
                    mergedMap.putAll(sessionsToSave)

                    val json = gson.toJson(mergedMap)
                    Files.write(sessionsFile.toPath(), json.toByteArray())
                }
            }

            // If more items in queue, schedule another save
            if (saveQueue.isNotEmpty()) {
                plugin.server.scheduler.runTaskAsynchronously(plugin, Runnable {
                    processSaveQueue()
                })
            } else {
                isSaving = false
            }
        } catch (e: Exception) {
            plugin.logger.severe("Failed to save player sessions: ${e.message}")
            e.printStackTrace()
            isSaving = false
        }
    }
}