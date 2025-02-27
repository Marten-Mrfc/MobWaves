package dev.marten_mrfcyt.mobWaves.zones

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldguard.protection.flags.StateFlag
import dev.marten_mrfcyt.mobWaves.utils.external.WorldGuardUtil
import org.bukkit.Location

object ZoneUtil {
    fun isMobWaveRegion(location: Location): Boolean {
        val regionManager = ZoneManager.getRegionManager(location.world) ?: return false
        val applicableRegions = regionManager.getApplicableRegions(BukkitAdapter.asBlockVector(location))
        return applicableRegions.any { it.getFlag(WorldGuardUtil.MOBWAVE_FLAG) == StateFlag.State.ALLOW }
    }

    fun getWaveName(location: Location): String? {
        if (!isMobWaveRegion(location)) {
            return "NotSet"
        }
        val regionManager = ZoneManager.getRegionManager(location.world) ?: return null
        val applicableRegions = regionManager.getApplicableRegions(BukkitAdapter.asBlockVector(location))
        return applicableRegions.firstNotNullOfOrNull { it.getFlag(WorldGuardUtil.WAVE_FLAG) }
    }

    fun getRegionNameOfXpZone(location: Location): String? {
        val regionManager = ZoneManager.getRegionManager(location.world) ?: return null
        val applicableRegions = regionManager.getApplicableRegions(BukkitAdapter.asBlockVector(location))
        if (applicableRegions.any { it.getFlag(WorldGuardUtil.XP_ZONE_FLAG) == StateFlag.State.ALLOW }) {
            return applicableRegions.firstNotNullOfOrNull { it.id }
        }
        return null
    }
}