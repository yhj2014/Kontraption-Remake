package net.illuc.kontraption.util

import net.illuc.kontraption.ship.KVec3i
import net.illuc.kontraption.ship.KontraptionBConfigControlOLD
import net.illuc.kontraption.ship.KontraptionBConfigControlOLD.ConfigBlock
import net.illuc.kontraption.ship.KontraptionBConfigControlOLD.BlockSetting
import net.minecraft.network.FriendlyByteBuf
import org.joml.Vector3i

// TECHNICALLY AN GUI WELL NOT CLASS  BUT WELL

fun FriendlyByteBuf.writeConfigBlocks(blocks: List<ConfigBlock>) {
    writeVarInt(blocks.size)
    for (block in blocks) {
        writeVarInt(block.pos.x)
        writeVarInt(block.pos.y)
        writeVarInt(block.pos.z)
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
        val pos =
            KVec3i(
                readVarInt(),
                readVarInt(),
                readVarInt()
            )
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

        blocks.add(ConfigBlock(pos, settings, blockId))
    }

    return blocks
}

fun FriendlyByteBuf.writeConfigBlock(block: ConfigBlock) {
    writeVarInt(block.pos.x)
    writeVarInt(block.pos.y)
    writeVarInt(block.pos.z)

    writeUtf(block.blockId)

    writeVarInt(block.settings.size)
    for (setting in block.settings) {
        writeUtf(setting.name)
        when (setting) {
            is BlockSetting.BooleanSetting -> {
                writeUtf("boolean")
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

fun FriendlyByteBuf.readConfigBlock(): ConfigBlock {
    val pos = KVec3i(readVarInt(), readVarInt(), readVarInt())

    val blockId = readUtf()
    val settingsCount = readVarInt()
    val settings = mutableListOf<BlockSetting<*>>()

    repeat(settingsCount) {
        val name = readUtf()
        val type = readUtf()
        val setting = when (type) {
            "boolean" -> BlockSetting.BooleanSetting(name, readBoolean())
            "int" -> BlockSetting.IntSetting(
                name,
                readVarInt(),
                readVarInt(),
                readVarInt(),
                readBoolean()
            )
            "string" -> BlockSetting.StringSetting(name, readUtf())
            else -> error("Unknown BlockSetting type: $type")
        }
        settings.add(setting)
    }

    return ConfigBlock(pos, settings, blockId)
}