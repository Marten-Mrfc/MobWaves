package dev.marten_mrfcyt.mobWaves.waves

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