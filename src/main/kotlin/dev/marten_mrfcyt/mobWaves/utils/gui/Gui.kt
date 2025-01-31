package dev.marten_mrfcyt.mobWaves.utils.gui

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class Gui(private val title: Component, internal val size: Int) {
    private val items = mutableListOf<GuiItem>()

    fun item(material: Material, init: GuiItem.() -> Unit): GuiItem {
        val item = GuiItem(material).apply(init)
        items.add(item)
        return item
    }

    fun open(player: Player) {
        val inventory = org.bukkit.Bukkit.createInventory(null, size, title)
        items.forEach { it.addToInventory(inventory) }
        player.openInventory(inventory)
    }
}

class GuiItem(private val material: Material) {
    private var displayName: Component = Component.text("")
    private var description: List<Component> = listOf()
    private var slots: IntArray = intArrayOf()
    private var onClick: (InventoryClickEvent) -> Unit = {}

    fun name(name: Component) {
        this.displayName = name
    }

    fun description(description: List<Component>) {
        this.description = description
    }

    fun slots(vararg slots: Int) {
        this.slots = slots
    }

    fun executes(onClick: (InventoryClickEvent) -> Unit) {
        this.onClick = onClick
    }

    fun addToInventory(inventory: Inventory) {
        val itemStack = ItemStack(material)
        val itemMeta: ItemMeta = itemStack.itemMeta
        itemMeta.displayName(displayName)
        itemMeta.lore(description)
        itemStack.itemMeta = itemMeta

        slots.forEach { slot ->
            inventory.setItem(slot, itemStack)
            GuiItemProcessor.registerClickHandler(inventory, slot, this, onClick)
        }
    }
}