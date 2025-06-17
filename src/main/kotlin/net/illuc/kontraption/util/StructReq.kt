package net.illuc.kontraption.util

import it.zerono.mods.zerocore.lib.data.geometry.CuboidBoundingBox
import net.minecraft.core.BlockPos
import net.minecraft.core.Vec3i
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block

class StructReq(
    private val blockMappings: Map<Byte, List<Block>>,
) {
    /**
     * Checks if the block at a given position satisfies the structure requirement for this Array of Layers.
     * IF YOU ARE USING AIRC YOU MUST DEFINE 0 BYTE IN THE BLOCKMAP
     * @param world The world in which the multiblock is located(CANNOT BE RELATIVE)
     * @param pos The position of the block
     * @param airc If 0 should by default be Air
     * @param rotY Rotates the cuboid abstract Y by passed ammout of degrees
     * @param rotX Rotates the cuboid abstract X by passed ammout of degrees
     * @param rotZ Rotates the cuboid abstract Z by passed ammout of degrees
     * @return True if the block at the position meets the requirement, false otherwise
     */
    fun isValidBlock(
        world: Level,
        pos: BlockPos,
        airc: Boolean,
        cbb: CuboidBoundingBox,
        // (0, 90, 180, 270)
        rotY: Int = 0,
        rotX: Int = 0,
        rotZ: Int = 0,
    ): Pair<Boolean, Byte> {
        val allowedLayers: Array<Array<ByteArray>> = OttUtils.generateAllowedLayers(cbb.lengthX, cbb.lengthY, cbb.lengthZ)

        if (cbb.lengthY != allowedLayers.size) return Pair(false, 0.toByte())

        val relativePos = pos.subtract(Vec3i(cbb.minX, cbb.minY, cbb.minZ))
        var (h, v, layer) = Triple(relativePos.x, relativePos.z, relativePos.y)
        when (rotY) {
            90 -> Triple(cbb.lengthZ - 1 - v, h, layer)
            180 -> Triple(cbb.lengthX - 1 - h, cbb.lengthZ - 1 - v, layer)
            270 -> Triple(v, cbb.lengthX - 1 - h, layer)
            else -> Triple(h, v, layer)
        }.also {
            h = it.first
            v = it.second
            layer = it.third
        }
        when (rotX) {
            90 -> Triple(h, cbb.lengthY - 1 - layer, v)
            180 -> Triple(h, cbb.lengthY - 1 - layer, cbb.lengthZ - 1 - v)
            270 -> Triple(h, layer, cbb.lengthZ - 1 - v)
            else -> Triple(h, layer, v)
        }.also {
            h = it.first
            v = it.third
            layer = it.second
        }
        when (rotZ) {
            90 -> Triple(cbb.lengthY - 1 - layer, h, v)
            180 -> Triple(cbb.lengthX - 1 - h, cbb.lengthY - 1 - layer, v)
            270 -> Triple(layer, cbb.lengthX - 1 - h, v)
            else -> Triple(layer, h, v)
        }.also {
            h = it.first
            v = it.third
            layer = it.second
        }
        if (layer !in allowedLayers.indices || h !in allowedLayers[0].indices || v !in allowedLayers[0][0].indices) {
            return Pair(false, 0.toByte())
        }

        val requiredType = allowedLayers[layer][h][v]
        val validBlocks = blockMappings[requiredType] ?: return Pair(false, 0.toByte())
        val blockAtPosition = world.getBlockState(pos)

        return if (airc) {
            if (requiredType == 0.toByte()) {
                Pair(blockAtPosition.isAir, requiredType)
            } else {
                Pair(validBlocks.contains(blockAtPosition.block), requiredType)
            }
        } else {
            Pair(validBlocks.contains(blockAtPosition.block), requiredType)
        }
    }
}
