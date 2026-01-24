package net.illuc.kontraption.blockEntities

import mekanism.common.tile.base.TileEntityMekanism
import net.illuc.kontraption.KontraptionBlocks
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.block.state.BlockState
import org.valkyrienskies.core.internal.joints.VSJointId

class TileEntityServo(pos: BlockPos?, state: BlockState?) : TileEntityMekanism(KontraptionBlocks.SERVO, pos, state) {
    private var shipID: Long = -1
    var hingeId: VSJointId? = null
    var attachmentConstraintId: VSJointId? = null
    var otherPos: BlockPos? = null
    var shipIds: List<Long>? = null

    override fun load(tag: CompoundTag) {
        super.load(tag)

        /*if (tag.contains("Kontraption\$shipId")) {
            shipID = tag.getLong("Kontraption\$shipId")
        }
        if (activated)
            if (shipID == -1L)
                angle = angleBefore
            else
                shipID = -1L


        shipIds = tag.getLongArray("shipIds").asList()*/
    }
}
