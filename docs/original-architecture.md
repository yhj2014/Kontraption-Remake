# Kontraption 原架构书

> **项目状态**: v0.0.5 (Forge 1.20.1 + Valkyrien Skies 2 + Mekanism)

## 目录

1. [项目概述](#项目概述)
2. [技术栈](#技术栈)
3. [架构概览](#架构概览)
4. [模块职责](#模块职责)
5. [核心类与函数](#核心类与函数)
6. [依赖关系](#依赖关系)
7. [系统流程](#系统流程)

---

## 项目概述

**Kontraption** 是一个受《太空工程师》启发的 Minecraft 模组，使用 Valkyrien Skies 2 实现物理驱动的飞船和飞艇系统。它依赖 Mekanism 提供能量和化学系统支持，并与 ComputerCraft 集成以支持 Lua 脚本控制。

### 主要功能

- **推进器系统**: 离子推进器和液氢推进器
- **大型多方块结构**: 环形离子推进器、液氢推进器、电磁炮
- **飞船控制**: 键盘控制、鼠标控制、ComputerCraft 外围设备
- **武器系统**: 气体驱动火炮
- **工具系统**: 飞船组装和移动工具
- **装饰块**: 水獭、宇宙、illuc 毛绒方块

---

## 技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| Minecraft | Forge | 1.20.1 |
| Mod加载器 | Forge | 47.2.0 |
| Kotlin | Kotlin for Forge | 4.11.0 |
| 物理引擎 | Valkyrien Skies 2 | 2.4.6+ |
| 核心模组 | Mekanism | 10.4.0+ |
| 可选模组 | ComputerCraft | 1.117.0+ |
| 数学库 | JOML | 1.10.5 |

### 构建系统

- 构建工具: Gradle 8.x
- Java 版本: 17
- IDE 支持: IntelliJ IDEA / Eclipse

---

## 架构概览

### 目录结构

```
kontraption/
├── src/main/
│   ├── java/net/illuc/kontraption/
│   │   ├── blockType/          # 方块类型定义
│   │   ├── client/             # Java 客户端代码
│   │   ├── mixin/              # Mixin 注入
│   │   ├── network/            # Java 网络数据包
│   │   ├── particles/          # 粒子数据
│   │   ├── util/               # 通用工具类
│   │   └── [核心文件]          # GlobalRegistry, Blocks等
│   └── kotlin/net/illuc/kontraption/
│       ├── blockEntities/      # 方块实体
│       ├── blocks/             # 方块类
│       ├── ship/               # 飞船控制模块
│       ├── multiblocks/        # 多方块结构
│       ├── network/            # 网络数据包
│       ├── client/             # 客户端代码
│       ├── config/             # 配置系统
│       ├── container/          # 容器/界面
│       ├── controls/           # 玩家控制
│       ├── entity/             # 实体
│       ├── item/               # 物品
│       ├── peripherals/        # 外围设备
│       ├── renderers/          # 渲染器
│       └── util/               # 工具类
└── src/main/resources/
    ├── assets/kontraption/     # 资源文件
    ├── data/kontraption/       # 数据文件
    └── [配置文件]
```

### 核心设计模式

1. **注册系统**: 集中式的 DeferredRegister 注册
2. **方块实体基类**: `ControllableTileEntity` 实现 `IControllable`
3. **接口驱动**: `ThrusterInterface` 统一所有推进器
4. **飞船附件**: Valkyrien Skies 的 `ShipAttachment` 机制
5. **物理监听器**: `ShipPhysicsListener` 处理物理事件

---

## 模块职责

### 1. 核心模块

**文件**: [Kontraption.kt](file:///workspace/src/main/kotlin/net/illuc/kontraption/Kontraption.kt)

主模组类，负责：
- 模组初始化
- 注册表注册
- 网络处理
- 事件监听
- 配置加载

### 2. 方块模块

**文件**: [KontraptionBlocks.java](file:///workspace/src/main/java/net/illuc/kontraption/KontraptionBlocks.java)、[GlobalRegistry.java](file:///workspace/src/main/java/net/illuc/kontraption/GlobalRegistry.java)

方块定义和注册，包含：
- 推进器方块
- 控制界面方块
- 武器方块
- 装饰方块

### 3. 方块实体模块

基类: [ControllableTileEntity.kt](file:///workspace/src/main/kotlin/net/illuc/kontraption/util/ControllableTileEntity.kt)

**关键方块实体**:
- `TileEntityIonThruster`: 离子推进器
- `TileEntityShipControlInterface`: 飞船控制界面
- `TileEntityCannon`: 火炮
- `TileEntityGyro`: 陀螺仪
- `TileEntityKey`: 按键块
- 大型离子环多方块实体

### 4. 飞船控制模块

**核心类**:
- [ThrusterInterface.kt](file:///workspace/src/main/kotlin/net/illuc/kontraption/ThrusterInterface.kt) - 推进器接口
- [KontraptionThrusterControl.kt](file:///workspace/src/main/kotlin/net/illuc/kontraption/ship/KontraptionThrusterControl.kt) - 推进器控制
- [KontraptionGyroControl.kt](file:///workspace/src/main/kotlin/net/illuc/kontraption/ship/KontraptionGyroControl.kt) - 陀螺仪控制
- [KontraptionSeatedControllingPlayer.kt](file:///workspace/src/main/kotlin/net/illuc/kontraption/controls/KontraptionSeatedControllingPlayer.kt) - 玩家控制

### 5. 网络通信模块

**核心类**:
- [KontraptionPacketHandler.kt](file:///workspace/src/main/kotlin/net/illuc/kontraption/network/KontraptionPacketHandler.kt) - 数据包处理器
- `PacketKontraptionDriving` - 驾驶数据包
- `PacketKontraptionScreen` - 屏幕数据包
- `PacketKontraptionGuiInteract` - GUI 交互

### 6. 客户端渲染模块

- 粒子效果渲染
- 选择区域渲染
- 自定义方块渲染
- 大型结构渲染

### 7. 配置模块

**文件**: [KontraptionConfig.kt](file:///workspace/src/main/kotlin/net/illuc/kontraption/config/KontraptionConfig.kt)

配置项:
- 推进器参数 (功率、消耗)
- 陀螺仪参数 (扭矩)
- 物理参数 (速度限制、PID 系数)
- 工具枪参数 (能量消耗)

---

## 核心类与函数

### ThrusterInterface

```kotlin
interface ThrusterInterface {
    val thrusterLevel: Level?
    var enabled: Boolean
    val worldPosition: BlockPos?
    val forceDirection: Direction
    val thrusterPower: Double
    var currentThrust: Double
    
    fun enable(level: ServerLevel?, bpos: BlockPos?)
    fun disable()
}
```

### KontraptionThrusterControl

```kotlin
class KontraptionThrusterControl : ShipPhysicsListener {
    private val thrusters = CopyOnWriteArrayList<Thruster>()
    
    fun physTick(physShip: PhysShip, physLevel: PhysLevel) {
        // 1. 根据玩家输入计算目标速度
        // 2. 计算速度误差
        // 3. 应用 PID 响应
        // 4. 计算所需力
        // 5. 向对应方向的推进器施加力
    }
}
```

### ItemToolgun

多模式工具，支持:
- `ASSEMBLE`: 选择并组装飞船
- `MOVE`: 传送现有飞船
- `LOCK`: 锁定飞船位置
- `PUSH`: 推动飞船
- `ROTATE`: 旋转飞船
- `WELD`: 焊接方块

---

## 依赖关系

### 主要依赖树

```
kontraption
├── forge
├── mekanism (必需)
│   ├── 能量容器
│   ├── 气体系统
│   ├── 多方块系统
│   └── 机器系统
├── valkyrienskies (必需)
│   ├── vs-core (API/Impl/Internal/Util)
│   ├── 物理引擎
│   ├── 飞船系统
│   └── 附件系统
├── kotlinforforge
├── cc-tweaked (可选)
└── jei (可选)
```

### 关键接口集成点

| 外部系统 | 集成方式 | 位置 |
|---------|---------|------|
| Mekanism 能量 | 继承 Mekanism 方块/方块实体 | `ControllableTileEntity` |
| Mekanism 多方块 | Mekanism 多方块系统 | `LiquidFuelThrusterMultiblockData` |
| Valkyrien Skies | `ShipAttachment`, `ShipPhysicsListener` | `ship/` 包 |
| ComputerCraft | `Peripheral` 接口 | `peripherals/ShipControlInterfacePeripheral.kt` |

---

## 系统流程

### 1. 飞船控制流程

```
玩家输入
  ↓
KontraptionShipMountingEntity
  ↓
PacketKontraptionDriving
  ↓
服务器接收 → KontraptionSeatedControllingPlayer
  ↓
KontraptionThrusterControl / KontraptionGyroControl
  ↓
Valkyrien Skies PhysShip
  ↓
物理引擎应用力
  ↓
飞船移动/旋转
```

### 2. 推进器物理流程

```
物理 tick (PhysTick)
  ↓
KontraptionThrusterControl#physTick()
  ├─ 目标速度计算
  ├─ 速度误差计算
  ├─ 类 PID 响应
  ├─ 计算所需力
  └─ 向推进器分配推力
       ↓
    ThrusterInterface#powered = true
    ThrusterInterface#currentThrust = applied
       ↓
    能量消耗 (如果是离子) / 气体消耗 (如果是液氢)
       ↓
    粒子效果
```

### 3. 工具枪组装流程

```
玩家选择区域 (ASSEMBLE 模式)
  ↓
创建选择区域渲染 (客户端)
  ↓
玩家确认组装
  ↓
能量检查
  ↓
KontraptionVSUtils#createNewShipWithBlocks()
  ↓
Valkyrien Skies 组装系统
  ↓
创建飞船实体
```

---

## Mixin 注入点

### 客户端 Mixins

| Mixin | 目标 | 用途 |
|-------|------|------|
| `MixinGameRenderer` | 游戏渲染 | 全局钩子 |
| `MixinRenderMechanicalPipe` | 机械管道 | 视觉优化 |
| `MixinRenderPressureTube` | 压力管道 | 视觉优化 |
| `MixinRenderUniversalCable` | 线缆 | 视觉优化 |

### 服务端/通用 Mixins

| Mixin | 目标 | 用途 |
|-------|------|------|
| `MixinLaser` | 激光 | 视觉效果 |
| `MixinRadiationManager` | 辐射 | 系统集成 |
| `MixinSoundHandler` | 声音 | 音效处理 |
| `MixinTeleporter` | 传送门 | 视觉效果 |
| `MixinThermalEvapRender` | 蒸发器 | 渲染 |
| `MixinThreadMinerSearch` | 采矿器 | 性能优化 |
| `MixinTileEntityDigitalMiner` | 数字采矿器 | 集成 |

---

*文档版本: v0.0.5-1.20.1-Forge*
