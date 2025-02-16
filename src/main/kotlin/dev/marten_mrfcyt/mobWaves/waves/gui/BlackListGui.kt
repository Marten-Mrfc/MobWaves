package dev.marten_mrfcyt.mobWaves.waves.gui

import dev.marten_mrfcyt.mobWaves.utils.*
import dev.marten_mrfcyt.mobWaves.utils.external.getAllMythicMobs
import dev.marten_mrfcyt.mobWaves.utils.gui.Gui
import io.lumine.mythic.api.mobs.MythicMob
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent

fun openBlackList(source: Player, mythicPage: Int = 0, blacklistPage: Int = 0) {
    val gui = Gui("BlackListGui".asMini(), 9 * 5)
    loadDecoration(gui)

    val allMobs = getAllMythicMobs()
    val blacklistedMobs = BlackList.getAllBlackListedMobs()
    val availableMobs = allMobs.filter { mob -> !blacklistedMobs.any { it.internalName == mob.internalName } }

    loadMobs(source, availableMobs, blacklistedMobs, gui, mythicPage, blacklistPage)
    gui.open(source)
}

private fun loadMobs(source: Player, mythicMobs: List<MythicMob>, blacklistedMobs: List<MythicMob>, gui: Gui, mythicPage: Int, blacklistPage: Int) {
    val mobsPerPage = 16
    val mythicStartIndex = mythicPage * mobsPerPage
    val blacklistStartIndex = blacklistPage * mobsPerPage

    val paginatedMythicMobs = mythicMobs.drop(mythicStartIndex).take(mobsPerPage)
    val paginatedBlacklistMobs = blacklistedMobs.drop(blacklistStartIndex).take(mobsPerPage)

    var mythicMobCount = 0
    var blacklistMobCount = 0

    for (row in 0 until 4) {
        for (i in 0 until 4) {
            if (mythicMobCount < paginatedMythicMobs.size) {
                val mob = paginatedMythicMobs[mythicMobCount++]
                gui.apply {
                    item(Material.ZOMBIE_HEAD) {
                        name(mob.internalName.asMini())
                        description(listOf("Click to blacklist this mob".asMini()))
                        slots(row * 9 + i)
                        executes { event: InventoryClickEvent ->
                            event.isCancelled = true
                            BlackList.addBlackListedMob(mob)
                            openBlackList(source, mythicPage, blacklistPage)
                        }
                    }
                }
            }
        }
        for (i in 5 until 9) {
            if (blacklistMobCount < paginatedBlacklistMobs.size) {
                val blacklistedMob = paginatedBlacklistMobs[blacklistMobCount++]
                gui.apply {
                    item(Material.ZOMBIE_HEAD) {
                        name(blacklistedMob.internalName.asMini())
                        description(listOf("Click to remove from blacklist".asMini()))
                        slots(row * 9 + i)
                        executes { event: InventoryClickEvent ->
                            event.isCancelled = true
                            BlackList.removeBlackListedMob(blacklistedMob)
                            openBlackList(source, mythicPage, blacklistPage)
                        }
                    }
                }
            }
        }
    }

    loadPagination(gui, mythicPage, blacklistPage, mythicMobs.size, blacklistedMobs.size, mobsPerPage, source)
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
                name("<dark_purple>Click to <green>blacklist</green> a mob".asMini())
                slots(4 * 9 + i)
                executes { event: InventoryClickEvent -> event.isCancelled = true }
            }
        }
        for(i in 5 until 9) {
            item(Material.GREEN_STAINED_GLASS_PANE) {
                name("<dark_purple>Click to <red>remove</red> from blacklist".asMini())
                slots(4 * 9 + i)
                executes { event: InventoryClickEvent -> event.isCancelled = true }
            }
        }
    }
}

private fun loadPagination(gui: Gui, mythicPage: Int, blacklistPage: Int, totalMythicMobs: Int, totalBlacklistMobs: Int, mobsPerPage: Int, source: Player) {
    if (mythicPage > 0) {
        gui.apply {
            item(Material.ARROW) {
                name("<yellow>Previous Available Mobs Page".asMini())
                slots(36)
                executes { event: InventoryClickEvent ->
                    event.isCancelled = true
                    openBlackList(source, mythicPage - 1, blacklistPage)
                }
            }
        }
    }
    if ((mythicPage + 1) * mobsPerPage < totalMythicMobs) {
        gui.apply {
            item(Material.ARROW) {
                name("<yellow>Next Available Mobs Page".asMini())
                slots(39)
                executes { event: InventoryClickEvent ->
                    event.isCancelled = true
                    openBlackList(source, mythicPage + 1, blacklistPage)
                }
            }
        }
    }
    if (blacklistPage > 0) {
        gui.apply {
            item(Material.ARROW) {
                name("<yellow>Previous Blacklisted Mobs Page".asMini())
                slots(41)
                executes { event: InventoryClickEvent ->
                    event.isCancelled = true
                    openBlackList(source, mythicPage, blacklistPage - 1)
                }
            }
        }
    }
    if ((blacklistPage + 1) * mobsPerPage < totalBlacklistMobs) {
        gui.apply {
            item(Material.ARROW) {
                name("<yellow>Next Blacklisted Mobs Page".asMini())
                slots(44)
                executes { event: InventoryClickEvent ->
                    event.isCancelled = true
                    openBlackList(source, mythicPage, blacklistPage + 1)
                }
            }
        }
    }
}