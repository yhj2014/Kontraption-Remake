# Kontraption Code Wiki

> **A Space Engineers inspired addon for Mekanism utilizing Valkyrien Skies 2**

## Table of Contents

1. [Project Overview](#project-overview)
2. [Architecture Overview](#architecture-overview)
3. [Module Responsibilities](#module-responsibilities)
4. [Key Classes and Functions](#key-classes-and-functions)
5. [Dependency Relationships](#dependency-relationships)
6. [Configuration System](#configuration-system)
7. [Network Communication](#network-communication)
8. [Rendering System](#rendering-system)
9. [Mixins](#mixins)
10. [Project Build & Run](#project-build--run)

---

## Project Overview

**Kontraption** is a Minecraft mod for Forge 1.20.1 that adds spaceship/aircraft-related blocks and multiblocks. It integrates with:
- **Valkyrien Skies 2** (v2.4.6+) - For physics-based ship movement
- **Mekanism** (v10.4.0+) - For energy and chemical systems

### Key Features
- **Ion Thruster** - Simple energy-powered thruster
- **Liquid Fuel Thruster** - Multiblock hydrogen-powered thruster (3x3x3 to 17x17x17)
- **Large Ion Ring Thruster** - Energy-powered ring-shaped multiblock thruster
- **Ship Control Interface** - Player-controlled ship piloting
- **Gyroscope** - Ship rotation stabilization and control
- **Toolgun** - Ship assembly and movement tool
- **Railgun** - Multiblock weapon system (WIP)

### Version Information
```properties
minecraft_version=1.20.1
forge_version=47.2.0
mod_version=0.0.5
```

---

## Architecture Overview

```
kontraption/
├── src/main/
│   ├── java/net/illuc/kontraption/
│   │   ├── Kontraption.kt                    # Main mod entry point
│   │   ├── GlobalRegistry.java               # Registry holder
│   │   ├── KontraptionBlocks.java            # Block definitions
│   │   ├── KontraptionTileEntityTypes.java   # TileEntity types
│   │   ├── KontraptionBlockTypes.java        # Block type configurations
│   │   ├── mixin/                            # Mixin injectors
│   │   └── util/
│   │       └── KontraptionVSUtils.java       # Valkyrien Skies utilities
│   │
│   └── kotlin/net/illuc/kontraption/
│       ├── blockEntities/                    # TileEntity implementations
│       │   ├── TileEntityIonThruster.kt
│       │   ├── TileEntityShipControlInterface.kt
│       │   ├── TileEntityGyro.kt
│       │   ├── TileEntityCannon.kt
│       │   └── largehydrogen/               # Liquid fuel thruster TEs
│       │   └── railgun/                      # Railgun TEs
│       │
│       ├── blocks/                           # Block classes
│       │   ├── BlockIonThruster.kt
│       │   ├── BlockGyro.kt
│       │   └── ShipControlInterfaceBlock.kt
│       │
│       ├── ship/                             # Ship physics controllers
│       │   ├── KontraptionThrusterControl.kt
│       │   ├── KontraptionGyroControl.kt
│       │   └── KontraptionKeyBlockControl.kt
│       │
│       ├── multiblocks/                      # Multiblock structures
│       │   ├── largeionring/                # Large ion ring
│       │   └── largeHydrogenThruster/       # Liquid fuel thruster
│       │
│       ├── network/                          # Packet handling
│       │   └── to_server/
│       │
│       ├── client/                           # Client-side rendering
│       │   ├── KontraptionClientTickHandler.kt
│       │   └── render/
│       │
│       ├── config/                           # Configuration
│       │   └── KontraptionConfig.kt
│       │
│       └── util/                             # Utilities
│           ├── VSUtils.kt
│           ├── ShapeGenerators.kt
│           └── ControllableTileEntity.kt
│
└── src/main/resources/
    ├── META-INF/mods.toml
    ├── assets/kontraption/
    │   ├── blockstates/
    │   ├── models/
    │   ├── textures/
    │   └── lang/
    └── kontraption.mixins.json
```

---

## Module Responsibilities

### 1. Core Module (`Kontraption.kt`)

**File**: [Kontraption.kt](file:///workspace/src/main/kotlin/net/illuc/kontraption/Kontraption.kt)

Main mod entry point annotated with `@Mod(Kontraption.MODID)`.

**Key Responsibilities:**
- Register all blocks, items, tile entities, and creative tab
- Initialize network packet handler
- Register Valkyrien Skies ship attachments
- Client-side renderer registration
- Command registration

**Key Methods:**
| Method | Purpose |
|--------|---------|
| `commonSetup()` | Initialize multiblocks, tags, packets, VS attachments |
| `clientSetup()` | Set render layers for blocks |
| `createCreativeTab()` | Define mod creative tab contents |

---

### 2. Block Registry (`KontraptionBlocks.java`, `GlobalRegistry.java`)

**Files**: 
- [KontraptionBlocks.java](file:///workspace/src/main/java/net/illuc/kontraption/KontraptionBlocks.java)
- [GlobalRegistry.java](file:///workspace/src/main/java/net/illuc/kontraption/GlobalRegistry.java)

**Key Blocks:**
| Block | Purpose |
|-------|---------|
| `ION_THRUSTER` | Energy-powered thruster |
| `SHIP_CONTROL_INTERFACE` | Player piloting interface |
| `GYRO` | Rotation stabilization |
| `LIQUID_FUEL_THRUSTER_CASING/VALVE/EXHAUST` | Multiblock hydrogen thruster |
| `CANNON` | Gas-powered projectile weapon |
| `LARGE_ION_THRUSTER_*` | Ring-shaped multiblock thruster |
| `RAILGUN_*` | Multiblock railgun components |
| `*_PLUSHIE` | Decorative blocks (Otter, Cosmic, Illuc) |

---

### 3. Tile Entity Module

**Base Class**: `ControllableTileEntity` ([ControllableTileEntity.kt](file:///workspace/src/main/kotlin/net/illuc/kontraption/util/ControllableTileEntity.kt))

Abstract base for all controllable blocks, extends Mekanism's `TileEntityMekanism` and implements `IControllable`.

**Key Tile Entities:**

#### TileEntityIonThruster
**File**: [TileEntityIonThruster.kt](file:///workspace/src/main/kotlin/net/illuc/kontraption/blockEntities/TileEntityIonThruster.kt)

Energy-powered single-block thruster.

```kotlin
class TileEntityIonThruster : ControllableTileEntity, ThrusterInterface {
    // Energy container for power storage
    private var energyContainer: MachineEnergyContainer<TileEntityIonThruster>?
    
    // Thruster configuration
    override val thrusterPower: Double = KontraptionConfigs.kontraption.ionThrust.get()
    override var currentThrust: Double = 0.0
    override var powered: Boolean = false
    
    override fun onUpdateServer() // Energy consumption and thrust calculation
}
```

#### TileEntityShipControlInterface
**File**: [TileEntityShipControlInterface.kt](file:///workspace/src/main/kotlin/net/illuc/kontraption/blockEntities/TileEntityShipControlInterface.kt)

Central ship control hub for player piloting.

**Key Features:**
- Spawns `KontraptionShipMountingEntity` for player seat
- Handles player input transformation to ship coordinates
- Provides ComputerCraft peripheral integration
- Controls thrusters and gyros via ship attachments

**Key Methods:**
```kotlin
fun spawnSeat(...)      // Creates player seat entity
fun tick()             // Process player input, update ship controls
fun sit(player, force) // Mount player to seat
fun toggleDampener()   // Toggle inertia dampeners
```

#### TileEntityCannon
**File**: [TileEntityCannon.kt](file:///workspace/src/main/kotlin/net/illuc/kontraption/blockEntities/TileEntityCannon.kt)

Gas-powered projectile weapon using Mekanism hydrogen.

```kotlin
class TileEntityCannon : TileEntityConfigurableMachine {
    var inputTank: IGasTank?       // Hydrogen gas tank
    var cooldown: Int = 20         // Fire rate
    var ship: Ship?                // Parent ship reference
    
    override fun onUpdateServer()   // Fire projectiles, spawn particles
}
```

---

### 4. Ship Physics Control Module

**Base Interface**: `ThrusterInterface` ([ThrusterInterface.kt](file:///workspace/src/main/kotlin/net/illuc/kontraption/ThrusterInterface.kt))

```kotlin
interface ThrusterInterface {
    val thrusterLevel: Level?
    var enabled: Boolean
    val worldPosition: BlockPos?
    val forceDirection: Direction
    val thrusterPower: Double
    val basePower: Double
    var powered: Boolean
    var currentThrust: Double
    
    fun enable(level: ServerLevel?, bpos: BlockPos?)
    fun disable()
}
```

#### KontraptionThrusterControl
**File**: [KontraptionThrusterControl.kt](file:///workspace/src/main/kotlin/net/illuc/kontraption/ship/KontraptionThrusterControl.kt)

Ship attachment implementing `ShipPhysicsListener` for thruster physics.

```kotlin
class KontraptionThrusterControl : ShipPhysicsListener {
    private val thrusters = CopyOnWriteArrayList<Thruster>()
    
    // Thruster data class
    data class Thruster(
        val position: Vector3i,
        val forceDirection: Vector3d,
        val forceStrength: Double,
        val thruster: ThrusterInterface,
    )
    
    override fun physTick(physShip: PhysShip, physLevel: PhysLevel)
    fun setPlayerInput(input: Vector3d)
    fun setDampenersState(st: Boolean)
    fun assignThrustPerDirection(requestedThrust: Map<Vector3d, Double>, setMax: Boolean)
    
    companion object {
        fun getOrCreate(ship: LoadedServerShip): KontraptionThrusterControl
    }
}
```

**Physics Algorithm:**
1. Calculate target velocity from player input
2. Compute velocity error: `velError = targetVelocity - currentVelocity`
3. Apply PID-like response: `desiredAccel = velError * response`
4. Calculate desired force: `desiredForce = desiredAccel * mass`
5. Apply forces to thrusters aligned with desired force direction

#### KontraptionGyroControl
**File**: [KontraptionGyroControl.kt](file:///workspace/src/main/kotlin/net/illuc/kontraption/ship/KontraptionGyroControl.kt)

Ship attachment for rotation control.

```kotlin
class KontraptionGyroControl : ShipPhysicsListener {
    private val gyros = CopyOnWriteArrayList<Gyro>()
    private var targetRotation = Quaterniond()
    private var targetStrength = 1.0
    
    override fun physTick(physShip: PhysShip, physLevel: PhysLevel)
    fun pointTowards(targetRotation: Quaterniond, power: Double)
}
```

---

### 5. Multiblock System

#### Large Ion Ring
**File**: [LargeIonRingMultiBlock.kt](file:///workspace/src/main/kotlin/net/illuc/kontraption/multiblocks/largeionring/LargeIonRingMultiBlock.kt)

Ring-shaped energy-powered thruster (5x5 to 15x15 footprint, 2 blocks tall).

```kotlin
open class LargeIonRingMultiBlock : AbstractCuboidMultiblockController, ThrusterInterface {
    var exhaustDirection: Direction = Direction.DOWN
    var centerExhaust: BlockPos
    var innerVolume: Int
    var particleDir: Vector3d
    val energyStorage: EnergyBuffer
    
    override fun isMachineWhole(validatorCallback): Boolean
    fun serverMachineAssembly()
}
```

**Structure Validation:**
- Must be square (lengthX == lengthZ)
- Size range: 5-15 blocks
- Height: exactly 2 blocks
- Must contain controller block
- Uses `ShapeGenerators.largeIonRing()` for structure validation

#### Liquid Fuel Thruster
**File**: [LiquidFuelThrusterMultiblockData.kt](file:///workspace/src/main/kotlin/net/illuc/kontraption/multiblocks/largeHydrogenThruster/LiquidFuelThrusterMultiblockData.kt)

Hydrogen-powered multiblock thruster (3x3x3 to 17x17x17).

```kotlin
class LiquidFuelThrusterMultiblockData : MultiblockData, ThrusterInterface, IValveHandler {
    var gasTank: IGasTank?          // Hydrogen storage
    var burnRemaining: Double
    var currentThrust: Double
    
    fun burnFuel(world: Level)
    fun sendParticleData(...)
}
```

---

### 6. Networking Module

**File**: [KontraptionPacketHandler.kt](file:///workspace/src/main/kotlin/net/illuc/kontraption/network/KontraptionPacketHandler.kt)

```kotlin
class KontraptionPacketHandler : BasePacketHandler() {
    override fun initialize() {
        // Client to server messages
        registerClientToServer(PacketKontraptionGuiInteract::class.java)
        registerClientToServer(PacketKontraptionDriving::class.java)
        registerClientToServer(PacketKontraptionScreen::class.java)
    }
}
```

#### PacketKontraptionDriving
**File**: [PacketKontraptionDriving.kt](file:///workspace/src/main/kotlin/net/illuc/kontraption/network/to_server/PacketKontraptionDriving.kt)

Transmits player driving input from client to server.

```kotlin
class PacketKontraptionDriving(
    val impulse: Vector3dc,    // Movement direction
    val rotation: Vector3dc,  // Rotation input
    val openConf: Boolean,    // Config menu toggle
    val bface: Byte,          // Key bindings state
) : IMekanismPacket
```

---

### 7. Client Module

#### KontraptionClientTickHandler
**File**: [KontraptionClientTickHandler.kt](file:///workspace/src/main/kotlin/net/illuc/kontraption/client/KontraptionClientTickHandler.kt)

Handles client-side tick events for notifications and UI.

#### ClientEvents
**File**: [ClientEvents.java](file:///workspace/src/main/java/net/illuc/kontraption/ClientEvents.java)

```kotlin
public class ClientEvents {
    @Mod.EventBusSubscriber(Dist.CLIENT)
    public static class ClientRuntimeEvents {
        @SubscribeEvent
        public static void onRenderWorld(RenderLevelStageEvent event) {
            // Renders selection zones and debug info
            renderData(matrixStack, mainCamera)
        }
    }
}
```

---

### 8. Utilities Module

#### VSUtils
**File**: [VSUtils.kt](file:///workspace/src/main/kotlin/net/illuc/kontraption/util/VSUtils.kt) and [KontraptionVSUtils.java](file:///workspace/src/main/java/net/illuc/kontraption/util/KontraptionVSUtils.java)

Valkyrien Skies integration utilities:

```java
public class KontraptionVSUtils {
    public static LoadedShip getShipManagingPos(Level level, BlockPos blockPos)
    public static LoadedServerShip getShipObjectManagingPos(ServerLevel level, BlockPos blockPos)
    public static void createNewShipWithBlocks(BlockPos pos, DenseBlockPosSet set, ServerLevel level)
    public static String dimensionID(ServerLevel level)
}
```

#### ControllableTileEntity
**File**: [ControllableTileEntity.kt](file:///workspace/src/main/kotlin/net/illuc/kontraption/util/ControllableTileEntity.kt)

Base class for controllable blocks with settings management.

```kotlin
abstract class ControllableTileEntity : TileEntityMekanism, IControllable {
    final override val controlSettings = mutableMapOf<String, Any>()
    
    override fun registerWithControlSystem()
    override fun unregisterFromControlSystem()
    override fun registerControlSettings()
}
```

#### ShapeGenerators
**File**: [ShapeGenerators.kt](file:///workspace/src/main/kotlin/net/illuc/kontraption/util/ShapeGenerators.kt)

Generates 3D shapes for multiblock validation.

```kotlin
object ShapeGenerators {
    fun largeIonRing(width: Int, height: Int, depth: Int): Shape3D
}
```

---

## Key Classes and Functions

### Enums and Data Classes

#### KontraptionLang
**File**: [KontraptionLang.kt](file:///workspace/src/main/kotlin/net/illuc/kontraption/KontraptionLang.kt)

```kotlin
enum class KontraptionLang implements ILangEntry {
    KONTRAPTION("constants", "mod_name"),
    MODE_CHANGE("toolgun", "mode_change"),
    ASSEMBLE("toolgun", "assemble"),
    // ... more entries
}
```

### Item Classes

#### ItemToolgun
**File**: [ItemToolgun.kt](file:///workspace/src/main/kotlin/net/illuc/kontraption/item/ItemToolgun.kt)

Multi-mode tool for ship assembly and movement.

**Modes:**
- `ASSEMBLE` - Select and create ships from blocks
- `MOVE` - Teleport existing ships
- `LOCK` - Lock ship position (WIP)
- `PUSH` - Push ships (WIP)
- `ROTATE` - Rotate ships (WIP)
- `WELD` - Weld blocks (WIP)

**Key Methods:**
```kotlin
fun makeSelection(level, player, hand, pos)  // Block selection for ship creation
fun moveSelection(level, player, hand, pos)  // Ship teleportation
fun inventoryTick(...)                         // Preview rendering
```

### Peripheral Integration

#### ShipControlInterfacePeripheral
**File**: [ShipControlInterfacePeripheral.kt](file:///workspace/src/main/kotlin/net/illuc/kontraption/peripherals/ShipControlInterfacePeripheral.kt)

ComputerCraft peripheral for Lua ship control.

```kotlin
class ShipControlInterfacePeripheral : IDynamicPeripheral {
    // Methods available to CC:
    // getRotation() -> {x, y, z, w}
    // getMovement() -> {x, y, z}
    // getPosition() -> {x, y, z}
    // getWeight() -> Double
    // getSlug() -> String
    // getVelocity() -> {x, y, z}
    // setMovement(x, y, z)
    // setRotation(x, y, z, w)
    // rotateAlongAxis(x, y, z)
    // preciseThrustImpulse(x, y, z)
}
```

### Particle System

#### Particle Data Classes
**Files**: 
- [ThrusterParticleData.java](file:///workspace/src/main/java/net/illuc/kontraption/particles/ThrusterParticleData.java)
- [MuzzleFlashParticleData.java](file:///workspace/src/main/java/net/illuc/kontraption/particles/MuzzleFlashParticleData.java)

---

## Dependency Relationships

```
┌─────────────────────────────────────────────────────────────────┐
│                          Kontraption                             │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────┐     ┌──────────────┐     ┌──────────────┐   │
│  │ Valkyrien    │     │   Mekanism   │     │ ComputerCraft│   │
│  │ Skies 2      │     │              │     │              │   │
│  │              │     │              │     │              │   │
│  │ - vsCore     │     │ - Energy     │     │ - Peripheral │   │
│  │ - PhysShip   │     │ - Chemicals  │     │ - Computer    │   │
│  │ - ShipAttach │     │ - Tanks      │     │   Access      │   │
│  └──────┬───────┘     └──────┬───────┘     └──────┬───────┘   │
│         │                    │                    │            │
│         └────────────────────┼────────────────────┘            │
│                              │                                 │
│         ┌────────────────────┴────────────────────┐           │
│         │         Shared Dependencies             │           │
│         │                                          │           │
│         │  - JOML (Vector/Math)                   │           │
│         │  - Minecraft/Forge API                  │           │
│         │  - Mixin                                │           │
│         └─────────────────────────────────────────┘           │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### Build Dependencies (build.gradle)
```groovy
// Core dependencies
minecraft "net.minecraftforge:forge:${minecraft_version}-${forge_version}"

// Valkyrien Skies 2
implementation("org.valkyrienskies.core:api:${vs_core_version}")
implementation("org.valkyrienskies.core:impl:${vs_core_version}")
implementation fg.deobf("org.valkyrienskies:valkyrienskies-120-forge:${vs2_version}")

// Mekanism
implementation fg.deobf("mekanism:Mekanism:${mekanism_version}")
runtimeOnly fg.deobf("mekanism:Mekanism:${mekanism_version}:generators")
runtimeOnly fg.deobf("mekanism:Mekanism:${mekanism_version}:additions")

// Other
implementation "org.joml:joml:1.10.5"
implementation "com.fasterxml.jackson.core:jackson-annotations:2.13.3"
implementation fg.deobf("cc.tweaked:cc-tweaked-1.20.1-forge:1.117.0")

// JEI (runtime)
runtimeOnly fg.deobf("mezz.jei:jei-${minecraft_version}-forge:${jei_version}")
```

---

## Configuration System

**File**: [KontraptionConfig.kt](file:///workspace/src/main/kotlin/net/illuc/kontraption/config/KontraptionConfig.kt)

### Configuration Categories

#### Thrusters
```kotlin
val ionThrust: CachedDoubleValue           // Ion thruster power (default: 400.0)
val ionConsumption: CachedDoubleValue       // Energy consumption (default: 100.0)
val liquidFuelThrust: CachedDoubleValue    // Liquid fuel thruster power (default: 90.0)
val liquidFuelConsumption: CachedDoubleValue // Fuel consumption (default: 30.0)
val largeIonThrust: CachedDoubleValue      // Large ion ring power (default: 900.0)
val largeIonEnergyConsumption: CachedDoubleValue // Energy usage (default: 300.0)
```

#### Gyroscope
```kotlin
val gyroTorqueStrength: CachedDoubleValue  // Rotation strength (default: 100.0)
```

#### Physics
```kotlin
val thrusterSpeedLimit: CachedDoubleValue  // Max ship speed (default: 20.0)
val dampeningStrength: CachedDoubleValue   // Air resistance (default: 1.0)
val zeroGravity: CachedBooleanValue         // Disable gravity (default: false)
val thrusterResponse: CachedDoubleValue     // PID P gain (default: 1.0)
val dampeningI: CachedDoubleValue          // PID I gain (default: 0.1)
val dampeningD: CachedDoubleValue          // PID D gain (default: 0.5)
```

#### Toolgun
```kotlin
val toolgunActionConsumption: CachedFloatingLongValue // Per-use energy
val toolgunAssembleConsumption: CachedFloatingLongValue // Per-block energy
val toolgunStorage: CachedFloatingLongValue            // Max capacity
val toolgunChargeRate: CachedFloatingLongValue        // Charge speed
```

---

## Network Communication

### Packet Flow
```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Client    │────▶│   Server    │     │   Server    │
│   Player    │     │   Packet    │     │   Ship      │
│   Input     │     │   Handler   │     │   Control   │
└─────────────┘     └─────────────┘     └─────────────┘
      │                    │                    │
      │  PacketKontraption│                    │
      │  .Driving          │                    │
      │───────────────────▶│                    │
      │                    │ Update             │
      │                    │ SeatedPlayer       │
      │                    │ Attachment         │
      │                    │───────────────────▶│
```

### Registered Packets
| Packet | Direction | Purpose |
|--------|-----------|---------|
| `PacketKontraptionDriving` | Client → Server | Player movement/rotation input |
| `PacketKontraptionGuiInteract` | Client → Server | GUI button interactions |
| `PacketKontraptionScreen` | Client → Server | Screen state sync |

---

## Rendering System

### Client Renderers

#### LargeIonRenderer
Renders the large ion ring thruster with custom shaders.

#### PlushieRenderer
Renders decorative plushie blocks.

### Particle Renderers

#### ThrusterParticle
Renders thruster exhaust particles.

#### MuzzleFlashParticle
Renders cannon muzzle flash effects.

### Selection Zone Rendering
**Files**: 
- [Renderer.kt](file:///workspace/src/main/kotlin/net/illuc/kontraption/client/render/Renderer.kt)
- [SelectionZoneRenderer.kt](file:///workspace/src/main/kotlin/net/illuc/kontraption/client/render/SelectionZoneRenderer.kt)

Renders ship assembly selection boxes with the toolgun.

---

## Mixins

**File**: [kontraption.mixins.json](file:///workspace/src/main/resources/kontraption.mixins.json)

### Server-Side Mixins
| Mixin | Target | Purpose |
|-------|--------|---------|
| `MixinLaser` | Laser rendering | Modify laser visuals |
| `MixinRadiationManager` | Radiation system | Radiation handling |
| `MixinSoundHandler` | Sound system | Sound modifications |
| `MixinThermalEvapRender` | Thermal evap rendering | Render changes |
| `MixinTileEntityDigitalMiner` | Digital miner | Integration |

### Client-Side Mixins
| Mixin | Target | Purpose |
|-------|--------|---------|
| `MixinRenderMechanicalPipe` | Pipe rendering | Custom pipe visuals |
| `MixinRenderPressureTube` | Tube rendering | Custom tube visuals |
| `MixinRenderUniversalCable` | Cable rendering | Cable optimization |
| `MixinGameRenderer` | Game renderer | Global render hooks |
| `MixinThreadMinerSearch` | Miner search | Search optimizations |

### Example Mixin
**File**: [MixinTeleporter.java](file:///workspace/src/main/java/net/illuc/kontraption/mixin/MixinTeleporter.java)

```java
@Mixin(RenderTeleporter.class)
public class MixinTeleporter {
    @Redirect(
        method = "render(...)",
        at = @At(value = "INVOKE", target = "getColor()")
    )
    private EnumColor redirectGetColor(TileEntityTeleporter tile) {
        return tile.getColor() == null ? EnumColor.AQUA : tile.getColor();
    }
}
```

---

## Project Build & Run

### Build Requirements
- Java 17
- Gradle 8.x
- Minecraft 1.20.1
- Forge 47.2.0

### Build Commands
```bash
# Build the mod
./gradlew build

# Run client
./gradlew runClient

# Run server
./gradlew runServer

# Generate data (recipes, loot tables, tags)
./gradlew runData
```

### Run Configurations
```groovy
minecraft.runs {
    client {
        // Launches Minecraft with the mod
        property 'forge.enabledGameTestNamespaces', mod_id
    }
    server {
        // Launches dedicated server
        args '--nogui'
    }
    data {
        // Data generation
        args '--mod', mod_id, '--all', '--output', file('src/generated/resources/')
    }
}
```

### Development Setup
1. Clone repository
2. Run `./gradlew setupDecompWorkspace`
3. Import into IDE (IntelliJ IDEA recommended)
4. Run client/server via IDE run configurations

---

## Creative Tab Contents

**File**: [Kontraption.kt](file:///workspace/src/main/kotlin/net/illuc/kontraption/Kontraption.kt) → `createCreativeTab()`

Items available in the Kontraption creative tab:
- `LIGHTWEIGHT_ALLOY` - Crafting material
- `TOOLGUN` - Ship assembly/movement tool
- `LIQUID_FUEL_THRUSTER_CASING/VALVE/EXHAUST` - Multiblock thruster parts
- `ION_THRUSTER` - Single-block thruster
- `SHIP_CONTROL_INTERFACE` - Ship piloting block
- `CANNON` - Gas-powered weapon
- `GYRO` - Rotation control
- `LARGE_ION_THRUSTER_*` - Ring thruster parts
- `OTTER_PLUSHIE`, `COSMIC_PLUSHIE`, `ILLUC_PLUSHIE` - Decorative blocks

---

## Tags and Registries

### Block Tags
- `mineable/pickaxe` - Blocks mineable with pickaxe

### Custom Tags
- `kontraption:femboylist_of_shame.json` - Easter egg tag

### Recipe Data
- Hot gas thruster recipes
- Gyroscope recipes
- Ship control interface recipes
- Toolgun recipe

---

*Document generated from codebase analysis. Last updated: 2026-05-22*
