package net.illuc.kontraption.blockEntities

import mekanism.api.Action
import mekanism.api.AutomationType
import mekanism.api.IContentsListener
import mekanism.api.RelativeSide
import mekanism.api.math.FloatingLong
import mekanism.common.capabilities.energy.MachineEnergyContainer
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder
import mekanism.common.util.MekanismUtils
import net.illuc.kontraption.Kontraption
import net.illuc.kontraption.KontraptionBlocks
import net.illuc.kontraption.ThrusterInterface
import net.illuc.kontraption.config.KontraptionConfigs
import net.illuc.kontraption.ship.KontraptionBConfigControl
import net.illuc.kontraption.util.ControllableTileEntity
import net.illuc.kontraption.util.KontraptionVSUtils
import net.illuc.kontraption.util.toJOML
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import javax.annotation.Nonnull

class TileEntityIonThruster(
    pos: BlockPos?,
    state: BlockState?,
) : ControllableTileEntity(KontraptionBlocks.ION_THRUSTER, pos, state),
    ThrusterInterface {
    // TODO: TOUCH THIS MF
    override var enabled = false
    override var thrusterLevel: Level? = null
    override val worldPosition: BlockPos? = pos
    override val forceDirection: Direction = getDirection().opposite
    override var powered: Boolean = true
    override val thrusterPower: Double = KontraptionConfigs.kontraption.ionThrust.get()
    override val basePower: Double = KontraptionConfigs.kontraption.ionThrust.get()
    override var currentThrust: Double = 0.0

    private var clientEnergyUsed = FloatingLong.ZERO

    private var energyContainer: MachineEnergyContainer<TileEntityIonThruster>? = null

    override val controlDefaults =
        mapOf(
            "enabled" to true,
            "somenumbrr" to (0 until 10).random(),
            "name" to "Ion Thruster",
        )
    override val controlDefaultsNonSync =
        mapOf(
            "somenumbrr" to intSettingMeta("somenumbrr", 10, 0, false),
        )
    val isEnabled: Boolean get() = controlSettings["enabled"] as Boolean
    val number: Int get() = controlSettings["somenumbrr"] as Int
    val thrusterName: String get() = controlSettings["name"] as String

    @Nonnull
    override fun getInitialEnergyContainers(listener: IContentsListener?): IEnergyContainerHolder? {
        val builder = EnergyContainerHelper.forSide { this.direction }
        builder.addContainer(MachineEnergyContainer.input(this, listener).also { energyContainer = it }, RelativeSide.BACK)
        return builder.build()
    }

    override fun onUpdateServer() {
        super.onUpdateServer()
        thrusterLevel = level as ServerLevel
        var toUse = FloatingLong.ZERO
        if (MekanismUtils.canFunction(this)) {
            if (powered) { // WHY ILLUC YA USE IF BOOLEAN IS TRUE WTFF
                if (currentThrust != 0.0) {
                    val thrustPercent = currentThrust / thrusterPower
                    val pwrUsage = energyContainer!!.energyPerTick.multiply(thrustPercent)
                    toUse = energyContainer!!.extract(pwrUsage, Action.SIMULATE, AutomationType.INTERNAL) // Uh so we always are sure that we have poweer
                    if (!toUse.isZero) {
                        energyContainer!!.extract(toUse, Action.EXECUTE, AutomationType.INTERNAL)
                        enabled = true
                    } else {
                        enabled = false
                    }
                } else {
                    val maybeUsage = energyContainer!!.energyPerTick
                    toUse = energyContainer!!.extract(maybeUsage, Action.SIMULATE, AutomationType.INTERNAL)
                    enabled = !toUse.isZero
                }
            }
        }
        active = !toUse.isZero
        clientEnergyUsed = toUse
    }

    override fun onLoad() {
        if (level is ServerLevel) {
            enable(level as ServerLevel, blockPos)
        }
        super.onLoad()
    }
}
