package dev.marten_mrfcyt.mobWaves.utils.external

import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.protection.flags.StateFlag
import com.sk89q.worldguard.protection.flags.StringFlag
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry
import org.bukkit.plugin.Plugin

object WorldGuardUtil {
    lateinit var MOBWAVE_FLAG: StateFlag
    lateinit var WAVE_FLAG: StringFlag

    fun registerFlags() {
        val worldGuard = WorldGuard.getInstance()
        val registry: FlagRegistry = worldGuard.flagRegistry

        MOBWAVE_FLAG = StateFlag("mw-enabled", false)
        WAVE_FLAG = StringFlag("mw-wave")

        registry.register(MOBWAVE_FLAG)
        registry.register(WAVE_FLAG)
    }
}