package dev.marten_mrfcyt.mobWaves.waves.gui

import dev.marten_mrfcyt.mobWaves.utils.*
import dev.marten_mrfcyt.mobWaves.utils.gui.Gui
import dev.marten_mrfcyt.mobWaves.waves.Wave
import dev.marten_mrfcyt.mobWaves.waves.WaveModifier
import io.lumine.mythic.api.mobs.MythicMob
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent

fun openBosses(wave: Wave, source: Player, mythicPage: Int = 0, wavePage: Int = 0) {
    val gui = Gui("EditBossGui".asMini(), 9 * 5)
    loadDecoration(gui)
    loadBosses(wave, source, getAllNotWaveBosses(wave), getWaveBosses(wave), gui, mythicPage, wavePage)
    gui.open(source)
}

private fun loadBosses(wave: Wave, source: Player, mythicMobs: List<MythicMob>, waveMobs: List<MythicMob>, gui: Gui, mythicPage: Int, wavePage: Int) {
    val mobsPerPage = 16
    val mythicStartIndex = mythicPage * mobsPerPage
    val waveStartIndex = wavePage * mobsPerPage

    val paginatedMythicMobs = mythicMobs.drop(mythicStartIndex).take(mobsPerPage)
    val paginatedWaveMobs = waveMobs.drop(waveStartIndex).take(mobsPerPage)

    var mythicMobCount = 0
    var waveMobCount = 0

    for (row in 0 until 4) {
        for (i in 0 until 4) {
            if (mythicMobCount < paginatedMythicMobs.size) {
                val mob = paginatedMythicMobs[mythicMobCount++]
                gui.apply {
                    item(Material.WITHER_SKELETON_SKULL) {
                        name(mob.internalName.asMini())
                        description(listOf("Click to add this boss to the wave".asMini()))
                        slots(row * 9 + i)
                        executes { event: InventoryClickEvent ->
                            event.isCancelled = true
                            WaveModifier().modifyWave(wave, mob.internalName, Wave::addBoss)
                            openBosses(wave, source, mythicPage, wavePage)
                        }
                    }
                }
            }
        }
        for (i in 5 until 9) {
            if (waveMobCount < paginatedWaveMobs.size) {
                val waveMob = paginatedWaveMobs[waveMobCount++]
                gui.apply {
                    item(Material.WITHER_SKELETON_SKULL) {
                        name(waveMob.internalName.asMini())
                        description(listOf("Click to remove this boss from the wave".asMini()))
                        slots(row * 9 + i)
                        executes { event: InventoryClickEvent ->
                            event.isCancelled = true
                            WaveModifier().modifyWave(wave, waveMob.internalName, Wave::removeBoss)
                            openBosses(wave, source, mythicPage, wavePage)
                        }
                    }
                }
            }
        }
    }

    loadPagination(gui, mythicPage, wavePage, mythicMobs.size, waveMobs.size, mobsPerPage, wave, source)
}

private fun loadDecoration(gui: Gui) {
    gui.apply {
        item(Material.GRAY_STAINED_GLASS_PANE) {
            name("".asMini())
            slots(4, 13, 22, 31, 40)
            executes { event: InventoryClickEvent -> event.isCancelled = true }
        }
        for(i in 0 until 4) {
            item(Material.RED_STAINED_GLASS_PANE) {
                name("<dark_purple>Click on an egg to <green>add</green> a mob".asMini())
                slots(4 * 9 + i)
                executes { event: InventoryClickEvent -> event.isCancelled = true }
            }
        }
        for(i in 5 until 9) {
            item(Material.GREEN_STAINED_GLASS_PANE) {
                name("<dark_purple>Click on an egg to <red>remove</red> a mob".asMini())
                slots(4 * 9 + i)
                executes { event: InventoryClickEvent -> event.isCancelled = true }
            }
        }
    }
}

private fun loadPagination(gui: Gui, mythicPage: Int, wavePage: Int, totalMythicMobs: Int, totalWaveMobs: Int, mobsPerPage: Int, wave: Wave, source: Player) {
    if (mythicPage > 0) {
        gui.apply {
            item(Material.ARROW) {
                name("<yellow>Previous Mythic Mobs Page".asMini())
                slots(36)
                executes { event: InventoryClickEvent ->
                    event.isCancelled = true
                    openBosses(wave, source, mythicPage - 1, wavePage)
                }
            }
        }
    }
    if ((mythicPage + 1) * mobsPerPage < totalMythicMobs) {
        gui.apply {
            item(Material.ARROW) {
                name("<yellow>Next Mythic Mobs Page".asMini())
                slots(39)
                executes { event: InventoryClickEvent ->
                    event.isCancelled = true
                    openBosses(wave, source, mythicPage + 1, wavePage)
                }
            }
        }
    }
    if (wavePage > 0) {
        gui.apply {
            item(Material.ARROW) {
                name("<yellow>Previous Wave Mobs Page".asMini())
                slots(41)
                executes { event: InventoryClickEvent ->
                    event.isCancelled = true
                    openBosses(wave, source, mythicPage, wavePage - 1)
                }
            }
        }
    }
    if ((wavePage + 1) * mobsPerPage < totalWaveMobs) {
        gui.apply {
            item(Material.ARROW) {
                name("<yellow>Next Wave Mobs Page".asMini())
                slots(44)
                executes { event: InventoryClickEvent ->
                    event.isCancelled = true
                    openBosses(wave, source, mythicPage, wavePage + 1)
                }
            }
        }
    }
}