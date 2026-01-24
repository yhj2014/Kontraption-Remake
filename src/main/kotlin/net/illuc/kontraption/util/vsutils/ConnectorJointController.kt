package net.illuc.kontraption.util.vsutils

import net.illuc.kontraption.util.gtpa
import net.illuc.kontraption.util.toJOMLD
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import org.joml.Quaterniond
import org.joml.Vector3d
import org.valkyrienskies.core.api.ships.ServerShip
import org.valkyrienskies.core.api.util.GameTickOnly
import org.valkyrienskies.core.api.util.PhysTickOnly
import org.valkyrienskies.core.internal.joints.VSFixedJoint
import org.valkyrienskies.core.internal.joints.VSJointPose
import org.valkyrienskies.mod.common.dimensionId
import org.valkyrienskies.mod.common.getLoadedShipManagingPos
import org.valkyrienskies.mod.common.shipObjectWorld

object ConnectorControllers {
    private val controllers = mutableMapOf<ServerLevel, ConnectorJointController>()//I hope we wont have cross dimensional connections, would be akward

    fun get(level: ServerLevel): ConnectorJointController =
        controllers.getOrPut(level) { ConnectorJointController(level) }
    fun tick(level: ServerLevel) {
        controllers[level]?.tick()
    }

    fun unload(level: ServerLevel) {
        controllers.remove(level)
    }
}


class ConnectorJointController(
    private val level: ServerLevel
) {
    data class Link(
        val a: BlockPos,
        val b: BlockPos,
        var jointId: Int = -1
    )
    private val links = mutableMapOf<BlockPos, Link>()

    fun connect(a: BlockPos, b: BlockPos) {
        if (links.containsKey(a) || links.containsKey(b)) return
        val link = Link(a, b)
        links[a] = link
        links[b] = link
    }

    fun disconnect(pos: BlockPos) {
        val link = links[pos] ?: return
        removeJoint(link)
        links.remove(link.a)
        links.remove(link.b)
    }
    fun isConnected(pos: BlockPos): Boolean =
        links.containsKey(pos)

    fun getOther(pos: BlockPos): BlockPos? =
        links[pos]?.let { if (it.a == pos) it.b else it.a }//i mean, dwe have to keep dis on tile then?

    @OptIn(PhysTickOnly::class)
    fun tick() {
        for (link in links.values.distinct()) {
            if (link.jointId == -1) {
                tryCreateJoint(link)
            } else {
                validate(link)
            }
        }
    }


    @OptIn(GameTickOnly::class)
    private fun validate(link: Link) {
        val shipA = level.getLoadedShipManagingPos(link.a)
        val shipB = level.getLoadedShipManagingPos(link.b)

        // if its same ship then yeet, it may or may not also remove missing? eh to test
        if (shipA?.id == shipB?.id) {
            removeJoint(link)
            return
        }
    }
    @OptIn(GameTickOnly::class)
    private fun removeJoint(link: Link) {
        if (link.jointId != -1) {
            level.gtpa.removeJoint(link.jointId)
            link.jointId = -1
        }
    }
    @OptIn(GameTickOnly::class, PhysTickOnly::class)
    private fun tryCreateJoint(link: Link) {
        val shipA = level.getLoadedShipManagingPos(link.a)
        val shipB = level.getLoadedShipManagingPos(link.b)

        val idA = shipA?.id
        val idB = shipB?.id
        if (idA == idB) return

        val stateA = level.getBlockState(link.a)
        val stateB = level.getBlockState(link.b)

        val facingA = stateA.getValue(BlockStateProperties.FACING).normal.toJOMLD()
        val facingB = stateB.getValue(BlockStateProperties.FACING).normal.toJOMLD()
        val poseA = shipA.makePose( link.a,facingA)
        val poseB = shipB.makePose( link.b,facingB)

        val joint = VSFixedJoint(
            idA, poseA,
            idB, poseB,
            compliance = 1e-100
        )

        level.gtpa.addJoint(joint) { id ->
            if (id != -1) {
                link.jointId = id
            }
        }
    }

}


@OptIn(PhysTickOnly::class)
fun ServerShip?.makePose(pos: BlockPos, facing: Vector3d? = null, offset: Double = 0.5): VSJointPose {
    val worldAnchor = Vector3d( pos.x + offset, pos.y + offset, pos.z + offset).fma(offset, facing, Vector3d())
    return VSJointPose(worldAnchor, Quaterniond())//yes i know it doesnt use ship BUT this should only be used in ship env anywayy
}

