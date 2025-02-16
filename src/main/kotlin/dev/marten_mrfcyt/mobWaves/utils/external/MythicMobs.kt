package dev.marten_mrfcyt.mobWaves.utils.external

import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import dev.marten_mrfcyt.mobWaves.utils.BlackList
import dev.marten_mrfcyt.mobWaves.utils.SafeChecker
import gg.flyte.twilight.scheduler.async
import gg.flyte.twilight.scheduler.sync
import io.lumine.mythic.api.MythicProvider
import io.lumine.mythic.api.mobs.MythicMob
import dev.marten_mrfcyt.mobWaves.waves.WaveRound
import dev.marten_mrfcyt.mobWaves.zones.ZoneUtil
import io.lumine.mythic.bukkit.BukkitAdapter
import io.lumine.mythic.core.mobs.ActiveMob
import org.bukkit.Location
import java.io.File
import kotlin.random.Random

private lateinit var safeChecker: SafeChecker

fun getAllMythicMobs(): List<MythicMob> {
    return MythicProvider.get().mobManager.mobTypes.toList()
}

fun getWaveMobs(round: WaveRound): List<MythicMob> {
    val allMythicMobs = getAllMythicMobs()
    return round.mobs.mapNotNull { mobName -> allMythicMobs.find { mobName == it.internalName } }
}

fun getWaveBosses(round: WaveRound): List<MythicMob> {
    val allMythicMobs = getAllMythicMobs()
    return round.bosses.mapNotNull { mobName -> allMythicMobs.find { mobName == it.internalName } }
}

fun getAllNotWaveBosses(round: WaveRound): List<MythicMob> {
    val allMythicMobs = getAllMythicMobs()
    val waveBosses = getWaveBosses(round).map { it.internalName }
    val blacklistedMobs = BlackList.getAllBlackListedMobs().map { it.internalName }
    return allMythicMobs.filterNot {
        it.internalName in waveBosses || it.internalName in blacklistedMobs
    }
}

fun getAllNotWaveMobs(round: WaveRound): List<MythicMob> {
    val allMythicMobs = getAllMythicMobs()
    val waveMobs = getWaveMobs(round).map { it.internalName }
    val blacklistedMobs = BlackList.getAllBlackListedMobs().map { it.internalName }
    return allMythicMobs.filterNot {
        it.internalName in waveMobs || it.internalName in blacklistedMobs
    }
}

fun spawnMythicMob(mob: MythicMob, location: Location): ActiveMob {
    var spawnedMob: ActiveMob? = null
    val world = location.world ?: throw IllegalStateException("World cannot be null")
    val isRegion = ZoneUtil.isMobWaveRegion(location)

    fun isSafe(loc: Location): Boolean {
        return SafeChecker(world).isSafe(loc) && (!isRegion || ZoneUtil.isMobWaveRegion(loc))
    }

    if (isSafe(location)) {
        spawnedMob = mob.spawn(BukkitAdapter.adapt(location), 1.0)
        return spawnedMob ?: throw Exception("Failed to spawn mythic mob")
    }

    val safeLocations = mutableListOf<Location>()
    for (dx in -10..10) {
        for (dy in -3..2) {
            for (dz in -10..10) {
                if (dx == 0 && dy == 0 && dz == 0) continue
                if (dx * dx + dy * dy + dz * dz > 200) continue
                val candidate = location.clone().add(dx.toDouble(), dy.toDouble(), dz.toDouble())
                if (isSafe(candidate)) safeLocations.add(candidate)
            }
        }
    }

    if (safeLocations.isNotEmpty()) {
        val randomLocation = safeLocations[Random.nextInt(safeLocations.size)]
        spawnedMob = mob.spawn(BukkitAdapter.adapt(randomLocation), 1.0)
        return spawnedMob ?: throw Exception("Failed to spawn mythic mob")
    }

    throw Exception("No safe location found to spawn mythic mob")
}