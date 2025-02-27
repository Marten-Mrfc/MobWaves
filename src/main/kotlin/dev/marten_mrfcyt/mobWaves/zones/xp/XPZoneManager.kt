package dev.marten_mrfcyt.mobWaves.zones.xp

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldguard.protection.flags.StateFlag
import dev.marten_mrfcyt.mobWaves.MobWaves
import dev.marten_mrfcyt.mobWaves.session.PlayerSession
import dev.marten_mrfcyt.mobWaves.session.SessionManager
import dev.marten_mrfcyt.mobWaves.utils.external.WorldGuardUtil
import dev.marten_mrfcyt.mobWaves.zones.ZoneManager
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

object XPZoneManager {
    fun startXPTimer(plugin: MobWaves) {
        object : BukkitRunnable() {
            override fun run() {
                plugin.server.scheduler.runTaskAsynchronously(plugin, Runnable {
                    SessionManager.getActiveSessions().forEach { session ->
                        try {
                            if (!session.isInXPZone || session.currentXPZone == null) {
                                session.isInXPZone = false
                                return@forEach
                            }

                            val currentZone = session.currentXPZone ?: return@forEach
                            val amount = getXPAmount(session.player.location)
                            val maxZoneXP = getMaxXP(session.player.location)
                            val zoneXP = session.xpPerZone[currentZone] ?: 0

                            if (amount == null) return@forEach

                            if (maxZoneXP != null && zoneXP >= maxZoneXP * session.currentRound) {
                                return@forEach
                            }
                            if (session.totalXPGained >= session.maxXPTotal * session.currentRound) {
                                return@forEach
                            }

                            val smallAmount = amount / 100.0
                            session.xpAccumulator += smallAmount
                            val fullXP = session.xpAccumulator.toInt()

                            if (fullXP >= 1) {
                                val maxForZone = if (maxZoneXP != null) {
                                    maxZoneXP * session.currentRound - zoneXP
                                } else {
                                    Int.MAX_VALUE
                                }
                                val maxForTotal = session.maxXPTotal * session.currentRound - session.totalXPGained
                                val actualXPToGive = minOf(fullXP, maxForZone, maxForTotal)

                                if (actualXPToGive > 0) {
                                    session.player.giveExp(actualXPToGive)
                                    updateXPGain(session, actualXPToGive)
                                    session.xpAccumulator -= actualXPToGive
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                })
            }
        }.runTaskTimerAsynchronously(plugin, 20L, 3L)
    }

    fun addPlayer(player: Player, regionName: String) {
        val session = SessionManager.getSession(player) ?: return

        session.isInXPZone = true
        session.currentXPZone = regionName
        session.xpPerZone.putIfAbsent(regionName, 0)
    }

    private fun updateXPGain(session: PlayerSession, amount: Int) {
        session.totalXPGained += amount
        session.currentXPZone?.let { zone ->
            val oldZoneXP = session.xpPerZone[zone] ?: 0
            session.xpPerZone[zone] = oldZoneXP + amount
        }
    }

    fun removePlayer(player: Player) {
        val session = SessionManager.getSession(player) ?: return
        session.isInXPZone = false
        session.currentXPZone = null
    }

    private fun getXPAmount(location: Location): Int? {
        val regionManager = ZoneManager.getRegionManager(location.world) ?: return null
        val applicableRegions = regionManager.getApplicableRegions(BukkitAdapter.asBlockVector(location))
        return applicableRegions.firstNotNullOfOrNull { it.getFlag(WorldGuardUtil.XP_AMOUNT_FLAG) }
    }

    fun isXPZone(location: Location): Boolean {
        val regionManager = ZoneManager.getRegionManager(location.world) ?: return false
        val applicableRegions = regionManager.getApplicableRegions(BukkitAdapter.asBlockVector(location))
        return applicableRegions.any { it.getFlag(WorldGuardUtil.XP_ZONE_FLAG) == StateFlag.State.ALLOW }
    }

    fun getMaxXP(location: Location): Int? {
        val regionManager = ZoneManager.getRegionManager(location.world) ?: return null
        val applicableRegions = regionManager.getApplicableRegions(BukkitAdapter.asBlockVector(location))
        return applicableRegions.firstNotNullOfOrNull { it.getFlag(WorldGuardUtil.XP_MAX_WAVE) }
    }
}