@file:Suppress("AddExplicitTargetToParameterAnnotation")

package net.illuc.kontraption.ship

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import net.illuc.kontraption.util.toJOML
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.entity.BlockEntity
import org.joml.Vector3i
import org.valkyrienskies.core.api.VsBeta
import org.valkyrienskies.core.api.attachment.getAttachment
import org.valkyrienskies.core.api.ships.LoadedServerShip
import org.valkyrienskies.core.api.util.GameTickOnly
import java.util.concurrent.CopyOnWriteArrayList
data class KVec3i(
    val x: Int,
    val y: Int,
    val z: Int,
) {
    fun toJoml() = Vector3i(x, y, z)
    //Yes mc has its own Vec3i but im WAYY to tired to even try if its serializable
    companion object {
        fun fromJoml(v: Vector3i) = KVec3i(v.x, v.y, v.z)
    }
}
@JsonIgnoreProperties(ignoreUnknown = true)
class KontraptionBConfigControlOLD {
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
    @JsonSubTypes(
        JsonSubTypes.Type(value = BlockSetting.BooleanSetting::class, name = "boolean"),
        JsonSubTypes.Type(value = BlockSetting.IntSetting::class, name = "int"),
        JsonSubTypes.Type(value = BlockSetting.StringSetting::class, name = "string"),
    )

    // IMA ABOUT TO PUT SERIALIZATION INTO MY [HYPERLINK BLOCKED]
    sealed class BlockSetting<T> {
        abstract val name: String
        abstract var value: T


        data class BooleanSetting
            @JsonCreator
            constructor(
                @JsonProperty("name") override val name: String,
                @JsonProperty("value") override var value: Boolean, // fuck if i know what inteliJ wants here
            ) : BlockSetting<Boolean>()

        data class IntSetting
            @JsonCreator
            constructor(
                @JsonProperty("name") override val name: String,
                @JsonProperty("value") override var value: Int,
                @JsonProperty("maxInt") val maxVal: Int,
                @JsonProperty("minInt") val minVal: Int,
                @JsonProperty("scaled") val scaled: Boolean = false,
            ) : BlockSetting<Int>() {
                @JsonIgnore
                fun displayValue(): Double = if (scaled) value / 10.0 else value.toDouble()

                fun setFromDisplay(doubleVal: Double) {
                    value = if (scaled) (doubleVal * 10).toInt() else doubleVal.toInt() // i needed atleast one precission doubles xd
                }
            }

        data class StringSetting
            @JsonCreator
            constructor(
                @JsonProperty("name") override val name: String,
                @JsonProperty("value") override var value: String,
            ) : BlockSetting<String>()
    }

    data class ConfigBlock
        @JsonCreator
        constructor(
            @JsonProperty("pos") val pos: KVec3i,
            @JsonProperty("settings") val settings: MutableList<BlockSetting<*>>,
            @JsonProperty("blockId") val blockId: String,
        )

    @JsonIgnore
    private val listeners = mutableMapOf<KVec3i, MutableList<(BlockSetting<*>) -> Unit>>() // have no clue if event system wont cause issues, buttt not my problem rn
    private val blockSettings = CopyOnWriteArrayList<ConfigBlock>()

    fun addListener(
        pos: BlockPos,
        listener: (BlockSetting<*>) -> Unit,
    ) {
        listeners.computeIfAbsent(pos.toKVec3i()) { mutableListOf() }.add(listener)
    }

    fun removeListeners(pos: BlockPos) {
        listeners.remove(pos.toKVec3i())
    }

    fun addConfigBlock(
        pos: BlockPos,
        blockEntity: BlockEntity?,
        settings: List<BlockSetting<*>>,
    ) {
        val blockId = blockEntity?.blockState?.block?.descriptionId ?: "unknown"
        blockSettings.add(ConfigBlock(pos.toKVec3i(), settings.toMutableList(), blockId))
    }

    fun removeConfigBlock(pos: BlockPos) {
        blockSettings.removeAll { it.pos == pos.toKVec3i() }
    }

    // FUCKIN SERIALIZER AUTODETECTS GETS
    @JsonIgnore
    fun allConfigBlock(): CopyOnWriteArrayList<ConfigBlock> = blockSettings

    fun updateConfigBlock(
        pos: BlockPos,
        settingName: String,
        newValue: Any,
    ): Boolean {
        val configBlock = blockSettings.find { it.pos == pos.toKVec3i() } ?: return false
        val setting = configBlock.settings.find { it.name == settingName } ?: return false

        return when (setting) {
            is BlockSetting.BooleanSetting ->
                if (newValue is Boolean) {
                    setting.value = newValue
                    listeners[configBlock.pos]?.forEach { it(setting) }
                    true
                } else {
                    false
                }
            is BlockSetting.IntSetting ->
                if (newValue is Int) {
                    setting.value = newValue
                    listeners[configBlock.pos]?.forEach { it(setting) }
                    true
                } else {
                    false
                }
            is BlockSetting.StringSetting ->
                if (newValue is String) {
                    setting.value = newValue
                    listeners[configBlock.pos]?.forEach { it(setting) }
                    true
                } else {
                    false
                }
        }
    }

    fun updateConfigBlockFull(
        pos: BlockPos,
        newSettings: List<BlockSetting<*>>,
    ) {
        val configBlock = blockSettings.find { it.pos == pos.toKVec3i() }
        if (configBlock != null) {
            for (newSetting in newSettings) {
                val existingSetting = configBlock.settings.find { it.name == newSetting.name }
                if (existingSetting != null) {
                    when {
                        existingSetting is BlockSetting.BooleanSetting && newSetting is BlockSetting.BooleanSetting -> {
                            existingSetting.value = newSetting.value
                            listeners[configBlock.pos]?.forEach { it(existingSetting) }
                        }
                        existingSetting is BlockSetting.IntSetting && newSetting is BlockSetting.IntSetting -> {
                            existingSetting.value = newSetting.value
                            listeners[configBlock.pos]?.forEach { it(existingSetting) }
                        }
                        existingSetting is BlockSetting.StringSetting && newSetting is BlockSetting.StringSetting -> {
                            existingSetting.value = newSetting.value
                            listeners[configBlock.pos]?.forEach { it(existingSetting) }
                        }
                    }
                }
            }
        }
    }

    companion object {
        @OptIn(GameTickOnly::class, VsBeta::class)
        fun getOrCreate(ship: LoadedServerShip): KontraptionBConfigControlOLD = ship.getAttachment<KontraptionBConfigControlOLD>() ?: KontraptionBConfigControlOLD().also { ship.setAttachment(it) }

        @JvmStatic
        @JsonCreator
        fun create(
            @JsonProperty("blockSettings") blockSettings: List<ConfigBlock>?,
        ): KontraptionBConfigControlOLD =
            KontraptionBConfigControlOLD().apply {
                if (blockSettings != null) {
                    this.blockSettings.addAll(blockSettings)
                }
            }
    }
}
fun BlockPos.toKVec3i() : KVec3i {
    return KVec3i(x, y, z)
}
