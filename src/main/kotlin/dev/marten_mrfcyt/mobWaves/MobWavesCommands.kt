package dev.marten_mrfcyt.mobWaves

import com.mojang.brigadier.arguments.StringArgumentType
import dev.marten_mrfcyt.mobWaves.utils.message
import dev.marten_mrfcyt.mobWaves.waves.WaveModifier
import dev.marten_mrfcyt.mobWaves.waves.gui.WaveGui
import dev.marten_mrfcyt.mobWaves.waves.handler.WaveManager.addActiveWave
import lirand.api.dsl.command.builders.LiteralDSLBuilder
import lirand.api.dsl.command.builders.command
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

fun Plugin.mobWavesCommands() = command("wave") {
    setup()
}

private fun LiteralDSLBuilder.setup() {
    literal("create") {
        argument("name", StringArgumentType.string()) {
            executes {
                source.message("Creating a wave")
                val name = getArgument<String>("name")
                val success = WaveModifier().createWave(name)
                if (success) {
                    source.message("Wave created: $name")
                } else {
                    source.message("Failed to create a wave")
                }
            }
        }
    }
    literal("delete") {
        argument("name", StringArgumentType.greedyString()) {
            suggests { builder ->
                WaveModifier().listWaves().map { it.name }.forEach {
                    builder.suggest(it)
                }
                builder.buildFuture()
            }
            executes {
                val name = getArgument<String>("name")
                source.message("Deleting a wave")
                WaveModifier().deleteWave(name)
                source.message("Wave deleted: $name")
            }
        }
    }
    literal("editor") {
        argument("name", StringArgumentType.greedyString()) {
            suggests { builder ->
                WaveModifier().listWaves().map { it.name }.forEach {
                    builder.suggest(it)
                }
                builder.buildFuture()
            }
            executes {
                val name = getArgument<String>("name")
                source.message("Opening the wave editor")
                val wave = WaveModifier().listWaves().first { it.name == name }
                WaveGui(wave, source)
            }
        }
    }
    literal("list") {
        executes {
            val waves = WaveModifier().listWaves()
            waves.forEach {
                source.message("- ${it.name}")
            }
        }
    }
    literal("start") {
        argument("name", StringArgumentType.greedyString()) {
            suggests { builder ->
                WaveModifier().listWaves().map { it.name }.forEach {
                    builder.suggest(it)
                }
                builder.buildFuture()
            }
            executes {
                val name = getArgument<String>("name")
                source.message("Starting a wave")
                source.message("Wave: $name")
                addActiveWave(source as Player, WaveModifier().listWaves().first { it.name == name })
            }
        }
    }
}
