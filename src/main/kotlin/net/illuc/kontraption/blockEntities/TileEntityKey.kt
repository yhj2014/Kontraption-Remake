package net.illuc.kontraption.blockEntities

import mekanism.common.Mekanism
import mekanism.common.tile.base.TileEntityMekanism
import net.illuc.kontraption.Kontraption
import net.illuc.kontraption.KontraptionBlocks
import net.illuc.kontraption.blocks.BlockKey
import net.illuc.kontraption.events.KeyBindEvent
import net.illuc.kontraption.ship.KontraptionBConfigControl
import net.illuc.kontraption.ship.KontraptionKeyBlockControl
import net.illuc.kontraption.util.ControllableTileEntity
import net.illuc.kontraption.util.KontraptionVSUtils
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.state.BlockState

class TileEntityKey(
    pos: BlockPos?,
    state: BlockState?,
) : ControllableTileEntity(KontraptionBlocks.KEY, pos, state) {
    override val controlDefaults =
        mapOf(
            "enabled" to false,
            "somenumbrr" to (0 until 120421).random(),
            "keybind_numner" to -1,
            "name" to "Redstone Interface",
        )
    override val controlDefaultsNonSync: Map<String, KontraptionBConfigControl.BlockSetting.IntSetting> =
        mapOf(
            "somenumbrr" to intSettingMeta("somenumbrr", 120422, 0, false),
        )
    val isEnabled: Boolean get() = controlSettings["enabled"] as Boolean
    val number: Int get() = controlSettings["somenumbrr"] as Int
    var keybindz: Int = controlSettings["keybind_numner"] as? Int ?: -1
        get() = (controlSettings["keybind_numner"] as? Int) ?: field
        set(value) {
            controlSettings["keybind_numner"] = value
        }
    val thrusterName: String get() = controlSettings["name"] as String

    fun tick() {
    }

    override fun getUpdateTag(): CompoundTag {
        val tag = super.getUpdateTag()
        saveAdditional(tag)
        return tag
    }

    fun isRedstoneActive(): Boolean = isEnabled

    fun setRedstone(
        state: Boolean,
    ) {
        if (level == null) {
            Mekanism.logger.info("somehow level is null xd")
            return
        }
        val blockstate = level?.getBlockState(this.blockPos)
        if (blockstate?.block is BlockKey) {
            (blockstate.block as BlockKey).setRedstone(state, level!!, this.blockPos)
        }
    }

    fun getsetKeybind(
        op: Boolean,
        kb: Int = 0,
    ): Int {
        if (op == true) {
            keybindz = kb
            setChanged()
            return 0
        } else {
            return keybindz
        }
    }

    fun fire(event: KeyBindEvent) {
        val eventkey = event.keybindIndex
        if (eventkey == this.keybindz) {
            setRedstone(event.updown)
        }
    }

    fun disable() {
    }

    fun enable() { // Totally not stolen from gyro xd
    }
    // I AM AN OASIS OF CALM.
    // A FUCKING INCREDIBLY PEACEFUL LOTUS FLOWER FLOATING ON THE PERFECTLY CALM SURFACE OF A FUCKING LAKE.
    // I’M BASICALLY LIKE A WHOLE FUCKING WAGON FULL OF MEDITATING TIBETAN MONKS.

    fun shipAdd() {
        if (level !is ServerLevel) return
        if (worldPosition == null) return
        val ship =
            KontraptionVSUtils.getShipObjectManagingPos((level as ServerLevel), worldPosition)
                ?: KontraptionVSUtils.getShipManagingPos((level as ServerLevel), worldPosition)
                ?: return
        KontraptionKeyBlockControl.getOrCreate(ship).let {
            it.removeAll(worldPosition)
            it.addKeys(
                worldPosition,
                0,
            )
        }
    }

    override fun onLoad() {
        if (level is ServerLevel) {
            shipAdd()
        }
        super.onLoad()
    }
}
