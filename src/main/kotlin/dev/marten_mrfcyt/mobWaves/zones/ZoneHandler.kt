package dev.marten_mrfcyt.mobWaves.zones

import dev.marten_mrfcyt.mobWaves.session.SessionManager
import dev.marten_mrfcyt.mobWaves.utils.external.Discord
import dev.marten_mrfcyt.mobWaves.zones.xp.XPZoneManager
import mlib.api.utilities.action
import org.bukkit.Location
import org.bukkit.entity.Player
import java.awt.Color

object ZoneHandler {
    fun updateActionBars() {
        SessionManager.getActiveSessions().forEach { session ->
            session.currentWave?.let { wave ->
                val xpInfo = if (session.isInXPZone) {
                    val currentZone = session.currentXPZone
                    val zoneXp = if (currentZone != null) session.xpPerZone[currentZone] ?: 0 else 0
                    val maxXpTotal = session.maxXPTotal
                    val totalXp = session.totalXPGained
                    val maxXp = XPZoneManager.getMaxXP(session.player.location)?.times(session.currentRound) ?: 0
                    " <gray>| <yellow>Zone XP<gray>: <white>$zoneXp<gray>/<white>$maxXp <gray>| <yellow>Totaal XP<gray>: <white>$totalXp<gray>/<white>$maxXpTotal"
                } else ""

                session.player.action("<yellow>Wave<gray>: <white>${wave.name.replace("_", " ")} <gray>- <white>${session.currentRound}$xpInfo")
            }
        }
    }

    fun notifyJoin(player: Player, region: String) {
        val title = "Player Joined"
        val description = "${player.name} has joined the region $region."
        val color = Color.GREEN
        val fields = listOf(
            Discord.EmbedObject.Field("Wave", region.replace("_", " "), true),
            Discord.EmbedObject.Field("Player Count", SessionManager.getActiveSessions().count { it.currentWave?.name == region }.toString(), true),
            Discord.EmbedObject.Field("Round", (SessionManager.getSession(player)?.currentRound ?: 0).toString(), true)
        )
        Discord().sendNotification(title, description, color, fields)
    }

    fun notifyLeave(player: Player, region: String) {
        val title = "Player Left"
        val description = "${player.name} has left the region $region."
        val color = Color.RED
        val fields = listOf(
            Discord.EmbedObject.Field("Player Count", SessionManager.getActiveSessions().count { it.currentWave?.name == region }.toString(), true),
            Discord.EmbedObject.Field("Round", (SessionManager.getSession(player)?.currentRound ?: 0).toString(), true)
        )
        Discord().sendNotification(title, description, color, fields)
    }

    fun notifyXPZoneJoin(player: Player) {
        val title = "Player Joined XP Zone"
        val description = "${player.name} has joined the XP Zone with a cap of: ${XPZoneManager.getMaxXP(player.location)}."
        val color = Color.GREEN
        val fields = listOf(
            Discord.EmbedObject.Field("Player Count", SessionManager.getActiveSessions().count { it.isInXPZone }.toString(), true),
            Discord.EmbedObject.Field("XP Gained", (SessionManager.getSession(player)?.totalXPGained ?: 0).toString(), true)
        )
        Discord().sendNotification(title, description, color, fields)
    }

    fun notifyXPZoneLeave(player: Player) {
        val title = "Player Left XP Zone"
        val description = "${player.name} has left the XP Zone"
        val color = Color.RED
        val fields = listOf(
            Discord.EmbedObject.Field("Player Count", SessionManager.getActiveSessions().count { it.isInXPZone }.toString(), true),
            Discord.EmbedObject.Field("XP Gained", (SessionManager.getSession(player)?.totalXPGained ?: 0).toString(), true)
        )
        Discord().sendNotification(title, description, color, fields)
    }

    fun isLocationValid(location: Location): Boolean {
        return ZoneUtil.getWaveName(location) != null || XPZoneManager.isXPZone(location)
    }
}