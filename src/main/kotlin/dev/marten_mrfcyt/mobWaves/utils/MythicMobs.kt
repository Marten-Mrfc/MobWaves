package dev.marten_mrfcyt.mobWaves.utils

import io.lumine.mythic.api.MythicProvider
import io.lumine.mythic.api.mobs.MythicMob
import dev.marten_mrfcyt.mobWaves.waves.Wave
import io.lumine.mythic.bukkit.BukkitAdapter
import io.lumine.mythic.core.mobs.ActiveMob

fun getAllMythicMobs(): List<MythicMob> {
    return MythicProvider.get().mobManager.mobTypes.toList()
}

fun getWaveMobs(wave: Wave): List<MythicMob> {
    val allMythicMobs = getAllMythicMobs()
    return wave.mobs.mapNotNull { mobName -> allMythicMobs.find { mobName == it.internalName } }
}

fun getWaveBosses(wave: Wave): List<MythicMob> {
    val allMythicMobs = getAllMythicMobs()
    return wave.bosses.mapNotNull { mobName -> allMythicMobs.find { mobName == it.internalName } }
}
fun getAllNotWaveBosses(wave: Wave): List<MythicMob> {
    val allMythicMobs = getAllMythicMobs()
    val waveBosses = getWaveBosses(wave).map { it.internalName }
    return allMythicMobs.filterNot { it.internalName in waveBosses }
}

fun getAllNotWaveMobs(wave: Wave): List<MythicMob> {
    val allMythicMobs = getAllMythicMobs()
    val waveMobs = getWaveMobs(wave).map { it.internalName }
    return allMythicMobs.filterNot { it.internalName in waveMobs }
}

fun spawnMythicMob(mob: MythicMob, location: org.bukkit.Location): ActiveMob? {
    val mob = mob.spawn(BukkitAdapter.adapt(location), 1.0)
    return mob
}