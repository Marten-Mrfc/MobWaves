package dev.marten_mrfcyt.mobWaves.session

import dev.marten_mrfcyt.mobWaves.MobWaves
import dev.marten_mrfcyt.mobWaves.waves.handler.WaveManager
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.UUID

data class SerializablePlayerSession(
    val playerUuid: UUID,
    var currentWaveName: String? = null,
    var currentRound: Int = 0,
    var waveCenter: SerializableLocation? = null,
    var totalXPGained: Int = 0,
    var xpPerZone: Map<String, Int> = mutableMapOf(),
    var xpAccumulator: Double = 0.0,
    var isInXPZone: Boolean = false,
    var currentXPZone: String? = null,
    val maxXPTotal: Int = MobWaves.instance.config.getInt("max-xp-total", 100)
) {
    companion object {
        fun fromPlayerSession(session: PlayerSession): SerializablePlayerSession {
            return SerializablePlayerSession(
                playerUuid = session.player.uniqueId,
                currentWaveName = session.currentWave?.name,
                currentRound = session.currentRound,
                waveCenter = session.waveCenter?.let { SerializableLocation.fromLocation(it) },
                totalXPGained = session.totalXPGained,
                xpPerZone = session.xpPerZone.toMap(),
                xpAccumulator = session.xpAccumulator,
                isInXPZone = session.isInXPZone,
                currentXPZone = session.currentXPZone,
                maxXPTotal = session.maxXPTotal
            )
        }
    }

    fun toPlayerSession(player: Player): PlayerSession {
        val wave = currentWaveName?.let { WaveManager.getWaveByString(it) }
        return PlayerSession(
            player = player,
            currentWave = wave,
            currentRound = currentRound,
            waveCenter = waveCenter?.toLocation(),
            waveMobs = mutableListOf(), // ActiveMobs can't be serialized, will be repopulated
            totalXPGained = totalXPGained,
            xpPerZone = xpPerZone.toMutableMap(),
            xpAccumulator = xpAccumulator,
            isInXPZone = isInXPZone,
            currentXPZone = currentXPZone,
            maxXPTotal = maxXPTotal
        )
    }
}

data class SerializableLocation(
    val world: String,
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Float = 0f,
    val pitch: Float = 0f
) {
    companion object {
        fun fromLocation(location: Location): SerializableLocation {
            return SerializableLocation(
                world = location.world.name,
                x = location.x,
                y = location.y,
                z = location.z,
                yaw = location.yaw,
                pitch = location.pitch
            )
        }
    }

    fun toLocation(): Location? {
        val bukkitWorld = Bukkit.getWorld(world) ?: return null
        return Location(bukkitWorld, x, y, z, yaw, pitch)
    }
}