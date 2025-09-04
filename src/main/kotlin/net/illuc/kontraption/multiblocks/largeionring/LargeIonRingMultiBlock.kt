package net.illuc.kontraption.multiblocks.largeionring

import it.zerono.mods.zerocore.lib.IActivableMachine
import it.zerono.mods.zerocore.lib.energy.EnergyBuffer
import it.zerono.mods.zerocore.lib.energy.EnergySystem
import it.zerono.mods.zerocore.lib.energy.IWideEnergyStorage
import it.zerono.mods.zerocore.lib.energy.handler.WideEnergyStoragePolicyWrapper
import it.zerono.mods.zerocore.lib.multiblock.IMultiblockController
import it.zerono.mods.zerocore.lib.multiblock.IMultiblockPart
import it.zerono.mods.zerocore.lib.multiblock.cuboid.AbstractCuboidMultiblockController
import it.zerono.mods.zerocore.lib.multiblock.validation.IMultiblockValidator
import net.illuc.kontraption.GlobalRegistry
import net.illuc.kontraption.ThrusterInterface
import net.illuc.kontraption.config.KontraptionConfigs
import net.illuc.kontraption.multiblocks.largeionring.parts.AbstractRingEntity
import net.illuc.kontraption.util.*
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.phys.Vec3
import net.minecraftforge.api.distmarker.Dist
import org.joml.Vector3d
import org.valkyrienskies.core.api.ships.Ship
import java.util.function.Consumer

// TODO: IMPORTANT, THIS IS WAYY TO HARD CODED, YEET IT INTO BUNCH OF SUPERCLASSES SKELIES
open class LargeIonRingMultiBlock(
    world: Level,
) : AbstractCuboidMultiblockController<LargeIonRingMultiBlock>(world),
    ThrusterInterface,
    IActivableMachine {
    // vars
    var exhaustDirection: Direction = Direction.DOWN
    var centerExhaust: BlockPos = this.boundingBox.center
    var exhaustDiameter = 0
    var offset: Vec3 = Vec3(0.0, 0.0, 0.0)
    var center: BlockPos = BlockPos(0, 0, 0)
    var shipGrabpos: BlockPos = BlockPos(0, 0, 0)
    var innerVolume = 1
    var particleDir = exhaustDirection.normal.multiply(3 + exhaustDiameter).toJOMLD()
    var pos = centerExhaust.offset(exhaustDirection.normal.multiply(2))
    var ship: Ship? = null
    val sizeX = boundingBox.maxX - boundingBox.minX
    val sizeZ = boundingBox.maxZ - boundingBox.minZ
    val areaBIG = (sizeX * sizeZ) * 2
    val areaSMALL = ((sizeX - 2) * (sizeZ - 2)) * 2
    var controller: AbstractRingEntity? = null

    // ---- THRUSTER BS ------
    override var enabled = true
    override var thrusterLevel: Level? = world
    override var worldPosition: BlockPos? = center
    override var forceDirection: Direction = exhaustDirection.opposite
    override var powered: Boolean = true
    override var thrusterPower: Double = 100.0
    override val basePower: Double = 100.0
    override var currentThrust: Double = 0.0
    // ----------------

    // energy settings
    private val ENERGY_CAPACITY: Double = 10000000.0

    // Burn-related crap
    var burnRemaining = 0.0
    var lastBurnRate = 0.0

    private val energyStorage = EnergyBuffer(EnergySystem.ForgeEnergy, ENERGY_CAPACITY)
    val energyInputHandler = WideEnergyStoragePolicyWrapper.inputOnly(this.energyStorage)

    val blockMappings: Map<Byte, List<Block>> =
        mapOf(
            0.toByte() to listOf(GlobalRegistry.Blocks.OTTER_PLUSHIE.get()),
            1.toByte() to listOf(GlobalRegistry.Blocks.LARGE_ION_THRUSTER_CASING.get()),
            2.toByte() to
                listOf(
                    GlobalRegistry.Blocks.LARGE_ION_THRUSTER_CASING.get(),
                    GlobalRegistry.Blocks.LARGE_ION_THRUSTER_VALVE.get(),
                    GlobalRegistry.Blocks.LARGE_ION_THRUSTER_CONTROLLER.get(),
                ),
            3.toByte() to listOf(GlobalRegistry.Blocks.LARGE_ION_THRUSTER_CASING.get()),
            4.toByte() to listOf(GlobalRegistry.Blocks.LARGE_ION_THRUSTER_CASING.get()),
            5.toByte() to listOf(GlobalRegistry.Blocks.LARGE_ION_THRUSTER_COIL.get()),
        )

    val structureHelper = StructHelper(blockMappings)

    // nukes internall stuff
    fun reset() {
        this.isMachineActive = false
        this.energyStorage.energyStored = 0.0
    }

    override fun isMachineActive(): Boolean = enabled

    override fun setMachineActive(active: Boolean) {
        if (this.isMachineActive() == active) {
            return // why tf did i use exact here?
        }

        if (active) {
            this.connectedParts.forEach(Consumer { obj: IMultiblockPart<LargeIonRingMultiBlock?> -> obj.onMachineActivated() })
        } else {
            this.connectedParts.forEach(Consumer { obj: IMultiblockPart<LargeIonRingMultiBlock?> -> obj.onMachineDeactivated() })
        }

        this.callOnLogicalServer(Runnable { this.markReferenceCoordForUpdate() })
    }

    fun getEnergyStorage(): IWideEnergyStorage = this.energyInputHandler

    fun addToShip() {
        ship = KontraptionVSUtils.getShipObjectManagingPos((thrusterLevel as ServerLevel), centerExhaust)
            ?: KontraptionVSUtils.getShipManagingPos((thrusterLevel as ServerLevel), centerExhaust)
        if (ship != null) {
            controller?.let { enable(thrusterLevel as ServerLevel, it.position) } // I would prefer to just rerun assembly tho soo thats a thing to look into
        }
    }

    override fun onPartAdded(p0: IMultiblockPart<LargeIonRingMultiBlock>) {
        // println("added part to ION RING")
        this.callOnLogicalServer(
            kotlinx.coroutines.Runnable {
                val fastPos = BlockPos(boundingBox.minX, boundingBox.minY, boundingBox.minZ)
                // structureRequirement.previewAllowedBlockOutlines(fastPos, thrusterLevel as ServerLevel)
            },
        )
    }

    override fun onPartRemoved(p0: IMultiblockPart<LargeIonRingMultiBlock>) {
        // println("removed part from ION RING")
        this.callOnLogicalServer(
            kotlinx.coroutines.Runnable {
                val fastPos = BlockPos(boundingBox.minX, boundingBox.minY, boundingBox.minZ)
                // structureRequirement.previewAllowedBlockOutlines(fastPos, thrusterLevel as ServerLevel)
            },
        )
    }

    override fun onMachineRestored() {
        // println("restored ION RING")
    }

    override fun onMachinePaused() {
        // println("paused ION RING")
    }

    override fun onMachineDisassembled() {
        // println("disassembled ION RING")
        this.callOnLogicalServer(
            kotlinx.coroutines.Runnable {
                val fastPos = BlockPos(boundingBox.minX, boundingBox.minY, boundingBox.minZ)
                // structureRequirement.previewAllowedBlockOutlines(fastPos, thrusterLevel as ServerLevel)
            },
        )
    }

    override fun getMinimumNumberOfPartsForAssembledMachine(): Int {
        if (sizeX == sizeZ) {
            return areaBIG - areaSMALL
        }
        return 100000
    }

    override fun getMaximumXSize() = 15

    override fun getMaximumZSize() = 15

    override fun getMaximumYSize() = 2

    override fun updateServer(): Boolean {
        if (powered) {
            burnFuel()
        } else {
            lastBurnRate = 0.0
        }

        if (powered && enabled) {
            if (Dist.DEDICATED_SERVER.isDedicatedServer && thrusterLevel != null) {
                particleDir =
                    if (ship == null) {
                        exhaustDirection.normal.multiply(innerVolume).toJOMLD()
                    } else {
                        ship!!.transform.shipToWorld.transformDirection(exhaustDirection.normal.multiply(innerVolume).toJOMLD())
                    }
            }
        }
        if (controller != null) controller?.setEnabledd(enabled && powered)
        return true
    }

    override fun updateClient() {
        // Client-side logic
    }

    override fun isBlockGoodForFrame(
        p0: Level,
        p1: Int,
        p2: Int,
        p3: Int,
        p4: IMultiblockValidator,
    ): Boolean {
        return false // Add your logic here
    }

    override fun isBlockGoodForTop(
        p0: Level,
        p1: Int,
        p2: Int,
        p3: Int,
        p4: IMultiblockValidator,
    ): Boolean = false

    override fun isBlockGoodForBottom(
        p0: Level,
        p1: Int,
        p2: Int,
        p3: Int,
        p4: IMultiblockValidator,
    ): Boolean = false

    override fun isBlockGoodForSides(
        p0: Level,
        p1: Int,
        p2: Int,
        p3: Int,
        p4: IMultiblockValidator,
    ): Boolean = false

    override fun isBlockGoodForInterior(
        world: Level,
        x: Int,
        y: Int,
        z: Int,
        validatorCallback: IMultiblockValidator,
    ): Boolean = false

    override fun onAssimilated(p0: IMultiblockController<LargeIonRingMultiBlock>) {
        //  println("WARNING ASSIMILATED")
    }

    override fun onAssimilate(p0: IMultiblockController<LargeIonRingMultiBlock>) {
        // println("WARNING ASSIMILATING")
    }

    override fun onMachineAssembled() {
        super.onMachineAssembled()
        this.callOnLogicalServer(this::serverMachineAssembly)
    }

    fun serverMachineAssembly() {
        // println("ASSEMBLING")
        center = this.pos
        centerExhaust = this.boundingBox.center

        ship = KontraptionVSUtils.getShipObjectManagingPos((thrusterLevel as ServerLevel), centerExhaust)
            ?: KontraptionVSUtils.getShipManagingPos((thrusterLevel as ServerLevel), centerExhaust)

        offset =
            Vector3d(1.0, 1.0, 1.0)
                .add(
                    exhaustDirection.normal
                        .toJOMLD()
                        .normalize()
                        .negate(),
                ).mul(0.25 * exhaustDiameter)
                .add(
                    exhaustDirection.normal
                        .toJOMLD()
                        .mul(1.5),
                ).toMinecraft()
        pos = centerExhaust.offset(exhaustDirection.normal.multiply(1))
        thrusterPower = (innerVolume * KontraptionConfigs.kontraption.largeIonThrust.get())
        if (ship != null) {
            thrusterLevel = world
            if (controller != null) {
                worldPosition = controller!!.position
            }
            forceDirection = exhaustDirection.opposite
            controller?.let { enable(thrusterLevel as ServerLevel, it.position) }
        }
    }

    private fun burnFuel() {
        if (currentThrust <= 0.0) return
        val pwrPrc = currentThrust / thrusterPower
        val toBurn = KontraptionConfigs.kontraption.largeIonEnergyConsumption.get() * pwrPrc

        if (energyStorage.energyStored >= toBurn) {
            energyStorage.extractEnergy(EnergySystem.ForgeEnergy, toBurn, false)
            if (!enabled) enable()
        } else {
            if (enabled) disable()
        }

        burnRemaining = energyStorage.energyStored % 1
        lastBurnRate = toBurn
    }

    override fun isMachineWhole(validatorCallback: IMultiblockValidator): Boolean {
        if (boundingBox.lengthX !in 5..15 ||
            boundingBox.lengthZ !in 5..15 ||
            boundingBox.lengthY != 2 || boundingBox.lengthZ != boundingBox.lengthX
        ) {
            validatorCallback.setLastError("Invalid Size of the multiblock")
            return false
        }

        var hasController = false

        val hollowVolume = (boundingBox.lengthX - 4) * (boundingBox.lengthZ - 4) * boundingBox.lengthY
        val expectedPartsCount = boundingBox.lengthX * boundingBox.lengthZ * boundingBox.lengthY - hollowVolume
        if (expectedPartsCount != this.partsCount) {
            validatorCallback.setLastError("Invalid block amount for the multiblock")
            return false
        }
        val shape: Shape3D = ShapeGenerators.largeIonRing(boundingBox.lengthX, boundingBox.lengthY, boundingBox.lengthZ)
        structureHelper.setShape3D(shape)
        val min = BlockPos(boundingBox.minX, boundingBox.minY, boundingBox.minZ)

        val sy = boundingBox.lengthY
        val sz = boundingBox.lengthZ
        val sx = boundingBox.lengthX

        for (y in 0 until sy) {
            for (z in 0 until sz) {
                for (x in 0 until sx) {
                    val pos = min.offset(x, y, z)
                    val (ok, type) = structureHelper.isValidBlock(world, pos, boundingBox)
                    if (!ok) {
                        validatorCallback.setLastError(pos, "Invalid block type at $pos (expected $type)")
                        return false
                    }
                    val blockEntity = world.getBlockEntity(pos) as? AbstractRingEntity
                    if (blockEntity?.type == GlobalRegistry.TileEntities.LARGE_ION_THRUSTER_CONTROLLER.get()) {
                        centerExhaust = this.boundingBox.center
                        val offset = blockEntity.worldPosition.subtract(centerExhaust)
                        blockEntity.setRNTags(offset, boundingBox.lengthX)
                        controller = blockEntity
                        innerVolume = (boundingBox.lengthX - 4) * (boundingBox.lengthX - 4) * 2
                        blockEntity.isController = true
                        hasController = true
                    }
                    when (type) {
                        3.toByte() -> blockEntity?.isTop = true
                        4.toByte() -> {
                            blockEntity?.isTop = true
                            blockEntity?.isCorner = true
                        }
                        else -> blockEntity?.isTop = false
                    }
                }
            }
        }
        return hasController
    }
}
