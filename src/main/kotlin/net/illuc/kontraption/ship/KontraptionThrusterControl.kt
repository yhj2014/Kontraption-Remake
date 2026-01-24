package net.illuc.kontraption.ship

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import net.illuc.kontraption.Kontraption
import net.illuc.kontraption.ThrusterInterface
import net.illuc.kontraption.config.KontraptionConfigs
import net.illuc.kontraption.util.toJOML
import net.illuc.kontraption.util.toJOMLD
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import org.joml.Vector3d
import org.joml.Vector3i
import org.valkyrienskies.core.api.VsBeta
import org.valkyrienskies.core.api.attachment.getAttachment
import org.valkyrienskies.core.api.ships.*
import org.valkyrienskies.core.api.util.GameTickOnly
import org.valkyrienskies.core.api.util.PhysTickOnly
import org.valkyrienskies.core.api.world.PhysLevel
import thedarkcolour.kotlinforforge.forge.vectorutil.v3d.toVector3d
import thedarkcolour.kotlinforforge.forge.vectorutil.v3d.toVector3f
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.abs
import kotlin.math.min

@JsonIgnoreProperties(ignoreUnknown = true) // FOR MY SANITY
class KontraptionThrusterControl : ShipPhysicsListener {
    data class Thruster(
        val position: Vector3i,
        val forceDirection: Vector3d,
        val forceStrength: Double,
        val thruster: ThrusterInterface,
    )

    // Lists
    private val thrusters = CopyOnWriteArrayList<Thruster>()

    // Local config bs
    private val CONFIGSPEEDLIMIT = KontraptionConfigs.kontraption.thrusterSpeedLimit.get()
    private val RESISTSTRENGHT = KontraptionConfigs.kontraption.dampeningStrength.get()

    // vars
    private var dampenerActive = false
    private var lastVelo = Vector3d(0.0, 0.0, 0.0)
    private var accError = Vector3d(0.0, 0.0, 0.0)
    private var playerInput = Vector3d() // have to store it bc we cant just apply force anywhere
    private var stayInPlace = false

    @OptIn(PhysTickOnly::class, PhysTickOnly::class, VsBeta::class)
    override fun physTick(physShip: PhysShip, physLevel: PhysLevel) {

        val velocity = Vector3d(physShip.velocity)
        val mass = physShip.mass
        val maxSpeed = KontraptionConfigs.kontraption.thrusterSpeedLimit.get()

        val targetVelocity =
            if (stayInPlace)
                Vector3d(0.0, 0.0, 0.0)
            else
                Vector3d(playerInput).mul(maxSpeed)

        val velError = targetVelocity.sub(velocity, Vector3d())
        if (velError.lengthSquared() < 0.0001){
            thrusters.forEach { it.thruster.powered = false }
            return
        }
        val response = KontraptionConfigs.kontraption.thrusterResponse.get()
        val desiredAccel = velError.mul(response)
        val desiredForce = desiredAccel.mul(mass)

        thrusters.forEach { thr ->
            val shipDir = thr.forceDirection
            val worldDir = physShip.transform.shipToWorld
                .transformDirection(shipDir, Vector3d())

            val alignment = worldDir.dot(desiredForce)

            if (alignment > 0.0) {
                val maxForce = thr.thruster.thrusterPower * KontraptionConfigs.kontraption.ionThrust.get()
                val applied = min(alignment, maxForce)

                thr.thruster.powered = true

                physShip.applyWorldForce(
                    worldDir.mul(applied, Vector3d())
                )
            } else {
                thr.thruster.powered = false
            }
        }
    }

    fun setDampenersState(st: Boolean) {
        dampenerActive = st
        if (!st) {
            accError = Vector3d(0.0, 0.0, 0.0).toVector3f().toVector3d() // DONT EVEN ASK
            lastVelo = Vector3d(0.0, 0.0, 0.0)
        }
    }

    fun setPlayerInput(input: Vector3d) {
        playerInput.set(input)
    }

    fun addThruster(
        pos: BlockPos,
        force: Vector3d,
        tier: Double,
        thruster: ThrusterInterface,
    ) {
        thrusters.add(Thruster(pos.toJOML(), force, tier, thruster))
    }

    fun removeThruster(
        pos: BlockPos,
        force: Vector3d,
        tier: Double,
        thruster: ThrusterInterface,
    ) {
        thrusters.remove(Thruster(pos.toJOML(), force, tier, thruster))
    }

    fun stopThruster(pos: BlockPos) {
        thrusters.removeAll { it.position == pos.toJOML() }
    }

    fun assignThrustPerDirection(
        requestedThrust: Map<Vector3d, Double>,
        setMax: Boolean,
    ) {
        val groups = thrusters.groupBy { it.forceDirection }
        for ((direction, totalThrust) in requestedThrust) {
            val thrustersInDirection = groups[direction] ?: continue
            if (!setMax) {
                val splitThrust = totalThrust / thrustersInDirection.size
                for (thruster in thrustersInDirection) {
                    thruster.thruster.currentThrust = splitThrust.coerceIn(0.0, thruster.thruster.thrusterPower)
                }
            } else {
                if (totalThrust > 0) {
                    for (thruster in thrustersInDirection) {
                        Kontraption.LOGGER.debug("Applied Thrust: ${thruster.thruster.currentThrust}")
                        thruster.thruster.currentThrust = thruster.thruster.thrusterPower
                    }
                }
            }
        }
        thrusters
            .filter { it.forceDirection !in requestedThrust.keys }
            .forEach { it.thruster.currentThrust = 0.0 }
    }

    companion object {
        @OptIn(VsBeta::class, GameTickOnly::class)
        fun getOrCreate(ship: LoadedServerShip): KontraptionThrusterControl =
            ship.getAttachment<KontraptionThrusterControl>()
                ?: KontraptionThrusterControl().also { ship.setAttachment(it) }
    }
}
