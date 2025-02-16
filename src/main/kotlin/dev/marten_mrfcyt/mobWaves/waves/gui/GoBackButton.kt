package dev.marten_mrfcyt.mobWaves.waves.gui

import dev.marten_mrfcyt.mobWaves.utils.asMini
import dev.marten_mrfcyt.mobWaves.utils.gui.Gui
import org.bukkit.Material
import org.bukkit.entity.Player

fun Gui.addBackButton(slot: Int, onBack: (Player) -> Unit) {
    item(Material.BARRIER) {
        name("<red>Go Back".asMini())
        description(listOf("<gray>Click to return to previous menu".asMini()))
        slots(slot)
        executes { event ->
            event.isCancelled = true
            onBack(event.whoClicked as Player)
        }
    }
}