package net.illuc.kontraption.network.to_server

import mekanism.common.network.IMekanismPacket
import net.illuc.kontraption.ship.KontraptionBConfigControlOLD
import net.illuc.kontraption.ship.KontraptionBConfigControlOLD.ConfigBlock
import net.illuc.kontraption.util.KontraptionVSUtils.getShipObjectManagingPos
import net.illuc.kontraption.util.readConfigBlock
import net.illuc.kontraption.util.writeConfigBlock
import net.minecraft.core.BlockPos
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.server.level.ServerLevel
import net.minecraftforge.network.NetworkEvent
import org.valkyrienskies.core.api.util.GameTickOnly

class PacketKontraptionScreen(
    val block: ConfigBlock,
) : IMekanismPacket {

    override fun encode(buf: FriendlyByteBuf) {
        buf.writeConfigBlock(block)
    }

    @OptIn(GameTickOnly::class)
    override fun handle(context: NetworkEvent.Context) {
        context.enqueueWork {
            val player = context.sender ?: return@enqueueWork

            val pos = BlockPos(block.pos.x, block.pos.y, block.pos.z)

            val ship = getShipObjectManagingPos(player.level() as ServerLevel,
                BlockPos(block.pos.x, block.pos.y, block.pos.z)
            ) ?: return@enqueueWork

            val control = KontraptionBConfigControlOLD.getOrCreate(ship)
            control.updateConfigBlockFull(
                pos,
                block.settings
            )
        }
        context.packetHandled = true
    }


    companion object {
        fun decode(buf: FriendlyByteBuf): PacketKontraptionScreen =
            PacketKontraptionScreen(buf.readConfigBlock())
    }
}

