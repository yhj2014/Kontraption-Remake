package net.illuc.kontraption.util

import net.minecraft.server.level.ServerLevel
import org.valkyrienskies.core.internal.joints.VSJoint
import org.valkyrienskies.core.internal.joints.VSJointAndId
import org.valkyrienskies.mod.common.ValkyrienSkiesMod
import org.valkyrienskies.mod.common.dimensionId
import org.valkyrienskies.mod.common.util.GameToPhysicsAdapter

object VSUtils {
}

//TOTALY NOT YOINKED FROM CLOCKWORKS :3
val ServerLevel.gtpa: GameToPhysicsAdapter get() = ValkyrienSkiesMod.getOrCreateGTPA(this.dimensionId)
fun GameToPhysicsAdapter.updateJoint(id: Int, joint: VSJoint) { this.updateJoint(VSJointAndId(id, joint)) }