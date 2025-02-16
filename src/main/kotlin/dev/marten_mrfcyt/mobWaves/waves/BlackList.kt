package dev.marten_mrfcyt.mobWaves.waves

import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import dev.marten_mrfcyt.mobWaves.utils.external.getAllMythicMobs
import io.lumine.mythic.api.mobs.MythicMob
import java.nio.file.Files
import java.nio.file.Path

object BlackList {
    private val gson = Gson()
    private val blacklistPath = Path.of("plugins", "MobWaves", "blacklist.json")
    private val type = object : TypeToken<List<String>>() {}.type

    init {
        createBlacklistFileIfNotExists()
    }

    private fun createBlacklistFileIfNotExists() {
        if (!Files.exists(blacklistPath)) {
            Files.createDirectories(blacklistPath.parent)
            Files.writeString(blacklistPath, "[]")
        }
    }

    fun getAllBlackListedMobs(): List<MythicMob> = try {
        val blacklistedNames: List<String> = gson.fromJson(Files.newBufferedReader(blacklistPath), type) ?: emptyList()
        val allMythicMobs = getAllMythicMobs()

        blacklistedNames.mapNotNull { name ->
            allMythicMobs.find { it.internalName == name }
        }
    } catch (e: Exception) {
        System.err.println("Error reading blacklist file: ${e.message}")
        emptyList()
    }

    fun addBlackListedMob(mob: MythicMob): Boolean {
        return try {
            val blacklistedMobs = getAllBlackListedMobs().toMutableList()
            if (blacklistedMobs.any { it.internalName == mob.internalName }) {
                return false
            }
            blacklistedMobs.add(mob)
            saveBlacklist(blacklistedMobs)
            true
        } catch (e: Exception) {
            System.err.println("Error adding mob to blacklist: ${e.message}")
            false
        }
    }

    fun removeBlackListedMob(mob: MythicMob): Boolean {
        return try {
            val blacklistedMobs = getAllBlackListedMobs().toMutableList()
            if (!blacklistedMobs.removeIf { it.internalName == mob.internalName }) {
                return false
            }
            saveBlacklist(blacklistedMobs)
            true
        } catch (e: Exception) {
            System.err.println("Error removing mob from blacklist: ${e.message}")
            false
        }
    }

    private fun saveBlacklist(mobs: List<MythicMob>) {
        val json = gson.toJson(mobs.map { it.internalName })
        Files.writeString(blacklistPath, json)
    }
}