package net.illuc.kontraption.events

import net.minecraft.server.level.ServerPlayer
import net.minecraftforge.eventbus.api.Event
import org.valkyrienskies.core.api.ships.LoadedServerShip
import org.valkyrienskies.core.api.ships.ServerShip
import org.valkyrienskies.core.api.util.GameTickOnly

class KeyBindEvent @OptIn(GameTickOnly::class) constructor(
    val keybindIndex: Int,
    val updown: Boolean,
    val player: ServerPlayer,
    val ship: LoadedServerShip,
) : Event()
