package dev.marten_mrfcyt.mobWaves.utils

import dev.marten_mrfcyt.mobWaves.MobWaves
import gg.flyte.twilight.scheduler.async
import gg.flyte.twilight.scheduler.sync
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Player
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Vector3f

val safeLocations = mutableListOf<Location>()
val displayBlocks = mutableListOf<BlockDisplay>()

class SafeChecker(private val world: World) {

    fun isSafe(loc: Location): Boolean {
        val roundedLoc = Location(loc.world, loc.blockX.toDouble(), loc.blockY.toDouble(), loc.blockZ.toDouble())
        if (!safeLocations.contains(roundedLoc)) {
            safeLocations.add(roundedLoc)
        }

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

    fun show(player: Player, showRed: Boolean = false) {
        try {
            async {
                safeLocations.forEach { location ->
                    val block = if (isSafe(location)) Material.LIME_CONCRETE else if (showRed) Material.RED_CONCRETE else return@forEach
                    sync {
                        val blockDisplay = world.spawn(location, BlockDisplay::class.java) { entity ->
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
            }
        } catch (e: UninitializedPropertyAccessException) {
            MobWaves.instance.logger.warning("Twilight plugin not initialized properly")
        }
    }

    fun hide() {
        displayBlocks.forEach {
            it.remove()
            it.location.getNearbyEntities(0.0, 0.0, 0.0).forEach { entity ->
                if (entity is BlockDisplay) {
                    entity.remove()
                }
            }
        }
        displayBlocks.clear()
        safeLocations.clear()
    }
}