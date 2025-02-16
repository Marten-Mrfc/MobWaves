package dev.marten_mrfcyt.mobWaves

import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.sun.jdi.connect.Connector
import dev.marten_mrfcyt.mobWaves.utils.SafeChecker
import dev.marten_mrfcyt.mobWaves.utils.message
import dev.marten_mrfcyt.mobWaves.waves.WaveModifier
import dev.marten_mrfcyt.mobWaves.waves.gui.WaveGui
import dev.marten_mrfcyt.mobWaves.waves.gui.openBlackList
import dev.marten_mrfcyt.mobWaves.waves.handler.WaveManager.addActiveWave
import dev.marten_mrfcyt.mobWaves.waves.handler.WaveManager.removeActiveWave
import dev.marten_mrfcyt.mobWaves.zones.ZoneManager
import dev.marten_mrfcyt.mobWaves.zones.ZoneUtil
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import lirand.api.dsl.command.builders.LiteralDSLBuilder
import lirand.api.dsl.command.builders.command
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

fun Plugin.mobWavesCommands() = command("wave") {
    setupWaves()
}

private fun LiteralDSLBuilder.setupWaves() {
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
        argument("name", StringArgumentType.string()) {
            suggests { builder ->
                WaveModifier().listWaves().map { it.name }.forEach {
                    builder.suggest(it)
                }
                builder.buildFuture()
            }
            argument("player", StringArgumentType.greedyString()) {
                suggests { builder ->
                    Bukkit.getOnlinePlayers().forEach {
                        builder.suggest(it.name)
                    }
                    builder.suggest("@a")
                    builder.buildFuture()
                }
                executes {
                    val playerArg = getArgument<String>("player")
                    val name = getArgument<String>("name")
                    if (playerArg == "@a") {
                        Bukkit.getOnlinePlayers().forEach { player ->
                            player.message("Starting a wave")
                            player.message("Wave: $name")
                            addActiveWave(player, WaveModifier().listWaves().first { it.name == name })
                        }
                    } else {
                        val player = Bukkit.getPlayer(playerArg) ?: source as Player
                        player.message("Starting a wave")
                        player.message("Wave: $name")
                        addActiveWave(player, WaveModifier().listWaves().first { it.name == name })
                    }
                }
            }
            executes {
                val player = source as Player
                val name = getArgument<String>("name")
                player.message("Starting a wave")
                player.message("Wave: $name")
                addActiveWave(player, WaveModifier().listWaves().first { it.name == name })
            }
        }
    }
    literal("stop") {
        argument("player", StringArgumentType.greedyString()) {
            suggests { builder ->
                Bukkit.getOnlinePlayers().forEach {
                    builder.suggest(it.name)
                }
                builder.suggest("@a")
                builder.buildFuture()
            }
            executes {
                val playerArg = getArgument<String>("player")
                if (playerArg == "@a") {
                    Bukkit.getOnlinePlayers().forEach { player ->
                        source.message("Stopping the wave")
                        removeActiveWave(player)
                        player.message("Wave stopped")
                    }
                } else {
                    val player = Bukkit.getPlayer(playerArg) ?: source as Player
                    source.message("Stopping the wave")
                    removeActiveWave(player)
                    player.message("Wave stopped")
                }
            }
        }
        executes {
            val player = source as Player
            source.message("Stopping the wave")
            removeActiveWave(player)
            player.message("Wave stopped")
        }
    }
    literal("blacklist") {
        executes { openBlackList(source as Player, 0, 0) }
    }
}

fun Plugin.zoneCommands() = command("zone") {
    setupZones()
}

private fun LiteralDSLBuilder.setupZones() {
    literal("show") {
        argument("showred", BoolArgumentType.bool()) {
            executes {
                val source = source as Player
                SafeChecker(source.location.world).show(source, getArgument<Boolean>("showred"))
            }
        }
    }
    literal("hide") {
        executes {
            val source = source as Player
            SafeChecker(source.location.world).hide()
        }
    }
}