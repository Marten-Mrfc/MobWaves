package dev.marten_mrfcyt.mobWaves.utils.gui

import dev.marten_mrfcyt.mobWaves.MobWaves
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

class InventoryClickListener(private val plugin: MobWaves): Listener {

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        GuiItemProcessor.handleClick(event)
    }
}