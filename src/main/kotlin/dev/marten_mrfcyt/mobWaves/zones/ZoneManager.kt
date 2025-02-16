package dev.marten_mrfcyt.mobWaves.zones

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.protection.managers.RegionManager
import org.bukkit.World

object ZoneManager {
    private val worldGuard: WorldGuard = WorldGuard.getInstance()

    fun getRegionManager(world: World): RegionManager? {
        return worldGuard.platform.regionContainer.get(BukkitAdapter.adapt(world))
    }
}