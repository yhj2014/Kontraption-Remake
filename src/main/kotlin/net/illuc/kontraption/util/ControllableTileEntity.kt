package net.illuc.kontraption.util

import mekanism.api.providers.IBlockProvider
import mekanism.common.tile.base.TileEntityMekanism
import net.illuc.kontraption.ship.KontraptionBConfigControl
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.state.BlockState

abstract class ControllableTileEntity(
    blockProvider: IBlockProvider,
    pos: BlockPos?,
    state: BlockState?,
) : TileEntityMekanism(blockProvider, pos, state),
    IControllable {
    final override val controlSettings = mutableMapOf<String, Any>()

    override fun onLoad() {
        if (controlSettings.isEmpty()) {
            registerControlSettings()
        }
        if (level is ServerLevel) {
            registerWithControlSystem()
            val server = (level as ServerLevel).server
            server.execute {
                registerWithControlSystem()
            }
        }
        super.onLoad()
    }

    override fun load(tag: CompoundTag) {
        super.load(tag)
        loadControlSettings(tag)
    }

    override fun saveAdditional(tag: CompoundTag) {
        super.saveAdditional(tag)
        saveControlSettings(tag)
    }

    override fun onUpdateServer() {
        super.onUpdateServer()
    }

    override fun setRemoved() {
        unregisterFromControlSystem()
        super.setRemoved()
    }

    override fun onChunkUnloaded() {
        unregisterFromControlSystem()
        super.onChunkUnloaded()
    }
}
