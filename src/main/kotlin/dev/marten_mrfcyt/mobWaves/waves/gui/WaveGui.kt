package dev.marten_mrfcyt.mobWaves.waves.gui

import dev.marten_mrfcyt.mobWaves.utils.asMini
import dev.marten_mrfcyt.mobWaves.utils.gui.Gui
import dev.marten_mrfcyt.mobWaves.utils.message
import dev.marten_mrfcyt.mobWaves.waves.Wave
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class WaveGui(wave: Wave, source: CommandSender) {
    init {
        val gui = Gui("WaveGui".asMini(), 18).apply {
            item(Material.DIAMOND_SWORD) {
                name("Wave: ${wave.name}".asMini())
                slots(13)
            }
            item(Material.ZOMBIE_HEAD) {
                name("<yellow>Edit Mobs".asMini())
                description(listOf("Click to edit the mobs in this wave".asMini()))
                slots(1)
                executes { event ->
                    event.isCancelled = true
                    event.whoClicked.message("Edit Mobs")
                    open(wave, source as Player)
                }
            }
            item(Material.WITHER_SKELETON_SKULL) {
                name("<yellow>Edit Bosses".asMini())
                description(listOf("Click to edit the bosses in this wave".asMini()))
                slots(4)
                executes { event ->
                    event.isCancelled = true
                    event.whoClicked.message("Edit Bosses")
                    openBosses(wave, source as Player)
                }
            }
            item(Material.REDSTONE_LAMP) {
                name("<yellow>Edit Wave Settings".asMini())
                description(listOf("Click to edit the settings of this wave".asMini()))
                slots(7)
                executes { event ->
                    event.isCancelled = true
                    event.whoClicked.message("Edit Wave Settings")
                    openSettings(wave, source as Player)
                }
            }
        }
        gui.open(source as Player)
    }
}