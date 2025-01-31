package dev.marten_mrfcyt.mobWaves.waves.gui

import dev.marten_mrfcyt.mobWaves.utils.asMini
import dev.marten_mrfcyt.mobWaves.utils.gui.Gui
import dev.marten_mrfcyt.mobWaves.waves.Wave
import dev.marten_mrfcyt.mobWaves.waves.WaveModifier
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.ClickType

fun openSettings(wave: Wave, source: Player) {
    val gui = Gui("EditSettingsGui".asMini(), 9 * 3)
    loadSettings(wave, source, gui)
    gui.open(source)
}

private fun loadSettings(wave: Wave, source: Player, gui: Gui) {
    fun addItem(
        material: Material,
        name: String,
        description: List<String>,
        slot: Int,
        onClick: (InventoryClickEvent) -> Unit
    ) {
        gui.item(material) {
            name(name.asMini())
            description(description.map { it.asMini() })
            slots(slot)
            executes { event: InventoryClickEvent ->
                event.isCancelled = true
                onClick(event)
                openSettings(wave, source)
            }
        }
    }

    addItem(
        Material.CONDUIT,
        "<yellow>Radius: ${wave.radius}",
        listOf("Right-click to increase", "Left-click to decrease"),
        10
    ) { event ->
        val newRadius = if (event.click == ClickType.RIGHT) wave.radius + 1 else wave.radius - 1
        WaveModifier().modifyWave(wave, newRadius, Wave::changeRadius)
    }

    addItem(
        Material.SHULKER_BOX,
        "<yellow>Wave Amount: ${wave.waveAmount}",
        listOf("Right-click to increase", "Left-click to decrease"),
        12
    ) { event ->
        val newWaveAmount = if (event.click == ClickType.RIGHT) wave.waveAmount + 1 else wave.waveAmount - 1
        WaveModifier().modifyWave(wave, newWaveAmount, Wave::changeWaveAmount)
    }

    addItem(
        Material.CLOCK,
        "<yellow>Wave Delay: ${wave.waveDelay}",
        listOf("Right-click to increase", "Left-click to decrease"),
        14
    ) { event ->
        val newWaveDelay = if (event.click == ClickType.RIGHT) wave.waveDelay + 1 else wave.waveDelay - 1
        WaveModifier().modifyWave(wave, newWaveDelay, Wave::changeWaveDelay)
    }

    addItem(
        Material.BARRIER,
        "<yellow>Leave Radius: ${wave.leaveRadius}",
        listOf("Right-click to increase", "Left-click to decrease"),
        16
    ) { event ->
        val newLeaveRadius = if (event.click == ClickType.RIGHT) wave.leaveRadius + 1 else wave.leaveRadius - 1
        WaveModifier().modifyWave(wave, newLeaveRadius, Wave::changeLeaveRadius)
    }
}