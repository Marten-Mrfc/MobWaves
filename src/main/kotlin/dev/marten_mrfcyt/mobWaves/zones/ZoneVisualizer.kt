package dev.marten_mrfcyt.mobWaves.zones

import com.sk89q.worldguard.protection.flags.StateFlag
import dev.marten_mrfcyt.mobWaves.MobWaves
import dev.marten_mrfcyt.mobWaves.utils.external.WorldGuardUtil
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import kotlin.math.sqrt

object ZoneVisualizer {
    private val visualizationTasks = mutableMapOf<World, BukkitTask>()
    private val zonePoints = mutableMapOf<World, List<List<Location>>>()

    private const val VISUALIZATION_INTERVAL = 40L
    private const val VIEW_DISTANCE = 20.0
    private const val LINE_DENSITY = 0.5
    private const val PARTICLE_HEIGHT_OFFSET = 1.0

    private val logger = MobWaves.instance.logger

    fun visualizeZones(plugin: Plugin, world: World) {
        logger.info("Starting zone visualization for world: ${world.name}")
        stopVisualization(world)

        plugin.server.scheduler.runTaskAsynchronously(plugin, Runnable {
            val outlines = calculateZoneOutlines(world)

            plugin.server.scheduler.runTask(plugin, Runnable {
                zonePoints[world] = outlines
                startVisualization(plugin, world)
                logger.info("Visualization started for world: ${world.name} with ${outlines.size} regions")
            })
        })
    }

    private fun calculateZoneOutlines(world: World): List<List<Location>> {
        val regionManager = ZoneManager.getRegionManager(world) ?: return emptyList()

        val outlines = mutableListOf<List<Location>>()
        var mobwaveRegions = 0

        for ((_, region) in regionManager.regions) {
            if (region.getFlag(WorldGuardUtil.MOBWAVE_FLAG) == StateFlag.State.ALLOW) {
                mobwaveRegions++

                val regionPoints = region.points.mapNotNull { point ->
                    val x = point.x().toDouble()
                    val z = point.z().toDouble()

                    // Find highest non-air block
                    var yPos = region.maximumPoint.y().toDouble()
                    val minY = region.minimumPoint.y().toDouble()

                    while (yPos >= minY) {
                        val block = Location(world, x, yPos, z).block
                        if (!block.type.isAir && !block.type.name.contains("LEAVES")) {
                            return@mapNotNull Location(world, x, yPos + PARTICLE_HEIGHT_OFFSET, z)
                        }
                        yPos--
                    }

                    null
                }

                if (regionPoints.isNotEmpty()) {
                    outlines.add(regionPoints)
                }
            }
        }

        logger.info("Found ${outlines.size} region outlines from $mobwaveRegions mobwave regions in ${world.name}")
        return outlines
    }

    private fun startVisualization(plugin: Plugin, world: World) {
        val task = object : BukkitRunnable() {
            override fun run() {
                val regionOutlines = zonePoints[world] ?: return

                if (world.players.isEmpty() || regionOutlines.isEmpty()) {
                    return
                }

                for (player in world.players) {
                    displayOutlinesForPlayer(player, regionOutlines)
                }
            }
        }.runTaskTimer(plugin, 0L, VISUALIZATION_INTERVAL)

        visualizationTasks[world] = task
    }

    private fun displayOutlinesForPlayer(player: Player, regionOutlines: List<List<Location>>) {
        val playerLoc = player.location
        val viewDistanceSquared = VIEW_DISTANCE * VIEW_DISTANCE

        for (outline in regionOutlines) {
            if (outline.size < 2) continue

            for (i in outline.indices) {
                val start = outline[i]
                val end = outline[(i + 1) % outline.size]

                // Skip if both endpoints are too far
                if (playerLoc.distanceSquared(start) > viewDistanceSquared &&
                    playerLoc.distanceSquared(end) > viewDistanceSquared) {
                    continue
                }

                // Draw line between points
                createLineBetweenPoints(start, end, player.world).forEach { point ->
                    player.world.spawnParticle(Particle.FLAME, point, 1, 0.0, 0.0, 0.0, 0.0)
                }
            }
        }
    }

    private fun createLineBetweenPoints(start: Location, end: Location, world: World): List<Location> {
        val points = mutableListOf<Location>()

        val dx = end.x - start.x
        val dz = end.z - start.z
        val distance = sqrt(dx * dx + dz * dz)
        val count = (distance / LINE_DENSITY).toInt().coerceAtLeast(1)

        for (i in 0..count) {
            val t = i.toDouble() / count
            val x = start.x + dx * t
            val z = start.z + dz * t

            findSuitableYPosition(world, x, z)?.let { y ->
                points.add(Location(world, x + 0.5, y, z + 0.5))
            }
        }

        return points
    }

    private fun findSuitableYPosition(world: World, x: Double, z: Double): Double? {
        for (y in 320 downTo -64) {
            val loc = Location(world, x, y.toDouble(), z)
            val blockType = loc.block.type

            if (!blockType.isAir && !blockType.name.contains("LEAVES")) {
                val blockAbove = Location(world, x, y + 1.0, z).block
                if (blockAbove.type.isAir) {
                    return y + PARTICLE_HEIGHT_OFFSET
                }
            }
        }
        return null
    }

    fun updateZonePoints(plugin: Plugin, world: World) {
        if (visualizationTasks.containsKey(world)) {
            visualizeZones(plugin, world)
        } else {
            logger.warning("Cannot update zones - no active visualization for world: ${world.name}")
        }
    }

    fun stopVisualization(world: World) {
        visualizationTasks[world]?.cancel()
        visualizationTasks.remove(world)
        zonePoints.remove(world)
        logger.info("Visualization stopped for world: ${world.name}")
    }

    fun stopAllVisualizations() {
        logger.info("Stopping all visualizations across ${visualizationTasks.size} worlds")
        visualizationTasks.values.forEach { it.cancel() }
        visualizationTasks.clear()
        zonePoints.clear()
    }
}