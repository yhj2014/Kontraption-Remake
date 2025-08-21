package net.illuc.kontraption.network.to_server

import mekanism.common.network.IMekanismPacket
import net.illuc.kontraption.ship.KontraptionBConfigControl
import net.illuc.kontraption.util.KontraptionVSUtils.getShipObjectManagingPos
import net.illuc.kontraption.util.readConfigBlock
import net.illuc.kontraption.util.toBlockPos
import net.illuc.kontraption.util.writeConfigBlock
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.server.level.ServerLevel
import net.minecraftforge.network.NetworkEvent
import org.valkyrienskies.core.api.ships.ServerShip
import java.util.function.Supplier

class PacketKontraptionScreen(
    val block: KontraptionBConfigControl.ConfigBlock,
) : IMekanismPacket {
    override fun encode(buf: FriendlyByteBuf) {
        buf.writeConfigBlock(block)
    }

    override fun handle(context: NetworkEvent.Context) {
        context.enqueueWork {
            val player = context.sender ?: return@enqueueWork
            val bpos = block.pos.toBlockPos()
            val ship = getShipObjectManagingPos(player.level() as ServerLevel, bpos) ?: return@enqueueWork
            val control = KontraptionBConfigControl.getOrCreate(ship) // ONO
            control.updateConfigBlockFull(
                bpos,
                block.settings,
            )
        }
        context.packetHandled = true
    }

    companion object {
        fun decode(buf: FriendlyByteBuf): PacketKontraptionScreen = PacketKontraptionScreen(buf.readConfigBlock())
    }
}
