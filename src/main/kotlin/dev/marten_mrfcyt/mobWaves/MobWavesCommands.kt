package dev.marten_mrfcyt.mobWaves

import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import dev.marten_mrfcyt.mobWaves.session.SessionManager
import dev.marten_mrfcyt.mobWaves.utils.SafeChecker
import dev.marten_mrfcyt.mobWaves.waves.Wave
import dev.marten_mrfcyt.mobWaves.waves.WaveModifier
import dev.marten_mrfcyt.mobWaves.waves.gui.WaveGui
import dev.marten_mrfcyt.mobWaves.waves.gui.openBlackList
import dev.marten_mrfcyt.mobWaves.waves.handler.WaveManager
import dev.marten_mrfcyt.mobWaves.zones.xp.XPZoneManager
import mlib.api.commands.builders.LiteralDSLBuilder
import mlib.api.commands.builders.command
import mlib.api.utilities.error
import mlib.api.utilities.message
import mlib.api.utilities.sendMini
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
                    source.error("Failed to create a wave")
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
                    val wave = WaveModifier().listWaves().first { it.name == name }

                    if (playerArg == "@a") {
                        Bukkit.getOnlinePlayers().forEach { player ->
                            startWaveForPlayer(player, wave, name)
                        }
                    } else {
                        val player = Bukkit.getPlayer(playerArg) ?: source as Player
                        startWaveForPlayer(player, wave, name)
                    }
                }
            }
            executes {
                val player = source as Player
                val name = getArgument<String>("name")
                val wave = WaveModifier().listWaves().first { it.name == name }
                startWaveForPlayer(player, wave, name)
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
                        stopWaveForPlayer(player, source as Player)
                    }
                } else {
                    val player = Bukkit.getPlayer(playerArg) ?: source as Player
                    stopWaveForPlayer(player, source as Player)
                }
            }
        }
        executes {
            val player = source as Player
            stopWaveForPlayer(player, player)
        }
    }
    literal("blacklist") {
        executes { openBlackList(source as Player, 0, 0) }
    }

    literal("listallsessions") {
        executes {
            val sessions = SessionManager.getActiveSessions()
            if (sessions.isEmpty()) {
                source.message("No active sessions found.")
                return@executes
            }

            source.message("<gold>Active sessions (${sessions.size}):")
            sessions.forEach { session ->
                source.sendMini("""
                  <gray>-   <yellow>Player: <white>${session.player.name}
                  <yellow>Wave: <white>${session.currentWave?.name ?: "none"}
                  <yellow>Round: <white>${session.currentRound}
                  <yellow>Total XP Gained: <white>${session.totalXPGained}
                  <yellow>Max XP Total: <white>${session.maxXPTotal}
                  <yellow>XP Zone: <white>${session.currentXPZone ?: "none"}
                  <yellow>XP Zone XP: <white>${session.xpPerZone[session.currentXPZone] ?: 0}
                  <yellow>Max Zone XP: <white>${XPZoneManager.getMaxXP(session.player.location)?.times(session.currentRound) ?: 0}
                  <yellow>XP Accumulator: <white>${session.xpAccumulator}
            """.trimIndent())
            }
        }
    }
}

private fun startWaveForPlayer(player: Player, wave: Wave, waveName: String) {
    player.message("Starting a wave")
    player.message("Wave: $waveName")
    WaveManager.addActiveWave(player, wave)
}

private fun stopWaveForPlayer(player: Player, source: Player) {
    source.message("Stopping the wave")
    WaveManager.removeActiveWave(player)
    player.message("Wave stopped")
}

fun Plugin.zoneCommands() = command("zone") {
    setupZones()
}

private fun LiteralDSLBuilder.setupZones() {
    literal("show") {
        argument("radius", IntegerArgumentType.integer()) {
            argument("showred", BoolArgumentType.bool()) {
                executes {
                    val source = source as Player
                    SafeChecker(source.location.world).show(source, getArgument<Int>("radius"), getArgument<Boolean>("showred"))
                }
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