package dev.marten_mrfcyt.mobWaves

import dev.marten_mrfcyt.mobWaves.utils.external.WorldGuardUtil
import dev.marten_mrfcyt.mobWaves.utils.gui.InventoryClickListener
import dev.marten_mrfcyt.mobWaves.waves.WaveModifier
import dev.marten_mrfcyt.mobWaves.waves.handler.MobDeathListener
import dev.marten_mrfcyt.mobWaves.zones.ZoneListener
import gg.flyte.twilight.Twilight
import lirand.api.architecture.KotlinPlugin
import lirand.api.extensions.server.registerEvents

class MobWaves : KotlinPlugin() {
    companion object {
        lateinit var instance: MobWaves
    }
    override fun onEnable() {
        logger.info("----------------------------")
        logger.info("--- MobWaves is starting ---")
        instance = this
        reloadConfig()
        Twilight.plugin = this
        saveConfig()
        logger.info("Instance has been set, registering commands")
        mobWavesCommands()
        logger.info("WaveCommands registered successfully, registering ZoneCommands")
        zoneCommands()
        logger.info("Commands registered successfully, loading waves")
        logger.info("Loaded: ${WaveModifier().listWaves().size} waves, registering events")
        registerEvents(
            InventoryClickListener(this),
            MobDeathListener(),
            ZoneListener(this)
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
