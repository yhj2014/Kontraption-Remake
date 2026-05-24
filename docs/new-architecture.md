# Kontraption 新项目架构书

> **目标版本**: v0.1.0 (NeoForge 1.21.1 + Sable 1.2.2+ + Create: Aeronautics 1.1.0+)
> **设计日期**: 2026-05-24

---

## 目录

1. [项目概述](#项目概述)
2. [技术栈](#技术栈)
3. [架构设计原则](#架构设计原则)
4. [架构概览](#架构概览)
5. [模块设计](#模块设计)
6. [核心类设计](#核心类设计)
7. [物理引擎抽象层](#物理引擎抽象层)
8. [Create: Aeronautics 集成设计](#create-aeronautics-集成设计)
9. [目录结构建议](#目录结构建议)

---

## 项目概述

### 项目定位

Kontraption 将继续作为一个受《太空工程师》启发的飞船模组，为 Minecraft 提供高级飞船建造和控制功能，但现在：
- 使用 **Sable** 作为物理引擎
- 与 **Create: Aeronautics** 提供可选的兼容性和集成
- 保持对 **Mekanism** 的深度依赖（能源、化学、多方块）

### 核心目标

1. **向后兼容** - 保持与旧版本功能的一致性
2. **性能优化** - 利用 Sable 的性能优势
3. **模块化设计** - 更清晰的代码组织
4. **灵活集成** - 与 Create: Aeronautics 可共存、可集成
5. **可扩展** - 为未来功能添加预留空间

---

## 技术栈

### 核心依赖

| 组件 | 版本要求 | 用途 |
|------|---------|------|
| Minecraft | 1.21.1 | 游戏平台 |
| NeoForge | 最新稳定版 | Mod 加载器 |
| Kotlin | 最新稳定版 | 主要开发语言 |
| Kotlin for Forge | ≥ 5.10.0 | Kotlin 与 NeoForge 集成 |
| Sable | ≥ 1.2.2 | 物理引擎 |
| Mekanism | ≥ 10.7.17.83 | 能源、化学、多方块系统 |
| Create: Aeronautics | ≥ 1.1.0 (可选) | 飞艇系统集成 |
| JOML | 最新版 | 数学库 |

### 可选/运行时依赖

- ComputerCraft (可选，外围设备支持)
- JEI (可选，物品显示)
- 其他模组的可选兼容性

### 构建系统

- **构建工具**: Gradle (Kotlin DSL)
- **Java 版本**: 21 (或 1.21 要求的版本)
- **IDE**: IntelliJ IDEA (推荐)

---

## 架构设计原则

### 1. 物理引擎抽象

设计一个物理引擎抽象层，使得将来再次更换物理引擎（如有必要）更容易：

```kotlin
// 物理引擎抽象接口
interface PhysicsEngineAdapter {
    fun isPhysicsObject(level: Level, pos: BlockPos): Boolean
    fun applyForce(...)
    fun applyTorque(...)
    // ... 其他抽象方法
}

// Sable 实现
class SableEngineAdapter : PhysicsEngineAdapter {
    // Sable 特定实现
}
```

### 2. 模块化

清晰的模块边界：
- `core` - 核心注册、配置、事件
- `physics` - 物理引擎集成
- `blocks` - 方块和方块实体
- `multiblocks` - 多方块结构
- `client` - 客户端渲染和输入
- `integration` - 与其他模组的集成
- `util` - 通用工具

### 3. 配置优先

- 所有可调参数都通过配置系统暴露
- 提供合理的默认值
- 服务器和客户端配置分离

### 4. 渐进式集成

与 Create: Aeronautics 的集成：
- 首先确保完全兼容（不冲突）
- 然后提供可选的增强集成
- 通过配置开关控制集成行为

---

## 架构概览

### 高层架构图

```
┌─────────────────────────────────────────────────────────┐
│                     Kontraption Mod                      │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │   Client     │  │    Common    │  │   Server     │  │
│  │   Module     │  │   Module     │  │   Module     │  │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘  │
│         │                 │                 │         │
│         └─────────────────┼─────────────────┘         │
│                           │                           │
│  ┌────────────────────────┴────────────────────────┐  │
│  │              Physics Engine Abstraction         │  │
│  └────────────────────────┬────────────────────────┘  │
│                           │                           │
│  ┌────────────────────────┼────────────────────────┐  │
│  │         Mekanism       │      Create:           │  │
│  │       Integration      │    Aeronautics         │  │
│  └────────────────────────┼────────────────────────┘  │
│                           │                           │
│  ┌────────────────────────┴────────────────────────┐  │
│  │                  Sable Engine                   │  │
│  └─────────────────────────────────────────────────┘  │
│                                                          │
└─────────────────────────────────────────────────────────┘
                              │
                    ┌─────────┴─────────┐
                    │                   │
              ┌─────▼─────┐     ┌──────▼──────┐
              │ NeoForge  │     │  Minecraft  │
              └───────────┘     └─────────────┘
```

### 依赖图

```
kontraption
├── neoforge
├── kotlinforforge
├── mekanism (必需)
│   ├── 能量系统
│   ├── 气体系统
│   ├── 多方块系统
│   └── 机器系统
├── sable (必需)
│   ├── 物理引擎
│   ├── 物理对象系统
│   └── 力/扭矩 API
├── create-aeronautics (可选)
│   ├── 飞艇系统
│   └── 组件系统
└── cc-tweaked (可选)
```

---

## 模块设计

### 1. Core Module (`core`)

**职责**: 模组初始化、注册、配置、事件总线

**核心类**:
```kotlin
@Mod(Kontraption.MOD_ID)
class Kontraption {
    companion object {
        const val MOD_ID = "kontraption"
        lateinit var instance: Kontraption
    }
    
    init {
        // 初始化
        Config.register()
        Registries.register()
        Events.register()
        Network.register()
    }
}
```

**文件位置**: `src/main/kotlin/net/illuc/kontraption/core/`

---

### 2. Physics Module (`physics`)

**职责**: 物理引擎抽象、飞船控制、力的应用

**子模块**:
- `adapter` - 物理引擎抽象和 Sable 实现
- `control` - 推进器控制、陀螺仪控制、玩家控制
- `attachment` - 物理对象附件数据

**核心设计** (见下节 [物理引擎抽象层](#物理引擎抽象层))

**文件位置**: `src/main/kotlin/net/illuc/kontraption/physics/`

---

### 3. Blocks Module (`blocks`)

**职责**: 方块定义、方块实体、方块状态

**子模块**:
- `basic` - 简单方块
- `propulsion` - 推进器方块
- `control` - 控制方块
- `weapon` - 武器方块
- `decorative` - 装饰方块

**关键方块实体**:
```kotlin
class IonThrusterBlockEntity : 
    ControllableBlockEntity, 
    EnergyStorageBlockEntity, 
    ThrusterProvider 
{
    // 推进器逻辑
}
```

**文件位置**: `src/main/kotlin/net/illuc/kontraption/blocks/`

---

### 4. Multiblocks Module (`multiblocks`)

**职责**: 大型多方块结构

**子模块**:
- `ionring` - 大型离子环推进器
- `liquidfuel` - 液氢推进器
- `railgun` - 电磁炮
- `common` - 通用多方块工具

**文件位置**: `src/main/kotlin/net/illuc/kontraption/multiblocks/`

---

### 5. Client Module (`client`)

**职责**: 客户端渲染、输入、粒子

**子模块**:
- `render` - 方块实体渲染、选择区域渲染
- `particle` - 粒子类型和渲染
- `gui` - 屏幕和界面
- `input` - 按键绑定、鼠标处理
- `event` - 客户端事件

**文件位置**: `src/main/kotlin/net/illuc/kontraption/client/`

---

### 6. Integration Module (`integration`)

**职责**: 与其他模组的集成

**子模块**:
- `mekanism` - Mekanism 集成（深度）
- `create_aeronautics` - Create: Aeronautics 集成
- `computercraft` - ComputerCraft 外围设备
- `jei` - JEI 集成

**文件位置**: `src/main/kotlin/net/illuc/kontraption/integration/`

---

### 7. Util Module (`util`)

**职责**: 通用工具、数学、扩展函数

**子模块**:
- `math` - 数学工具
- `extensions` - Kotlin 扩展函数
- `collections` - 集合工具
- `nbt` - NBT 工具

**文件位置**: `src/main/kotlin/net/illuc/kontraption/util/`

---

## 核心类设计

### 1. 物理引擎抽象层

**设计目的**: 隔离物理引擎实现，便于未来变更

```kotlin
// 物理对象抽象
interface IPhysicsObject {
    val position: Vec3
    val rotation: Quaterniond
    val velocity: Vec3
    val angularVelocity: Vec3
    val mass: Double
    
    fun applyForce(force: Vec3, origin: Vec3? = null)
    fun applyTorque(torque: Vec3)
    fun getBlockEntity(pos: BlockPos): BlockEntity?
    // ... 其他方法
}

// 物理引擎适配器接口
interface IPhysicsEngineAdapter {
    fun isPhysicsObject(level: Level, pos: BlockPos): Boolean
    fun getPhysicsObject(level: Level, pos: BlockPos): IPhysicsObject?
    fun createPhysicsObject(...)
    fun destroyPhysicsObject(...)
    // ... 其他方法
}

// Sable 实现
class SableEngineAdapter : IPhysicsEngineAdapter {
    override fun isPhysicsObject(level: Level, pos: BlockPos): Boolean {
        // Sable 特定实现
    }
    
    // ... 其他方法实现
}
```

**使用**:
```kotlin
// 在 Kontraption 主类中
private val adapter: IPhysicsEngineAdapter = SableEngineAdapter()

fun getPhysicsAdapter(): IPhysicsEngineAdapter = adapter
```

---

### 2. 推进器系统

**保持接口，但重写实现**:

```kotlin
interface ThrusterProvider {
    val isEnabled: Boolean
    val thrustDirection: Direction
    val maxThrust: Double
    var currentThrust: Double
    
    fun setThrust(thrust: Double)
    fun canWork(): Boolean
}

class IonThrusterBlockEntity : 
    ControllableBlockEntity(), 
    ThrusterProvider, 
    EnergyStorageBlockEntity 
{
    // 实现...
}
```

**推进器控制器**:
```kotlin
class ThrusterController(val physicsObject: IPhysicsObject) {
    private val thrusters = mutableListOf<ThrusterProvider>()
    
    fun register(thruster: ThrusterProvider)
    fun unregister(thruster: ThrusterProvider)
    
    fun applyThrust(targetVelocity: Vec3, targetAngularVelocity: Vec3) {
        // 计算所需的力和扭矩
        // 分配给各个推进器
    }
    
    fun tick() {
        // 每个 tick 的处理
    }
}
```

---

### 3. 陀螺仪系统

```kotlin
interface GyroscopeProvider {
    val stabilizationStrength: Double
}

class GyroscopeController(val physicsObject: IPhysicsObject) {
    private val gyroscopes = mutableListOf<GyroscopeProvider>()
    
    fun register(gyro: GyroscopeProvider)
    fun unregister(gyro: GyroscopeProvider)
    
    fun stabilize(targetRotation: Quaterniond) {
        // 计算所需扭矩
        // 应用到物理对象
    }
}
```

---

### 4. 飞船控制界面

```kotlin
class ShipControllerBlockEntity : 
    ControllableBlockEntity(),
    PeripheralProvider // ComputerCraft
{
    var seatedPlayer: Player? = null
    
    fun onPlayerInteract(player: Player) {
        // 处理玩家交互
    }
    
    fun tick() {
        // 处理控制
    }
}
```

---

### 5. 工具枪

保持功能，但重写物理交互部分:

```kotlin
class ToolgunItem : EnergizedItem() {
    enum class Mode {
        ASSEMBLE, MOVE, LOCK, PUSH, ROTATE, WELD
    }
    
    fun onUse(...) {
        // 处理使用
    }
}
```

---

## 物理引擎抽象层

### 详细设计

#### 1. 物理对象接口

```kotlin
/**
 * 表示一个物理对象（飞船）的抽象接口
 */
interface IPhysicsObject {
    /**
     * 世界位置
     */
    val position: Vec3
    
    /**
     * 旋转（四元数）
     */
    val rotation: Quaterniond
    
    /**
     * 线速度
     */
    val velocity: Vec3
    
    /**
     * 角速度
     */
    val angularVelocity: Vec3
    
    /**
     * 质量
     */
    val mass: Double
    
    /**
     * 转动惯量
     */
    val inertia: Vec3
    
    /**
     * 施加力（相对于世界坐标系）
     */
    fun applyForce(force: Vec3, origin: Vec3? = null)
    
    /**
     * 施加扭矩（相对于世界坐标系）
     */
    fun applyTorque(torque: Vec3)
    
    /**
     * 施加力（相对于物体局部坐标系）
     */
    fun applyLocalForce(force: Vec3, origin: Vec3? = null)
    
    /**
     * 施加扭矩（相对于物体局部坐标系）
     */
    fun applyLocalTorque(torque: Vec3)
    
    /**
     * 获取方块实体（相对于物体局部坐标）
     */
    fun getBlockEntity(localPos: BlockPos): BlockEntity?
    
    /**
     * 获取方块状态（相对于物体局部坐标）
     */
    fun getBlockState(localPos: BlockPos): BlockState
    
    /**
     * 遍历所有方块
     */
    fun forEachBlock(action: (BlockPos, BlockState) -> Unit)
    
    /**
     * 设置速度
     */
    fun setVelocity(velocity: Vec3)
    
    /**
     * 设置角速度
     */
    fun setAngularVelocity(velocity: Vec3)
    
    /**
     * 将局部坐标转换为世界坐标
     */
    fun localToWorld(pos: Vec3): Vec3
    
    /**
     * 将世界坐标转换为局部坐标
     */
    fun worldToLocal(pos: Vec3): Vec3
    
    /**
     * 将局部向量转换为世界向量
     */
    fun localToWorldDirection(dir: Vec3): Vec3
    
    /**
     * 将世界向量转换为局部向量
     */
    fun worldToLocalDirection(dir: Vec3): Vec3
}
```

#### 2. 物理引擎适配器接口

```kotlin
/**
 * 物理引擎适配器，隔离具体实现
 */
interface IPhysicsEngineAdapter {
    /**
     * 检查某个位置是否属于物理对象
     */
    fun isPhysicsObject(level: Level, pos: BlockPos): Boolean
    
    /**
     * 获取包含某个位置的物理对象
     */
    fun getPhysicsObject(level: Level, pos: BlockPos): IPhysicsObject?
    
    /**
     * 获取世界中的所有物理对象
     */
    fun getAllPhysicsObjects(level: Level): List<IPhysicsObject>
    
    /**
     * 从方块创建物理对象
     */
    fun assembleToShip(
        level: ServerLevel,
        centerPos: BlockPos,
        includedPositions: Set<BlockPos>
    ): IPhysicsObject?
    
    /**
     * 将物理对象移到新位置
     */
    fun teleport(
        physicsObject: IPhysicsObject,
        newPos: Vec3,
        newRotation: Quaterniond? = null
    )
    
    /**
     * 附加自定义数据到物理对象
     */
    fun <T> attachData(physicsObject: IPhysicsObject, key: ResourceLocation, data: T)
    
    /**
     * 获取附加数据
     */
    fun <T> getAttachedData(physicsObject: IPhysicsObject, key: ResourceLocation): T?
    
    /**
     * 移除附加数据
     */
    fun removeAttachedData(physicsObject: IPhysicsObject, key: ResourceLocation)
    
    /**
     * 注册物理 tick 监听器
     */
    fun registerPhysicsTickListener(listener: PhysicsTickListener)
    
    /**
     * 取消注册物理 tick 监听器
     */
    fun unregisterPhysicsTickListener(listener: PhysicsTickListener)
}

/**
 * 物理 tick 监听器
 */
interface PhysicsTickListener {
    fun onPrePhysicsTick(physicsObject: IPhysicsObject, deltaTime: Float)
    fun onPostPhysicsTick(physicsObject: IPhysicsObject, deltaTime: Float)
}
```

#### 3. Sable 适配器实现示例

**注意**: 实际实现取决于 Sable 的具体 API，这里是概念设计

```kotlin
class SableEngineAdapter : IPhysicsEngineAdapter {
    override fun isPhysicsObject(level: Level, pos: BlockPos): Boolean {
        // Sable 特定检查
        return SableApi.isShipPart(level, pos)
    }
    
    override fun getPhysicsObject(level: Level, pos: BlockPos): IPhysicsObject? {
        val sableShip = SableApi.getShipManaging(level, pos) ?: return null
        return SablePhysicsObject(sableShip)
    }
    
    // ... 其他方法
}

/**
 * Sable 物理对象包装
 */
class SablePhysicsObject(private val sableShip: SableShip) : IPhysicsObject {
    override val position: Vec3
        get() = sableShip.position.toMinecraft()
    
    override val rotation: Quaterniond
        get() = sableShip.rotation.toJOML()
    
    // ... 其他实现
}
```

---

## Create: Aeronautics 集成设计

### 集成策略

我们采用 **三层集成策略**:

1. **兼容性层** (必须) - 确保两者不冲突
2. **可选功能层** (推荐) - 提供可选的交互功能
3. **深度集成层** (可选) - 高级集成功能

### 1. 兼容性层

目标：两者共存，不互相干扰

- ID 冲突检查
- 方块位置冲突处理
- 事件冲突处理
- 配置选项: `integration.create_aeronautics.compatibility_mode`

### 2. 可选功能层

目标：提供有用的交互功能

- **互操作方块**
  - `KontraptionToAeronauticsAdapter` - 将 Kontraption 能量转换为 Create 旋转动力
  - `AeronauticsToKontraptionAdapter` - 将 Create 旋转动力转换为 Kontraption 推进力
  
- **共享控制界面**
  - 支持从 Create 的控制界面控制 Kontraption 飞船
  - 支持从 Kontraption 的控制界面控制 Create 的飞艇
  
- **配置选项**
  ```kotlin
  object CreateAeronauticsConfig {
      val enableIntegration: Boolean
      val enableEnergyConversion: Boolean
      val enableSharedControls: Boolean
  }
  ```

### 3. 深度集成层 (可选，视需求而定)

- **合并物理系统** - 如果 Sable 和 Create: Aeronautics 能共享物理
- **混合飞船** - 同时包含两种系统组件的飞船
- **统一控制界面**

### 集成模块结构

```
integration/create_aeronautics/
├── compatibility/       # 兼容性层
├── adapter/             # 功能适配器
├── blocks/              # 互操作方块
└── config/              # 集成配置
```

---

## 目录结构建议

### 完整目录树

```
kontraption/
├── docs/                                    # 文档
│   ├── original-architecture.md
│   ├── migration-plan.md
│   └── new-architecture.md
├── src/
│   ├── main/
│   │   ├── java/net/illuc/kontraption/       # 仅用于遗留 Java 代码（如需要）
│   │   │   └── [逐步迁移到 Kotlin]
│   │   ├── kotlin/net/illuc/kontraption/
│   │   │   ├── core/                         # 核心模块
│   │   │   │   ├── Kontraption.kt
│   │   │   │   ├── Config.kt
│   │   │   │   ├── Registries.kt
│   │   │   │   ├── Events.kt
│   │   │   │   ├── Network.kt
│   │   │   │   └── Constants.kt
│   │   │   ├── physics/                      # 物理模块
│   │   │   │   ├── adapter/
│   │   │   │   │   ├── IPhysicsEngineAdapter.kt
│   │   │   │   │   ├── IPhysicsObject.kt
│   │   │   │   │   └── SableEngineAdapter.kt
│   │   │   │   ├── control/
│   │   │   │   │   ├── ThrusterController.kt
│   │   │   │   │   ├── GyroscopeController.kt
│   │   │   │   │   └── ShipControl.kt
│   │   │   │   └── attachment/
│   │   │   │       └── KontraptionShipData.kt
│   │   │   ├── blocks/                       # 方块模块
│   │   │   │   ├── basic/
│   │   │   │   ├── propulsion/
│   │   │   │   ├── control/
│   │   │   │   ├── weapon/
│   │   │   │   └── decorative/
│   │   │   ├── blockentities/                # 方块实体
│   │   │   │   ├── IonThrusterBlockEntity.kt
│   │   │   │   ├── ShipControllerBlockEntity.kt
│   │   │   │   ├── GyroscopeBlockEntity.kt
│   │   │   │   ├── CannonBlockEntity.kt
│   │   │   │   └── ...
│   │   │   ├── multiblocks/                  # 多方块模块
│   │   │   │   ├── ionring/
│   │   │   │   ├── liquidfuel/
│   │   │   │   ├── railgun/
│   │   │   │   └── common/
│   │   │   ├── client/                       # 客户端模块
│   │   │   │   ├── render/
│   │   │   │   ├── particle/
│   │   │   │   ├── gui/
│   │   │   │   ├── input/
│   │   │   │   └── event/
│   │   │   ├── network/                      # 网络模块
│   │   │   │   ├── packets/
│   │   │   │   └── NetworkHandler.kt
│   │   │   ├── integration/                  # 集成模块
│   │   │   │   ├── mekanism/
│   │   │   │   ├── create_aeronautics/
│   │   │   │   │   ├── compatibility/
│   │   │   │   │   ├── adapter/
│   │   │   │   │   ├── blocks/
│   │   │   │   │   └── config/
│   │   │   │   ├── computercraft/
│   │   │   │   └── jei/
│   │   │   ├── item/                         # 物品模块
│   │   │   │   └── ToolgunItem.kt
│   │   │   └── util/                         # 工具模块
│   │   │       ├── math/
│   │   │       ├── extensions/
│   │   │       ├── collections/
│   │   │       └── nbt/
│   │   └── resources/
│   │       ├── assets/kontraption/
│   │       ├── data/kontraption/
│   │       ├── META-INF/
│   │       ├── kontraption.mixins.json
│   │       └── pack.mcmeta
│   └── test/
├── gradle/
├── build.gradle.kts                          # Kotlin DSL
├── settings.gradle.kts
├── gradle.properties
└── [其他文件]
```

---

## 配置设计

### 配置文件结构

```kotlin
// 通用配置
object KontraptionConfig {
    // 物理配置
    object Physics {
        val maxSpeed: Double
        val dampeningStrength: Double
        val thrusterResponse: Double
        val zeroGravity: Boolean
    }
    
    // 推进器配置
    object Propulsion {
        val ionThrust: Double
        val ionConsumption: Double
        val liquidFuelThrust: Double
        val liquidFuelConsumption: Double
        val largeIonThrust: Double
        val largeIonConsumption: Double
    }
    
    // 集成配置
    object Integration {
        object CreateAeronautics {
            val enableIntegration: Boolean
            val enableEnergyConversion: Boolean
            val enableSharedControls: Boolean
        }
    }
}
```

---

## 迁移检查清单

在开始编码前，确保：

- [ ] Sable API 文档已研究
- [ ] Create: Aeronautics API 文档已研究
- [ ] NeoForge 1.21.1 变更已了解
- [ ] Mekanism 10.7.17.83 变更已了解
- [ ] 团队对新架构达成共识
- [ ] 备份分支已创建

---

*文档版本: v1.0.0*
