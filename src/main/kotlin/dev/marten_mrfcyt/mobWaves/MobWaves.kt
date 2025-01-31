package dev.marten_mrfcyt.mobWaves

import dev.marten_mrfcyt.mobWaves.utils.gui.InventoryClickListener
import dev.marten_mrfcyt.mobWaves.waves.WaveModifier
import dev.marten_mrfcyt.mobWaves.waves.handler.MobDeathListener
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
        logger.info("Registering commands")
        mobWavesCommands()
        logger.info("Commands registered successfully!")
        logger.info("Registering events")
        registerEvents(
            InventoryClickListener(this),
            MobDeathListener()
        )
        logger.info("Events registered successfully!")
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
