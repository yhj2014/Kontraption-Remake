package net.illuc.kontraption.util

import net.illuc.kontraption.ship.KontraptionBConfigControl.BlockSetting
import net.illuc.kontraption.ship.KontraptionBConfigControl.ConfigBlock
import net.minecraft.network.FriendlyByteBuf
import org.joml.Vector3i

// TECHNICALLY AN GUI WELL NOT CLASS  BUT WELL

fun FriendlyByteBuf.writeConfigBlocks(blocks: List<ConfigBlock>) {
    writeVarInt(blocks.size)
    for (block in blocks) {
        writeBlockPos(block.pos.toBlockPos())
        writeUtf(block.blockId)
        writeVarInt(block.settings.size)
        for (setting in block.settings) {
            writeUtf(setting.name)
            when (setting) {
                is BlockSetting.BooleanSetting -> {
                    writeUtf("bool")
                    writeBoolean(setting.value)
                }
                is BlockSetting.IntSetting -> {
                    writeUtf("int")
                    writeVarInt(setting.value)
                    writeVarInt(setting.minVal)
                    writeVarInt(setting.maxVal)
                    writeBoolean(setting.scaled)
                }
                is BlockSetting.StringSetting -> {
                    writeUtf("string")
                    writeUtf(setting.value)
                }
            }
        }
    }
}

fun FriendlyByteBuf.readConfigBlocks(): List<ConfigBlock> {
    val count = readVarInt()
    val blocks = mutableListOf<ConfigBlock>()

    repeat(count) {
        val pos = readBlockPos()
        val blockId = readUtf()
        val settingsCount = readVarInt()
        val settings = mutableListOf<BlockSetting<*>>()

        repeat(settingsCount) {
            val name = readUtf()
            val typeId = readUtf()

            val setting: BlockSetting<*> =
                when (typeId) {
                    "bool" -> BlockSetting.BooleanSetting(name, readBoolean())
                    "int" -> {
                        val value = readVarInt()
                        val minVal = readVarInt()
                        val maxVal = readVarInt()
                        val scaled = readBoolean()
                        BlockSetting.IntSetting(name, value, maxVal, minVal, scaled)
                    }
                    "string" -> BlockSetting.StringSetting(name, readUtf())
                    else -> throw IllegalArgumentException("Unknown Buffer Type: $typeId")
                }

            settings.add(setting)
        }

        blocks.add(ConfigBlock(pos.toJOML(), settings, blockId))
    }

    return blocks
}

fun FriendlyByteBuf.writeConfigBlock(block: ConfigBlock) {
    writeInt(block.pos.x)
    writeInt(block.pos.y)
    writeInt(block.pos.z)
    writeUtf(block.blockId)
    writeInt(block.settings.size)
    for (setting in block.settings) {
        when (setting) {
            is BlockSetting.BooleanSetting -> {
                writeUtf("boolean")
                writeUtf(setting.name)
                writeBoolean(setting.value)
            }
            is BlockSetting.IntSetting -> {
                writeUtf("int")
                writeUtf(setting.name)
                writeVarInt(setting.value)
                writeVarInt(setting.minVal)
                writeVarInt(setting.maxVal)
                writeBoolean(setting.scaled)
            }
            is BlockSetting.StringSetting -> {
                writeUtf("string")
                writeUtf(setting.name)
                writeUtf(setting.value)
            }
        }
    }
}

fun FriendlyByteBuf.readConfigBlock(): ConfigBlock {
    val x = readInt()
    val y = readInt()
    val z = readInt()
    val pos = Vector3i(x, y, z)

    val blockId = readUtf()

    val settingsCount = readInt()
    val settings = mutableListOf<BlockSetting<*>>()
    repeat(settingsCount) {
        val type = readUtf()
        val name = readUtf()
        val setting =
            when (type) {
                "boolean" -> BlockSetting.BooleanSetting(name, readBoolean())
                "int" -> {
                    val value = readVarInt()
                    val minVal = readVarInt()
                    val maxVal = readVarInt()
                    val scaled = readBoolean()
                    BlockSetting.IntSetting(name, value, maxVal, minVal, scaled)
                }
                "string" -> BlockSetting.StringSetting(name, readUtf())
                else -> throw IllegalArgumentException("Unknown BlockSetting type: $type")
            }
        settings.add(setting)
    }

    return ConfigBlock(pos, settings, blockId)
}
