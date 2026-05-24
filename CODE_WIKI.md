# Kontraption 代码百科

> **灵感来自 Space Engineers 的 Mekanism 模组，使用 Valkyrien Skies 2 实现**

## 目录

1. [项目概述](#项目概述)
2. [架构概览](#架构概览)
3. [模块职责](#模块职责)
4. [核心类与函数](#核心类与函数)
5. [依赖关系](#依赖关系)
6. [配置系统](#配置系统)
7. [网络通信](#网络通信)
8. [渲染系统](#渲染系统)
9. [Mixin 注入](#mixin-注入)
10. [项目构建与运行](#项目构建与运行)

---

## 项目概述

**Kontraption** 是一个基于 Forge 1.20.1 的 Minecraft 模组，添加了飞船/飞行器相关的方块和多方块结构。集成了：
- **Valkyrien Skies 2** (v2.4.6+) - 物理引擎驱动的飞船移动
- **Mekanism** (v10.4.0+) - 能量和化学系统

### 主要功能
- **离子推进器** - 消耗能量的单方块推进器
- **液氢推进器** - 使用氢气的多方块推进器 (3x3x3 到 17x17x17)
- **大型离子环推进器** - 环形的能量驱动多方块推进器
- **飞船控制界面** - 玩家驾驶飞船的控制面板
- **陀螺仪** - 飞船旋转稳定和控制
- **工具枪** - 飞船组装和移动工具
- **电磁炮** - 多方块武器系统 (开发中)

### 版本信息
```
minecraft_version=1.20.1
forge_version=47.2.0
mod_version=0.0.5
```

---

## 架构概览

```
kontraption/
├── src/main/
│   ├── java/net/illuc/kontraption/
│   │   ├── Kontraption.kt                    # 主模组入口
│   │   ├── GlobalRegistry.java               # 注册表持有者
│   │   ├── KontraptionBlocks.java            # 方块定义
│   │   ├── KontraptionTileEntityTypes.java   # 方块实体类型
│   │   ├── KontraptionBlockTypes.java        # 方块类型配置
│   │   ├── mixin/                            # Mixin 注入器
│   │   └── util/
│   │       └── KontraptionVSUtils.java       # Valkyrien Skies 工具
│   │
│   └── kotlin/net/illuc/kontraption/
│       ├── blockEntities/                    # 方块实体实现
│       ├── blocks/                           # 方块类
│       ├── ship/                             # 飞船物理控制器
│       ├── multiblocks/                      # 多方块结构
│       ├── network/                          # 数据包处理
│       ├── client/                           # 客户端渲染
│       ├── config/                           # 配置系统
│       └── util/                             # 工具类
```

---

## 模块职责

### 1. 核心模块 (`Kontraption.kt`)

**文件**: `src/main/kotlin/net/illuc/kontraption/Kontraption.kt`

使用 `@Mod(Kontraption.MODID)` 注解的主模组入口类。

**主要职责:**
- 注册所有方块、物品、方块实体和创造模式标签
- 初始化网络数据包处理器
- 注册 Valkyrien Skies 飞船附件
- 客户端渲染器注册
- 命令注册

**核心方法:**
| 方法 | 用途 |
|------|------|
| `commonSetup()` | 初始化多方块、标签、数据包、VS 附件 |
| `clientSetup()` | 设置方块渲染层 |
| `createCreativeTab()` | 定义模组创造模式标签内容 |

---

### 2. 方块注册

**核心文件**: 
- `KontraptionBlocks.java` - 方块定义
- `GlobalRegistry.java` - 注册表持有者

**核心方块:**
| 方块 | 用途 |
|------|------|
| `ION_THRUSTER` | 能量驱动的推进器 |
| `SHIP_CONTROL_INTERFACE` | 玩家驾驶界面 |
| `GYRO` | 旋转稳定 |
| `LIQUID_FUEL_THRUSTER_*` | 多方块液氢推进器 |
| `CANNON` | 气体驱动的投射物武器 |
| `LARGE_ION_THRUSTER_*` | 环形多方块推进器 |
| `RAILGUN_*` | 电磁炮组件 |
| `*_PLUSHIE` | 装饰方块 |

---

### 3. 方块实体模块

**基类**: `ControllableTileEntity`

所有可控制方块的抽象基类，继承 Mekanism 的 `TileEntityMekanism` 并实现 `IControllable`。

#### TileEntityIonThruster
能量驱动的单方块推进器。

**核心属性:**
```kotlin
override val thrusterPower: Double    // 推力功率
override var currentThrust: Double    // 当前推力
override var powered: Boolean         // 是否通电
```

#### TileEntityShipControlInterface
玩家驾驶飞船的中心控制枢纽。

**核心功能:**
- 生成玩家座位实体 (`KontraptionShipMountingEntity`)
- 处理玩家输入到飞船坐标的转换
- 提供 ComputerCraft 外围设备集成
- 通过飞船附件控制推进器和陀螺仪

**核心方法:**
```kotlin
fun spawnSeat(...)      // 创建玩家座位实体
fun tick()             // 处理玩家输入，更新飞船控制
fun sit(player, force) // 将玩家安装到座位
fun toggleDampener()   // 切换惯性阻尼器
```

#### TileEntityCannon
使用 Mekanism 氢气的气体驱动投射物武器。

**核心属性:**
```kotlin
var inputTank: IGasTank?       // 氢气储罐
var cooldown: Int = 20         // 射击间隔
var ship: Ship?                // 父飞船引用
```

---

### 4. 飞船物理控制模块

**基类接口**: `ThrusterInterface`

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
实现 `ShipPhysicsListener` 的飞船附件，用于推进器物理。

**物理算法:**
1. 根据玩家输入计算目标速度
2. 计算速度误差: `velError = targetVelocity - currentVelocity`
3. 应用类 PID 响应: `desiredAccel = velError * response`
4. 计算所需力: `desiredForce = desiredAccel * mass`
5. 向所需力方向对齐的推进器施加力

#### KontraptionGyroControl
用于旋转控制的飞船附件，使用四元数进行旋转计算。

---

### 5. 多方块系统

#### 大型离子环
环形的能量驱动推进器 (5x5 到 15x15 占地面积，2 格高)。

**结构验证:**
- 必须是正方形 (lengthX == lengthZ)
- 大小范围: 5-15 格
- 高度: 正好 2 格
- 必须包含控制器方块

#### 液氢推进器
氢气驱动的多方块推进器 (3x3x3 到 17x17x17)。

---

### 6. 网络通信模块

**数据包列表:**
| 数据包 | 方向 | 用途 |
|--------|------|------|
| `PacketKontraptionDriving` | 客户端 → 服务器 | 玩家移动/旋转输入 |
| `PacketKontraptionGuiInteract` | 客户端 → 服务器 | GUI 按钮交互 |
| `PacketKontraptionScreen` | 客户端 → 服务器 | 屏幕状态同步 |

---

### 7. 客户端模块

**客户端渲染:**
- `KontraptionClientTickHandler` - 客户端 tick 处理
- `ClientRuntimeEvents` - 世界渲染钩子
- `ThrusterParticle` - 推进器粒子渲染
- `MuzzleFlashParticle` - 炮口火焰渲染
- `SelectionZoneRenderer` - 工具枪选择区域渲染

---

### 8. 工具模块

**Valkyrien Skies 集成:**
```java
public class KontraptionVSUtils {
    public static LoadedShip getShipManagingPos(Level level, BlockPos blockPos)
    public static LoadedServerShip getShipObjectManagingPos(ServerLevel level, BlockPos blockPos)
    public static void createNewShipWithBlocks(BlockPos pos, DenseBlockPosSet set, ServerLevel level)
}
```

---

## 核心类与函数

### ItemToolgun
多模式飞船组装和移动工具。

**模式:**
- `ASSEMBLE` - 选择并从方块创建飞船
- `MOVE` - 传送现有飞船
- `LOCK` - 锁定飞船位置 (开发中)
- `PUSH` - 推动飞船 (开发中)
- `ROTATE` - 旋转飞船 (开发中)
- `WELD` - 焊接方块 (开发中)

### ShipControlInterfacePeripheral
用于 Lua 飞船控制的 ComputerCraft 外围设备。

**可用方法:**
```kotlin
getRotation()      // 获取旋转四元数
getMovement()      // 获取移动目标
getPosition()      // 获取飞船位置
getWeight()       // 获取飞船质量
getVelocity()      // 获取飞船速度
setMovement(x,y,z)      // 设置移动目标
setRotation(x,y,z,w)    // 设置旋转
rotateAlongAxis(x,y,z)   // 沿轴旋转
preciseThrustImpulse(x,y,z) // 精确推力
```

---

## 依赖关系

```
Kontraption
    ├── Valkyrien Skies 2 (飞船物理)
    │       ├── vsCore API
    │       ├── PhysShip
    │       └── ShipAttachments
    │
    ├── Mekanism (能量、化学系统)
    │       ├── 能量容器
    │       ├── 化学储罐
    │       └── 机器系统
    │
    └── ComputerCraft (可选)
            └── 外围设备 API
```

**核心依赖:**
```groovy
// Valkyrien Skies 2
implementation("org.valkyrienskies.core:api:${vs_core_version}")
implementation fg.deobf("org.valkyrienskies:valkyrienskies-120-forge:${vs2_version}")

// Mekanism
implementation fg.deobf("mekanism:Mekanism:${mekanism_version}")

// ComputerCraft
implementation fg.deobf("cc.tweaked:cc-tweaked-1.20.1-forge:1.117.0")
```

---

## 配置系统

### 推进器配置
```kotlin
ionThrust = 400.0                    // 离子推进器功率
ionConsumption = 100.0               // 离子能量消耗
liquidFuelThrust = 90.0             // 液燃推进器功率
liquidFuelConsumption = 30.0        // 液燃燃料消耗
largeIonThrust = 900.0              // 大型离子环功率
largeIonEnergyConsumption = 300.0    // 大型离子能量消耗
```

### 陀螺仪配置
```kotlin
gyroTorqueStrength = 100.0          // 旋转强度
```

### 物理配置 (PID)
```kotlin
thrusterSpeedLimit = 20.0            // 最大速度
dampeningStrength = 1.0              // 空气阻力
zeroGravity = false                  // 零重力模式
thrusterResponse = 1.0               // PID P 值
dampeningI = 0.1                     // PID I 值
dampeningD = 0.5                     // PID D 值
```

### 工具枪配置
```kotlin
toolgunActionConsumption = 1000      // 每次使用能量
toolgunAssembleConsumption = 1000    // 每方块能量
toolgunStorage = 20000000             // 最大容量
toolgunChargeRate = 100000           // 充电速度
```

---

## 网络通信

### 数据包流程
```
玩家输入 → PacketKontraptionDriving → 服务器 → 更新 SeatedPlayer 附件 → 飞船控制
```

---

## 渲染系统

### 粒子效果
- `ThrusterParticle` - 推进器尾焰
- `MuzzleFlashParticle` - 炮口火焰
- `BulletParticle` - 炮弹

### 渲染器
- `LargeIonRenderer` - 大型离子环
- `LargeIonExhaustRenderer` - 离子环排气
- `PlushieRenderer` - 毛绒方块
- `SelectionZoneRenderer` - 选择区域

---

## Mixin 注入

**Mixin 列表:**

### 服务器端
| Mixin | 目标 | 用途 |
|-------|------|------|
| `MixinLaser` | 激光渲染 | 修改激光外观 |
| `MixinRadiationManager` | 辐射系统 | 辐射处理 |
| `MixinSoundHandler` | 声音系统 | 声音修改 |
| `MixinTileEntityDigitalMiner` | 数字采矿器 | 集成 |

### 客户端
| Mixin | 目标 | 用途 |
|-------|------|------|
| `MixinRenderMechanicalPipe` | 机械管道渲染 | 自定义管道外观 |
| `MixinRenderPressureTube` | 压力管道渲染 | 自定义管道外观 |
| `MixinRenderUniversalCable` | 通用线缆渲染 | 线缆优化 |
| `MixinGameRenderer` | 游戏渲染器 | 全局渲染钩子 |
| `MixinThreadMinerSearch` | 采矿器搜索 | 搜索优化 |

---

## 项目构建与运行

### 构建要求
- Java 17
- Gradle 8.x
- Minecraft 1.20.1
- Forge 47.2.0

### 构建命令
```bash
# 构建模组
./gradlew build

# 运行客户端
./gradlew runClient

# 运行服务器
./gradlew runServer

# 生成数据 (配方、战利品表、标签)
./gradlew runData
```

### 开发设置
1. 克隆仓库
2. 运行 `./gradlew setupDecompWorkspace`
3. 导入 IDE (推荐 IntelliJ IDEA)
4. 通过 IDE 运行配置启动

---

## 创造模式标签内容

- `LIGHTWEIGHT_ALLOY` - 合成材料
- `TOOLGUN` - 飞船组装/移动工具
- `LIQUID_FUEL_THRUSTER_*` - 多方块推进器部件
- `ION_THRUSTER` - 单方块推进器
- `SHIP_CONTROL_INTERFACE` - 飞船驾驶方块
- `CANNON` - 气体驱动武器
- `GYRO` - 旋转控制
- `LARGE_ION_THRUSTER_*` - 环形推进器部件
- `*_PLUSHIE` - 装饰方块

---

*文档基于代码库分析生成。更新日期: 2026-05-22*
