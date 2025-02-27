package dev.marten_mrfcyt.mobWaves.utils.external

import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.protection.flags.IntegerFlag
import com.sk89q.worldguard.protection.flags.StateFlag
import com.sk89q.worldguard.protection.flags.StringFlag
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry
import org.bukkit.plugin.Plugin

object WorldGuardUtil {
    lateinit var MOBWAVE_FLAG: StateFlag
    lateinit var WAVE_FLAG: StringFlag
    lateinit var XP_ZONE_FLAG: StateFlag
    lateinit var XP_AMOUNT_FLAG: IntegerFlag
    lateinit var XP_MAX_WAVE: IntegerFlag

    fun registerFlags() {
        val worldGuard = WorldGuard.getInstance()
        val registry: FlagRegistry = worldGuard.flagRegistry

        MOBWAVE_FLAG = StateFlag("mw-enabled", false)
        WAVE_FLAG = StringFlag("mw-wave")
        XP_ZONE_FLAG = StateFlag("mw-xp-zone", false)
        XP_AMOUNT_FLAG = IntegerFlag("mw-xp-amount")
        XP_MAX_WAVE = IntegerFlag("mw-xp-max-wave")

        registry.register(MOBWAVE_FLAG)
        registry.register(WAVE_FLAG)
        registry.register(XP_ZONE_FLAG)
        registry.register(XP_AMOUNT_FLAG)
        registry.register(XP_MAX_WAVE)
    }
}