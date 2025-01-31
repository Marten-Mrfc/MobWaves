package dev.marten_mrfcyt.mobWaves.utils.gui

import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import java.util.WeakHashMap

object GuiItemProcessor {
    private val inventoryClickHandlers = WeakHashMap<Inventory, MutableMap<Int, (InventoryClickEvent) -> Unit>>()

    fun registerClickHandler(inventory: Inventory, slot: Int, item: GuiItem, onClick: (InventoryClickEvent) -> Unit) {
        val clickHandlers = inventoryClickHandlers.getOrPut(inventory) { mutableMapOf() }
        clickHandlers[slot] = onClick
    }

    fun handleClick(event: InventoryClickEvent) {
        val inventory = event.clickedInventory ?: return
        val slot = event.slot
        val clickHandlers = inventoryClickHandlers[inventory]
        val handler = clickHandlers?.get(slot)
        handler?.invoke(event)
    }
}