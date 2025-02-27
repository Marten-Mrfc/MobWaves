package dev.marten_mrfcyt.mobWaves.utils

import dev.marten_mrfcyt.mobWaves.MobWaves
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Player
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Vector3f
import org.bukkit.scheduler.BukkitRunnable
private val displayBlocks = mutableListOf<BlockDisplay>()
class SafeChecker(private val world: World) {

    fun isSafe(loc: Location): Boolean {
        if (!world.getBlockAt(loc.blockX, loc.blockY - 1, loc.blockZ).type.isSolid) {
            return false
        }

        for (y in 0..1) {
            if (world.getBlockAt(loc.blockX, loc.blockY + y, loc.blockZ).type.isSolid) {
                return false
            }
        }

        return world.getBlockAt(loc.blockX, loc.blockY - 1, loc.blockZ).type.isSolid &&
                world.getBlockAt(loc.blockX, loc.blockY - 1, loc.blockZ).type.isOccluding
    }

    fun show(player: Player, radius: Int = 10, showRed: Boolean = false) {
        hide()
        val playerLoc = player.location.clone()

        object : BukkitRunnable() {
            override fun run() {
                // Calculate positions async
                val positions = mutableListOf<Pair<Location, Material>>()

                for (x in -radius..radius) {
                    for (y in -3..3) {
                        for (z in -radius..radius) {
                            val loc = Location(world,
                                playerLoc.blockX + x.toDouble(),
                                playerLoc.blockY + y.toDouble(),
                                playerLoc.blockZ + z.toDouble()
                            )

                            val block = if (isSafe(loc)) Material.LIME_CONCRETE
                            else if (showRed) Material.RED_CONCRETE
                            else continue

                            positions.add(loc to block)
                        }
                    }
                }

                // Spawn entities sync
                object : BukkitRunnable() {
                    override fun run() {
                        positions.forEach { (loc, block) ->
                            val blockDisplay = world.spawn(loc, BlockDisplay::class.java) { entity ->
                                entity.block = block.createBlockData()
                                entity.transformation = Transformation(
                                    Vector3f(),
                                    AxisAngle4f(),
                                    Vector3f(0.25f, 0.25f, 0.25f),
                                    AxisAngle4f()
                                )
                                entity.isVisibleByDefault = false
                            }
                            player.showEntity(MobWaves.instance, blockDisplay)
                            displayBlocks.add(blockDisplay)
                        }
                    }
                }.runTask(MobWaves.instance)
            }
        }.runTaskAsynchronously(MobWaves.instance)
    }

    fun hide() {
        displayBlocks.forEach {
            it.location.getNearbyEntities(0.1, 0.1, 0.1).forEach { entity ->
                if (entity is BlockDisplay) {
                    entity.remove()
                }
            }
        }
        displayBlocks.clear()
    }
}