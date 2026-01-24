package net.illuc.kontraption.blockEntities
import dan200.computercraft.shared.Capabilities
import mekanism.common.tile.base.TileEntityMekanism
import net.illuc.kontraption.Kontraption
import net.illuc.kontraption.KontraptionBlocks
import net.illuc.kontraption.peripherals.ConnectorPeripheral
import net.illuc.kontraption.util.OttUtils
import net.illuc.kontraption.util.OttUtils.fV3V3d
import net.illuc.kontraption.util.OttUtils.fV3dV3
import net.illuc.kontraption.util.gtpa
import net.illuc.kontraption.util.toJOMLD
import net.illuc.kontraption.util.vsutils.ConnectorControllers
import net.illuc.kontraption.util.vsutils.makePose
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.fml.ModList
import org.joml.Quaterniond
import org.joml.Vector3d
import org.valkyrienskies.core.api.ships.ServerShip
import org.valkyrienskies.core.api.ships.properties.ShipId
import org.valkyrienskies.core.api.util.GameTickOnly
import org.valkyrienskies.core.api.util.PhysTickOnly
import org.valkyrienskies.core.internal.joints.*
import org.valkyrienskies.mod.common.getLoadedShipManagingPos

// Comment for CEO, im not even throwing that BAD model i did
class TileEntityConnector(
    pos: BlockPos?,
    state: BlockState?,
) : TileEntityMekanism(KontraptionBlocks.CONNECTOR, pos, state) {
    private val peripheral = ConnectorPeripheral(this)
    private val peripheralCapability = LazyOptional.of { peripheral }

    // TODO: ADD THIS TO GODFORSAKEN CONFIG LIST(SHIP CONFIG)
    var isConnected = false
    var connectedTo: BlockPos? = null
    var underCC = false

    private var cachedTarget: BlockPos? = null
    private var cacheCooldown = 0

    private val sLevel: ServerLevel?
        get() = level as? ServerLevel

    @OptIn(GameTickOnly::class)
    private val ship: ServerShip?
        get() = (level as? ServerLevel)?.getLoadedShipManagingPos(blockPos)

    override fun <T> getCapability(
        capability: Capability<T>,
        side: Direction?,
    ): LazyOptional<T> =
        if (ModList.get().isLoaded("computercraft") && capability == Capabilities.CAPABILITY_PERIPHERAL as Capability<T>) {
            peripheralCapability as LazyOptional<T>
        } else {
            super.getCapability(capability, side)
        }

    fun enable() {
        // as ceo intended
    }

    override fun onLoad() {
        super.onLoad()
        val level = sLevel ?: return
        if (!level.isClientSide && isConnected && connectedTo != null) {
            ConnectorControllers.get(level)
                .connect(blockPos, connectedTo!!)
        }
    }

    private fun rayTrace(distance: Double): BlockEntity? {
        // Maybe change to static distance here anyway
        val serverLevel = sLevel ?: return null
        val state = serverLevel.getBlockState(blockPos)
        val facing = state.getValue(BlockStateProperties.FACING)

        var startVec = Vec3.atCenterOf(blockPos).add(Vec3.atLowerCornerOf(facing.normal).scale(0.7))
        var endVec = startVec.add(Vec3(facing.stepX.toDouble(), facing.stepY.toDouble(), facing.stepZ.toDouble()).scale(distance))

        ship?.let {
            startVec = fV3dV3(it.transform.shipToWorld.transformPosition(fV3V3d(startVec)))
            endVec = fV3dV3(it.transform.shipToWorld.transformPosition(fV3V3d(endVec)))
        }

        val result = serverLevel.clip(ClipContext(startVec, endVec, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, null))
        if (result.type == HitResult.Type.BLOCK) {
            Kontraption.LOGGER.debug("Hit Block")
            return serverLevel.getBlockEntity(result.blockPos)?.takeIf { it is TileEntityConnector } // HUH it is neat to use kotlin functions . . . okay
        }

        return null
    }
    private fun findTarget(): TileEntityConnector? {
        if (cacheCooldown-- > 0) {
            return cachedTarget?.let { level?.getBlockEntity(it) as? TileEntityConnector }
        }
        cacheCooldown = 10
        val hit = rayTrace(2.5) as? TileEntityConnector
        cachedTarget = hit?.blockPos
        return hit
    }

    override fun saveAdditional(tag: CompoundTag) {
        super.saveAdditional(tag)
        tag.putBoolean("isConnected", isConnected)
        tag.putBoolean("underCC", underCC)//underCC exist purerly cuz im a moron and cant figure out how to switch between redstone and cc mode
        connectedTo?.let { tag.putLong("connectedTo", it.asLong()) }
    }

    override fun load(tag: CompoundTag) {
        super.load(tag)
        isConnected = tag.getBoolean("isConnected")
        underCC = tag.getBoolean("underCC")
        connectedTo =
            if (tag.contains("connectedTo")) BlockPos.of(tag.getLong("connectedTo"))
            else null
    }

    fun connect() {
        if (level !is ServerLevel || isConnected) return
        val other = findTarget() ?: return

        val controller = ConnectorControllers.get(level as ServerLevel)
        controller.connect(blockPos, other.blockPos)

        isConnected = true
        connectedTo = other.blockPos
        setChanged()
    }



    fun disconnect() {
        if (level !is ServerLevel) return
        val controller = ConnectorControllers.get(level as ServerLevel)
        controller.disconnect(blockPos)//TECHNICALLY YOU CAN UH JUST BREAK THE BLOCK TO FUCK UP CONNECTION, but fortunly Controller will disallow illegal connections sooo rejoin?


        isConnected = false
        connectedTo = null
        setChanged()
    }


    fun tick() {
        if (!underCC && !isConnected && canConnect()) {
            connect()
        }

        if (isConnected && !canConnect()) {
            disconnect()
        }
    }


    fun canConnect(): Boolean =
        level?.hasNeighborSignal(blockPos) == true



}
