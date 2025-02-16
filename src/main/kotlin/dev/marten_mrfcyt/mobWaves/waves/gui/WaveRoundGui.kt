package dev.marten_mrfcyt.mobWaves.waves.gui

import dev.marten_mrfcyt.mobWaves.utils.asMini
import dev.marten_mrfcyt.mobWaves.utils.gui.Gui
import dev.marten_mrfcyt.mobWaves.utils.message
import dev.marten_mrfcyt.mobWaves.waves.Wave
import dev.marten_mrfcyt.mobWaves.waves.WaveRound
import dev.marten_mrfcyt.mobWaves.waves.WaveModifier
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class WaveRoundGui(private val wave: Wave, private val round: WaveRound, private val source: CommandSender) {
    init {
        val gui = Gui("WaveRoundGui".asMini(), 18).apply {
            item(Material.DIAMOND_SWORD) {
                name("Round: ${wave.name}".asMini())
                slots(13)
            }
            item(Material.ZOMBIE_HEAD) {
                name("<yellow>Edit Mobs".asMini())
                description(listOf("Click to edit the mobs in this round".asMini()))
                slots(1)
                executes { event ->
                    event.isCancelled = true
                    event.whoClicked.message("Edit Mobs")
                    open(round, source as Player, 0, 0, wave)
                }
            }
            item(Material.WITHER_SKELETON_SKULL) {
                name("<yellow>Edit Bosses".asMini())
                description(listOf("Click to edit the bosses in this round".asMini()))
                slots(3)
                executes { event ->
                    event.isCancelled = true
                    event.whoClicked.message("Edit Bosses")
                    openBosses(round, source as Player, 0, 0, wave)
                }
            }
            item(Material.REDSTONE_LAMP) {
                name("<yellow>Edit Round Settings".asMini())
                description(listOf("Click to edit the settings of this round".asMini()))
                slots(5)
                executes { event ->
                    event.isCancelled = true
                    event.whoClicked.message("Edit Round Settings")
                    openSettings(round, source as Player, wave)
                }
            }
            item(Material.RED_CONCRETE) {
                name("<red>Remove This Round".asMini())
                description(listOf("Click to remove this round".asMini()))
                slots(7)
                executes { event ->
                    event.isCancelled = true
                    WaveModifier().modifyWave(wave, round, Wave::removeRound)
                    source.message("Removed this round.")
                    WaveGui(wave, source)
                }
            }
        }
        gui.addBackButton(13) { source ->
            WaveGui(wave, source)
        }
        gui.open(source as Player)
    }
}