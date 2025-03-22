package dev.marten_mrfcyt.mobWaves.waves.gui

import dev.marten_mrfcyt.mobWaves.utils.external.getAllNotBlackListMobs
import dev.marten_mrfcyt.mobWaves.utils.external.getWaveMobs
import dev.marten_mrfcyt.mobWaves.waves.Wave
import dev.marten_mrfcyt.mobWaves.waves.WaveModifier
import dev.marten_mrfcyt.mobWaves.waves.WaveRound
import mlib.api.utilities.*
import io.lumine.mythic.api.mobs.MythicMob
import mlib.api.gui.Gui
import mlib.api.gui.GuiSize
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent

fun open(round: WaveRound, source: Player, mythicPage: Int = 0, wavePage: Int = 0, wave: Wave) {
    val gui = Gui("EditMobGui".asMini(), GuiSize.ROW_FIVE)
    loadDecoration(gui)
    loadMobs(round, source, getAllNotBlackListMobs(round), getWaveMobs(round), gui, mythicPage, wavePage, wave)
    gui.addBackButton(40) { source ->
        WaveRoundGui(wave, round, source)
    }
    gui.open(source)
}

private fun loadMobs(round: WaveRound, source: Player, mythicMobs: List<MythicMob>, waveMobs: HashMap<MythicMob, Int>, gui: Gui, mythicPage: Int, wavePage: Int, wave: Wave) {
    val mobsPerPage = 16
    val waveStartIndex = wavePage * mobsPerPage
    val mythicStartIndex = mythicPage * mobsPerPage
    val paginatedWaveMobs = waveMobs.keys.sortedBy { it.internalName }.drop(waveStartIndex).take(mobsPerPage)
    var waveMobCount = 0
    val paginatedMythicMobs = mythicMobs.sortedBy { it.internalName }.drop(mythicStartIndex).take(mobsPerPage)
    var mythicMobCount = 0

    for (row in 0 until 4) {
        for (i in 0 until 4) {
            if (mythicMobCount < paginatedMythicMobs.size) {
                val mob = paginatedMythicMobs[mythicMobCount++]
                gui.apply {
                    item(Material.SKELETON_SPAWN_EGG) {
                        name(mob.internalName.asMini())
                        description(listOf("Click to add this mob to the wave".asMini()))
                        slots(row * 9 + i)
                        onClick { event: InventoryClickEvent ->
                            event.isCancelled = true
                            WaveModifier().modifyWaveRound(round, mob.internalName) { addMob(it, 1) }
                            open(round, source, mythicPage, wavePage, wave)
                        }
                    }
                }
            }
        }
        for (i in 5 until 9) {
            if (waveMobCount < paginatedWaveMobs.size) {
                val waveMob = paginatedWaveMobs[waveMobCount++]
                gui.apply {
                    item(Material.SKELETON_SPAWN_EGG) {
                        name(waveMob.internalName.asMini())
                        description(listOf("Click to remove this mob from the wave".asMini()))
                        slots(row * 9 + i)
                        amount(waveMobs[waveMob]!!)
                        onClick { event: InventoryClickEvent ->
                            event.isCancelled = true
                            WaveModifier().modifyWaveRound(round, waveMob.internalName) { removeMob(it, 1) }
                            open(round, source, mythicPage, wavePage, wave)
                        }
                    }
                }
            }
        }
    }

    loadPagination(gui, mythicPage, wavePage, mythicMobs.size, waveMobs.size, mobsPerPage, round, source, wave)
}

private fun loadDecoration(gui: Gui) {
    gui.apply {
        item(Material.GRAY_STAINED_GLASS_PANE) {
            name("".asMini())
            slots(4, 13, 22, 31, 40)
            onClick { event: InventoryClickEvent -> event.isCancelled = true }
        }
        for(i in 0 until 4) {
            item(Material.RED_STAINED_GLASS_PANE) {
                name("<dark_purple>Click on an egg to <green>add</green> a mob".asMini())
                slots(4 * 9 + i)
                onClick { event: InventoryClickEvent -> event.isCancelled = true }
            }
        }
        for(i in 5 until 9) {
            item(Material.GREEN_STAINED_GLASS_PANE) {
                name("<dark_purple>Click on an egg to <red>remove</red> a mob".asMini())
                slots(4 * 9 + i)
                onClick { event: InventoryClickEvent -> event.isCancelled = true }
            }
        }
    }
}

private fun loadPagination(gui: Gui, mythicPage: Int, wavePage: Int, totalMythicMobs: Int, totalWaveMobs: Int, mobsPerPage: Int, round: WaveRound, source: Player, wave: Wave) {
    if (mythicPage > 0) {
        gui.apply {
            item(Material.ARROW) {
                name("<yellow>Previous Mythic Mobs Page".asMini())
                slots(36)
                onClick { event: InventoryClickEvent ->
                    event.isCancelled = true
                    open(round, source, mythicPage - 1, wavePage, wave)
                }
            }
        }
    }
    if ((mythicPage + 1) * mobsPerPage < totalMythicMobs) {
        gui.apply {
            item(Material.ARROW) {
                name("<yellow>Next Mythic Mobs Page".asMini())
                slots(39)
                onClick { event: InventoryClickEvent ->
                    event.isCancelled = true
                    open(round, source, mythicPage + 1, wavePage, wave)
                }
            }
        }
    }
    if (wavePage > 0) {
        gui.apply {
            item(Material.ARROW) {
                name("<yellow>Previous Wave Mobs Page".asMini())
                slots(41)
                onClick { event: InventoryClickEvent ->
                    event.isCancelled = true
                    open(round, source, mythicPage, wavePage - 1, wave)
                }
            }
        }
    }
    if ((wavePage + 1) * mobsPerPage < totalWaveMobs) {
        gui.apply {
            item(Material.ARROW) {
                name("<yellow>Next Wave Mobs Page".asMini())
                slots(44)
                onClick { event: InventoryClickEvent ->
                    event.isCancelled = true
                    open(round, source, mythicPage, wavePage + 1, wave)
                }
            }
        }
    }
}