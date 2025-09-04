package net.illuc.kontraption.util

import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import it.zerono.mods.zerocore.lib.data.geometry.CuboidBoundingBox
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState

typealias BlockStateTest = (BlockState) -> Boolean

@JvmInline
value class Dim3(
    val x: Int,
) {
    val y get() = x
    val z get() = x
} // Fast cube(used for testing prev)

// Flat version of original Multiblock array visualization, doesnt need remade equals or hashCode as its never to be compared
@Suppress("ArrayInDataClass")
data class Shape3D(
    val sx: Int,
    val sy: Int,
    val sz: Int,
    val types: ByteArray,
) {
    fun index(
        x: Int,
        y: Int,
        z: Int,
    ): Int = (y * sz + z) * sx + x
}

fun Shape3D.rotateY(times: Int): Shape3D {
    var result = this
    repeat(times % 4) {
        val newTypes = ByteArray(sx * sy * sz)
        val newSx = sz
        val newSy = sy
        val newSz = sx
        for (y in 0 until sy) {
            for (z in 0 until sz) {
                for (x in 0 until sx) {
                    val newZ = sx - 1 - x
                    val oldIndex = index(x, y, z)
                    val newIndex = (y * newSz + newZ) * newSx + z
                    newTypes[newIndex] = types[oldIndex]
                }
            }
        }
        result = Shape3D(newSx, newSy, newSz, newTypes)
    }
    return result
}

fun Shape3D.rotateX(times: Int): Shape3D {
    var result = this
    repeat(times % 4) {
        val newTypes = ByteArray(sx * sy * sz)
        val newSx = sx
        val newSy = sz
        val newSz = sy
        for (y in 0 until sy) {
            for (z in 0 until sz) {
                for (x in 0 until sx) {
                    val newZ = sy - 1 - y
                    newTypes[(z * newSz + newZ) * newSx + x] = types[index(x, y, z)]
                }
            }
        }
        result = Shape3D(newSx, newSy, newSz, newTypes)
    }
    return result
}

fun Shape3D.rotateZ(times: Int): Shape3D {
    var result = this
    repeat(times % 4) {
        val newTypes = ByteArray(sx * sy * sz)
        val newSx = sy
        val newSy = sx
        val newSz = sz
        for (y in 0 until sy) {
            for (z in 0 until sz) {
                for (x in 0 until sx) {
                    val newY = sx - 1 - x
                    newTypes[(newY * newSz + z) * newSx + y] = types[index(x, y, z)]
                }
            }
        }
        result = Shape3D(newSx, newSy, newSz, newTypes)
    }
    return result
}
// Soo i coulda just join em bc they are basicly the same BUT 1.I dont have a direction checker OR im not sure if dis will work perfectly ingame, in theory(Le test program) it rotates Shape3D well TODO: Figure out fricking rotation AGAIN

data class CordContainer(
    val rx: Int,
    val ry: Int,
    val rz: Int,
)

class StructHelper(
    blockMappings: Map<Byte, List<Block>>,
) {
    private lateinit var shape: Shape3D
    private val table: Array<BlockStateTest> = Array(256) { { false } } // Size should suffice for most multiblocks, if it doesnt rething your life choices

    init {
        for ((id, blocks) in blockMappings) {
            val set = IntOpenHashSet(blocks.size)
            for (blck in blocks) set.add(BuiltInRegistries.BLOCK.getId(blck)) // Fuck dpre
            table[id.toInt() and 0xFF] = { s -> set.contains(BuiltInRegistries.BLOCK.getId(s.block)) } // You can probably yeet sign conversion if we get bigger array but EFFICENCY!!
        }
        // 0 is considered to be air and by default should be ignored
        // Ofc more efficent way for this would be to have Directly user defined Set and not play around with conversion, but its on init soo idc
        table[0] = { s -> s.isAir }
    }

    fun setShape3D(shape3D: Shape3D) {
        this.shape = shape3D
    }

    fun relativeCoords(
        cbb: CuboidBoundingBox,
        pos: BlockPos,
    ): CordContainer {
        val rx = pos.x - cbb.minX
        val ry = pos.y - cbb.minY
        val rz = pos.z - cbb.minZ
        return CordContainer(rx, ry, rz)
    }

    fun isValidBlock(
        world: Level,
        pos: BlockPos,
        cbb: CuboidBoundingBox,
    ): Pair<Boolean, Byte> {
        if (!::shape.isInitialized) return false to 0 // TODO: Figure out why and HOW shape is sometimes not inited
        val rCorrd = relativeCoords(cbb, pos)
        if (rCorrd.rx !in 0 until shape.sx || rCorrd.ry !in 0 until shape.sy || rCorrd.rz !in 0 until shape.sz) return false to 0
        val flat = shape.index(rCorrd.rx, rCorrd.ry, rCorrd.rz)
        val typeId = shape.types[flat].toInt() and 0xFF
        val ok = table[typeId](world.getBlockState(pos))
        return ok to typeId.toByte()
    }
}
