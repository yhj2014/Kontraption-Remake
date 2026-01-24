package net.illuc.kontraption.ship

import net.illuc.kontraption.blockEntities.TileEntityGyro
import net.illuc.kontraption.config.KontraptionConfigs
import net.illuc.kontraption.util.toJOML
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.entity.BlockEntity
import org.joml.Quaterniond
import org.joml.Vector3d
import org.joml.Vector3i
import org.valkyrienskies.core.api.VsBeta
import org.valkyrienskies.core.api.attachment.getAttachment
import org.valkyrienskies.core.api.ships.*
import org.valkyrienskies.core.api.util.GameTickOnly
import org.valkyrienskies.core.api.util.PhysTickOnly
import org.valkyrienskies.core.api.world.PhysLevel
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl
import java.util.concurrent.CopyOnWriteArrayList

class KontraptionGyroControl : ShipPhysicsListener {
    data class Gyro(
        val position: Vector3i,
        val tier: Double,
        val be: BlockEntity,
    )

    private val gyros = CopyOnWriteArrayList<Gyro>()
    private var targetRotation = Quaterniond()
    private var targetStrength = 1.0

    val gyroStrength = 100000.0

    fun addGyro(
        pos: BlockPos,
        tier: Double,
        be: TileEntityGyro,
    ) {
        gyros.add(Gyro(pos.toJOML(), tier, be))
    }

    fun removeGyro(
        pos: BlockPos,
        tier: Double,
        be: TileEntityGyro,
    ) {
        gyros.remove(Gyro(pos.toJOML(), tier, be))
    }

    fun stopGyro(pos: BlockPos) {
        gyros.removeAll { it.position == pos.toJOML() }
    }

    fun pointTowards(
        targetRotation: Quaterniond,
        power: Double,
    ) {
        // val axis = seatDir.normal.toJOMLD().cross(targetDirection, Vector3d())
        this.targetRotation = targetRotation // Quaterniond(AxisAngle4d(seatDir.normal.toJOMLD().angle(targetDirection), axis)).normalize()
        this.targetStrength = power
    }

    @OptIn(PhysTickOnly::class)
    override fun physTick(physShip: PhysShip, physLevel: PhysLevel) {
        if (KontraptionConfigs.kontraption.zeroGravity.get()) {
            physShip.applyWorldForce(Vector3d(0.0, physShip.mass * 10, 0.0))
        }
        if (gyros.isNotEmpty()) {
            val totalPower = gyroStrength * gyros.size * targetStrength

            val rotDif =
                targetRotation
                    .mul(physShip.transform.shipToWorldRotation.invert(Quaterniond()), Quaterniond())
                    .normalize()
                    .invert()

            // Blackmagic ask triode
            val idealOmega = Vector3d(rotDif.x() * 2.0, rotDif.y() * 2.0, rotDif.z() * 2.0)
            if (rotDif.w() > 0) idealOmega.mul(-1.0)

            idealOmega.sub(physShip.angularVelocity)
            
            val idealTorque =
                physShip.transform.rotation.transform(
                    physShip.momentOfInertia.transform(
                        physShip.transform.rotation.transformInverse(idealOmega, Vector3d()),
                    ),
                )

            idealTorque.mul(KontraptionConfigs.kontraption.gyroTorqueStrength.get())

            physShip.applyInvariantTorque(idealTorque)
        }
    }

    companion object {
        @OptIn(GameTickOnly::class, VsBeta::class)
        fun getOrCreate(ship: LoadedServerShip): KontraptionGyroControl =
            ship.getAttachment<KontraptionGyroControl>()
                ?: KontraptionGyroControl().also { ship.setAttachment(it) }
    }
}
