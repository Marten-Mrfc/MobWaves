package dev.marten_mrfcyt.mobWaves.utils.external

import dev.marten_mrfcyt.mobWaves.waves.BlackList
import dev.marten_mrfcyt.mobWaves.utils.SafeChecker
import dev.marten_mrfcyt.mobWaves.waves.Wave
import io.lumine.mythic.api.MythicProvider
import io.lumine.mythic.api.mobs.MythicMob
import dev.marten_mrfcyt.mobWaves.waves.WaveRound
import dev.marten_mrfcyt.mobWaves.zones.ZoneUtil
import io.lumine.mythic.bukkit.BukkitAdapter
import io.lumine.mythic.core.mobs.ActiveMob
import org.bukkit.Location
import kotlin.random.Random
private lateinit var safeChecker: SafeChecker

fun getAllMythicMobs(): List<MythicMob> {
    return MythicProvider.get().mobManager.mobTypes.toList()
}

fun getWaveMobs(round: WaveRound): HashMap<MythicMob, Int> {
    val allMythicMobs = getAllMythicMobs()
    val mobMap = HashMap<MythicMob, Int>()
    round.mobs.forEach { mob ->
        val mythicMob = allMythicMobs.find { it.internalName == mob.name }
        if (mythicMob != null) {
            mobMap[mythicMob] = mob.amount
        }
    }
    return mobMap
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

fun getAllNotBlackListMobs(round: WaveRound): List<MythicMob> {
    val allMythicMobs = getAllMythicMobs()
    val blacklistedMobs = BlackList.getAllBlackListedMobs().map { it.internalName }
    return allMythicMobs.filterNot {
        it.internalName in blacklistedMobs
    }
}
fun spawnMythicMob(mob: MythicMob, location: Location, wave: Wave): ActiveMob {
    val world = location.world ?: throw IllegalStateException("World cannot be null")

    val waveName = wave.name
    val safeLocations = mutableListOf<Location>()
    val baseX = location.blockX
    val baseY = location.blockY
    val baseZ = location.blockZ

    for (dx in -10..10) {
        for (dy in -3..2) {
            for (dz in -10..10) {
                if (dx == 0 && dy == 0 && dz == 0) continue
                if (dx * dx + dy * dy + dz * dz > 200) continue

                val candidate = Location(world, (baseX + dx).toDouble(), (baseY + dy).toDouble(), (baseZ + dz).toDouble())
                if (isSafe(candidate, waveName)) {
                    safeLocations.add(candidate)
                }
            }
        }
    }

    if (safeLocations.isNotEmpty()) {
        val randomLocation = safeLocations[Random.nextInt(safeLocations.size)]
        val centeredLocation = randomLocation.add(0.5, 0.0, 0.5)
        return mob.spawn(BukkitAdapter.adapt(centeredLocation), 1.0) ?: throw Exception("Failed to spawn mythic mob")
    }

    throw Exception("No safe location found to spawn mythic mob within wave region $waveName")
}

private fun isSafe(loc: Location, waveName: String): Boolean {
    val world = loc.world ?: return false
    val isPhysicallySafe = world.getBlockAt(loc.blockX, loc.blockY - 1, loc.blockZ).type.isSolid &&
            !world.getBlockAt(loc.blockX, loc.blockY, loc.blockZ).type.isSolid &&
            !world.getBlockAt(loc.blockX, loc.blockY + 1, loc.blockZ).type.isSolid

    val locWaveName = ZoneUtil.getWaveName(loc)
    return isPhysicallySafe && locWaveName == waveName
}