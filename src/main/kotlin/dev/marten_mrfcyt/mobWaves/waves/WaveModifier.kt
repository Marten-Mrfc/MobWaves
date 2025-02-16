package dev.marten_mrfcyt.mobWaves.waves

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.util.UUID

class WaveModifier {
    private val gson = Gson()
    private val waveFile = File("plugins/MobWaves/waves.json")
    private val waveType = object : TypeToken<List<Wave>>() {}.type
    private var waves: MutableList<Wave> = mutableListOf()

    init {
        if (!waveFile.parentFile.exists()) {
            waveFile.parentFile.mkdirs()
        }
        if (waveFile.exists()) {
            waveFile.reader().use {
                waves = gson.fromJson(it, waveType) ?: mutableListOf()
            }
        } else {
            saveWaves()
        }
    }

    fun createWave(name: String): Boolean {
        val wave = Wave(name, mutableListOf(WaveRound()))
        waves.add(wave)
        saveWaves()
        return true
    }

    fun saveWaves() {
        try {
            waveFile.writer().use {
                gson.toJson(waves, it)
            }
        } catch (e: Exception) {
            println("Error saving waves: ${e.message}")
            e.printStackTrace()
        }
    }

    fun deleteWave(name: String) {
        waves.removeIf { it.name == name }
        saveWaves()
    }

    fun listWaves(): List<Wave> = waves

    fun <T> modifyWave(wave: Wave, item: T, action: Wave.(T) -> Unit) {
        waves.find { it.name == wave.name }?.action(item)
        wave.action(item)
        saveWaves()
    }
    fun <T> modifyWaveRound(waveRound: WaveRound, item: T, action: WaveRound.(T) -> Unit) {
        waves.flatMap { it.rounds }
            .find { it.id == waveRound.id }
            ?.action(item)
        waveRound.action(item)
        saveWaves()
    }
}