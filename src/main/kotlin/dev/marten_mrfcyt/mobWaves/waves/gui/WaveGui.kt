package dev.marten_mrfcyt.mobWaves.waves.gui

import dev.marten_mrfcyt.mobWaves.waves.Wave
import dev.marten_mrfcyt.mobWaves.waves.WaveModifier
import dev.marten_mrfcyt.mobWaves.waves.WaveRound
import mlib.api.gui.Gui
import mlib.api.gui.GuiSize
import mlib.api.utilities.*
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class WaveGui(private val wave: Wave, private val source: CommandSender) {
    init {
        val gui = Gui("WaveGui".asMini(), GuiSize.ROW_TWO).apply {
            item(Material.DIAMOND_SWORD) {
                name("Wave: ${wave.name}".asMini())
                slots(13)
            }
            wave.rounds.forEachIndexed { index, round ->
                item(Material.PAPER) {
                    name("<yellow>Round ${index + 1}".asMini())
                    description(listOf("Click to edit this round".asMini()))
                    slots(index)
                    onClick { event ->
                        event.isCancelled = true
                        event.whoClicked.message("Edit Round ${index + 1}")
                        WaveRoundGui(wave, round, source)
                    }
                }
            }
            item(Material.LIME_CONCRETE) {
                name("<green>Add Round".asMini())
                description(listOf("Click to add a new round".asMini()))
                slots(17)
                onClick { event ->
                    event.isCancelled = true
                    WaveModifier().modifyWave(wave, WaveRound(), Wave::addRound)
                    source.message("Added new round to wave: ${wave.name}")
                    WaveGui(wave, source)
                }
            }
        }
        gui.open(source as Player)
    }
}