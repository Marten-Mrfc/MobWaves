package dev.marten_mrfcyt.mobWaves.waves.gui

import dev.marten_mrfcyt.mobWaves.utils.asMini
import dev.marten_mrfcyt.mobWaves.utils.gui.Gui
import dev.marten_mrfcyt.mobWaves.waves.Wave
import dev.marten_mrfcyt.mobWaves.waves.WaveModifier
import dev.marten_mrfcyt.mobWaves.waves.WaveRound
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.ClickType

fun openSettings(round: WaveRound, source: Player, wave: Wave) {
    val gui = Gui("EditSettingsGui".asMini(), 9 * 3)
    loadSettings(round, source, gui, wave)
    gui.addBackButton(22) { source ->
        WaveRoundGui(wave, round, source)
    }
    gui.open(source)
}

private fun loadSettings(round: WaveRound, source: Player, gui: Gui, wave: Wave) {
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
                openSettings(round, source, wave)
            }
        }
    }

    addItem(
        Material.CONDUIT,
        "<yellow>Radius: ${round.radius}",
        listOf("Right-click to increase", "Left-click to decrease"),
        10
    ) { event ->
        val newRadius = if (event.click == ClickType.RIGHT) round.radius + 1 else round.radius - 1
        WaveModifier().modifyWaveRound(round, newRadius) { radius = it }
    }

    addItem(
        Material.CLOCK,
        "<yellow>Wave Delay: ${round.waveDelay}",
        listOf("Right-click to increase", "Left-click to decrease"),
        13
    ) { event ->
        val newWaveDelay = if (event.click == ClickType.RIGHT) round.waveDelay + 1 else round.waveDelay - 1
        WaveModifier().modifyWaveRound(round, newWaveDelay) { waveDelay = it }
    }

    addItem(
        Material.BARRIER,
        "<yellow>Leave Radius: ${round.leaveRadius}",
        listOf("Right-click to increase", "Left-click to decrease"),
        16
    ) { event ->
        val newLeaveRadius = if (event.click == ClickType.RIGHT) round.leaveRadius + 1 else round.leaveRadius - 1
        WaveModifier().modifyWaveRound(round, newLeaveRadius) { leaveRadius = it }
    }
}