package net.illuc.kontraption.util

import mekanism.common.tile.base.TileEntityMekanism
import net.illuc.kontraption.Kontraption
import net.illuc.kontraption.ship.KontraptionBConfigControl
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.entity.BlockEntity

interface IControllable {
    val controlSettings: MutableMap<String, Any>
    val controlDefaults: Map<String, Any>
    val controlDefaultsNonSync: Map<String, KontraptionBConfigControl.BlockSetting.IntSetting>
        get() = emptyMap()

    fun registerControlSettings() {
        controlDefaults.forEach { (key, value) ->
            controlSettings.putIfAbsent(key, value)
        }
    }

    fun intSettingMeta(
        name: String,
        max: Int,
        min: Int,
        scaled: Boolean,
    ) = KontraptionBConfigControl.BlockSetting.IntSetting(name, 0, max, min, scaled) // DETERMINATION

    fun saveControlSettings(tag: CompoundTag) {
        val settingsTag = CompoundTag()
        controlSettings.forEach { (key, value) ->
            when (value) {
                is Boolean -> settingsTag.putBoolean(key, value)
                is Int -> settingsTag.putInt(key, value)
                is String -> settingsTag.putString(key, value)
                else -> {} // we can just yeet more here
            }
        }
        tag.put("KontraptionSettings", settingsTag)
    }

    fun loadControlSettings(tag: CompoundTag) {
        if (tag.contains("KontraptionSettings")) {
            val settingsTag = tag.getCompound("KontraptionSettings")
            controlDefaults.forEach { (key, default) ->
                if (settingsTag.contains(key)) {
                    controlSettings[key] =
                        when (default) {
                            is Boolean -> settingsTag.getBoolean(key)
                            is Int -> settingsTag.getInt(key)
                            is String -> settingsTag.getString(key)
                            else -> default
                        }
                } else {
                    controlSettings[key] = default
                }
            }
        } else {
            Kontraption.LOGGER.debug("SOMETHING MAY HAVE WENT WRONG") // This was triggered few times and it really ruffles my feathers
            controlSettings.putAll(controlDefaults)
        }
    }

    fun TileEntityMekanism.updateFromControl() {
        if (level !is ServerLevel || blockPos == null) return

        val ship =
            KontraptionVSUtils.getShipObjectManagingPos(level as ServerLevel, blockPos)
                ?: KontraptionVSUtils.getShipManagingPos(level as ServerLevel, blockPos)
                ?: return

        KontraptionBConfigControl.getOrCreate(ship).allConfigBlock().find { it.pos == blockPos.toJOML() }?.let { configBlock ->
            configBlock.settings.forEach { setting ->
                when (setting) {
                    is KontraptionBConfigControl.BlockSetting.BooleanSetting ->
                        controlSettings[setting.name] = setting.value
                    is KontraptionBConfigControl.BlockSetting.IntSetting ->
                        controlSettings[setting.name] = setting.value
                    is KontraptionBConfigControl.BlockSetting.StringSetting ->
                        controlSettings[setting.name] = setting.value
                }
            }
        }
    }

    fun onControlSettingChanged(setting: KontraptionBConfigControl.BlockSetting<*>) {
        when (setting) {
            is KontraptionBConfigControl.BlockSetting.BooleanSetting ->
                controlSettings[setting.name] = setting.value
            is KontraptionBConfigControl.BlockSetting.IntSetting ->
                controlSettings[setting.name] = setting.value
            is KontraptionBConfigControl.BlockSetting.StringSetting ->
                controlSettings[setting.name] = setting.value
        }
        if (this is BlockEntity) {
            setChanged()
        }
    }

    fun TileEntityMekanism.registerWithControlSystem() {
        if (level !is ServerLevel || blockPos == null) return

        val settings =
            controlSettings.map { (name, value) ->
                val default = controlDefaults[name]
                when (default) {
                    is Boolean -> KontraptionBConfigControl.BlockSetting.BooleanSetting(name, value as Boolean)
                    is Int -> {
                        val meta = controlDefaultsNonSync[name] ?: intSettingMeta(name, 100, 0, false) // if it somehow have read that its int IT HAS TO HAVE DATA!!, this comment reffered to me placing !! at this, at first launch i got null pointer . . .
                        KontraptionBConfigControl.BlockSetting.IntSetting(
                            name,
                            value as Int,
                            meta.maxVal,
                            meta.minVal,
                            meta.scaled,
                        )
                    }
                    is String -> KontraptionBConfigControl.BlockSetting.StringSetting(name, value as String)
                    else -> throw IllegalArgumentException("Unsupported setting type")
                }
            }

        val ship =
            KontraptionVSUtils.getShipObjectManagingPos(level as ServerLevel, blockPos)
                ?: KontraptionVSUtils.getShipManagingPos(level as ServerLevel, blockPos)
                ?: return

        KontraptionBConfigControl.getOrCreate(ship).let {
            it.removeConfigBlock(blockPos)
            it.removeListeners(blockPos.toJOML())
            it.addConfigBlock(blockPos, this, settings)
            it.addListener(blockPos.toJOML()) { setting ->
                if (this is IControllable) {
                    this.onControlSettingChanged(setting)
                }
            }
        }
    }

    fun TileEntityMekanism.unregisterFromControlSystem() {
        if (level !is ServerLevel) return

        val ship =
            KontraptionVSUtils.getShipObjectManagingPos(level as ServerLevel, blockPos)
                ?: KontraptionVSUtils.getShipManagingPos(level as ServerLevel, blockPos)
                ?: return

        val bConfig = KontraptionBConfigControl.getOrCreate(ship)
        bConfig.removeListeners(blockPos.toJOML()) // facepalm, i did remove it from lisener system BUT NOT THE FUCKIN BCONFIG
        bConfig.removeConfigBlock(blockPos)
    }
}
