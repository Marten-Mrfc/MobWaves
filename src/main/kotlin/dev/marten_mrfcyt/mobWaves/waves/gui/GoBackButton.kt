package dev.marten_mrfcyt.mobWaves.waves.gui

import mlib.api.utilities.*
import mlib.api.gui.Gui
import org.bukkit.Material
import org.bukkit.entity.Player

fun Gui.addBackButton(slot: Int, onBack: (Player) -> Unit) {
    item(Material.BARRIER) {
        name("<red>Go Back".asMini())
        description(listOf("<gray>Click to return to previous menu".asMini()))
        slots(slot)
        onClick { event ->
            event.isCancelled = true
            onBack(event.whoClicked as Player)
        }
    }
}