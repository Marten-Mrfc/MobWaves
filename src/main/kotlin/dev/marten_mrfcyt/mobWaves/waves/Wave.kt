// src/main/kotlin/dev/marten_mrfcyt/mobWaves/waves/Wave.kt
package dev.marten_mrfcyt.mobWaves.waves

import kotlin.collections.remove

data class Wave(
    val name: String,
    var rounds: MutableList<WaveRound> = mutableListOf()
) {
    fun addRound(round: WaveRound) {
        rounds.add(round)
    }

    fun removeRound(round: WaveRound) {
        rounds.remove(round)
    }
}