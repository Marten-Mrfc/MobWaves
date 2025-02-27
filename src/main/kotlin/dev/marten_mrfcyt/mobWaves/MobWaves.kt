package dev.marten_mrfcyt.mobWaves

import dev.marten_mrfcyt.mobWaves.session.SessionListener
import dev.marten_mrfcyt.mobWaves.utils.external.WorldGuardUtil
import dev.marten_mrfcyt.mobWaves.waves.WaveModifier
import dev.marten_mrfcyt.mobWaves.waves.handler.MobDeathListener
import dev.marten_mrfcyt.mobWaves.zones.xp.XPZoneManager
import mlib.api.architecture.KotlinPlugin
import mlib.api.architecture.extensions.registerEvents

class MobWaves : KotlinPlugin() {
    companion object {
        lateinit var instance: MobWaves
    }

    override fun onEnable() {
        logger.info("----------------------------")
        logger.info("--- MobWaves is starting ---")
        super.onEnable()
        instance = this
        reloadConfig()
        saveConfig()
        logger.info("Instance has been set, registering commands")
        mobWavesCommands()
        logger.info("WaveCommands registered successfully, registering ZoneCommands")
        zoneCommands()
        logger.info("Commands registered successfully, loading waves")
        logger.info("Loaded: ${WaveModifier().listWaves().size} waves, starting XP timer")
        XPZoneManager.startXPTimer(this)
        logger.info("XP timer started, registering events")
        registerEvents(
            MobDeathListener(),
            SessionListener(this)
        )
        logger.info("Events registered successfully, registering flags")
        WorldGuardUtil.registerFlags()
        logger.info("Flags registered successfully")
        logger.info("--- MobWaves has started ---")
        logger.info("----------------------------")
    }

    override fun onDisable() {
        logger.info("----------------------------")
        logger.info("--- MobWaves is stopping ---")
        logger.info("Saving waves")
        WaveModifier().saveWaves()
        logger.info("Waves saved successfully!")
        logger.info("--- MobWaves has stopped ---")
        logger.info("----------------------------")
    }
}
