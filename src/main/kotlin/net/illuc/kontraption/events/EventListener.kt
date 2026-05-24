package net.illuc.kontraption.events

import net.illuc.kontraption.blockEntities.TileEntityKey
import net.illuc.kontraption.ship.KontraptionKeyBlockControl
import net.illuc.kontraption.util.toBlockPos
import net.minecraftforge.common.NeoForge
import net.minecraftforge.eventbus.api.SubscribeEvent
import org.valkyrienskies.core.api.util.GameTickOnly

object EventListener {
    @OptIn(GameTickOnly::class)
    @SubscribeEvent
    fun onKeyEvent(event: KeyBindEvent) {
        val level = event.player.level()
        val klist = KontraptionKeyBlockControl.getOrCreate(event.ship).getKeystones()
        klist.forEach { (position, keybind) ->
            val blockEntity = level.getBlockEntity(position.toBlockPos()) // WE WERE GETTING BLOCKENTITY FROM POSITION ANYWAY WHY THE FUCK WAS I TRYING TO SERIALIZE IT
            if (blockEntity is TileEntityKey) {
                blockEntity.fire(event)
            }
        }
    }

    fun register() {
        NeoForge.EVENT_BUS.register(this)
    }
}
