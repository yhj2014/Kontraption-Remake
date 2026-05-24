# Kontraption 迁移依赖 API 文档汇总

> **版本**: v1.0.0  
> **日期**: 2026-05-24  
> **目标**: 收集 Sable、Create: Aeronautics、Kotlin for Forge、Mekanism、NeoForge、JOML 的 API 文档和关键信息

---

## 目录

1. [Sable 物理引擎](#1-sable-物理引擎)
2. [Create: Aeronautics](#2-create-aeronautics)
3. [Kotlin for Forge](#3-kotlin-for-forge)
4. [Mekanism](#4-mekanism)
5. [NeoForge](#5-neoforge)
6. [JOML](#6-joml)
7. [文档资源汇总](#文档资源汇总)

---

## 1. Sable 物理引擎

### 基本信息

| 属性 | 值 |
|------|-----|
| **官方名称** | Sable |
| **作者** | ryanhcode |
| **支持版本** | 1.21.1 |
| **加载器** | NeoForge / Fabric |
| **物理引擎** | Rapier (Rust) |
| **Modrinth** | https://modrinth.com/mod/sable |
| **GitHub** | https://github.com/ryanhcode/sable |
| **Wiki** | https://github.com/ryanhcode/sable/wiki |
| **最新版本** | 1.2.2+ |

### 核心概念

#### 1.1 Sub-Level (子关卡)
Sable 中的核心概念，将一组可移动的方块和实体组合成一个"子关卡"进行物理模拟。

```kotlin
// Sable 将方块、方块实体和实体组成的可移动区域称为 Sub-Level
// 它在组装完成后仍可保持交互
```

#### 1.2 物理属性
Sable 通过 datapack 定义方块的物理属性：

| 属性 | 单位 | 默认值 | 描述 |
|------|------|--------|------|
| `sable:mass` | kpg | 1.0 | 方块质量 |
| `sable:inertia` | kpg*m² | [1/6, 1/6, 1/6] | 各轴惯性矩乘数 |
| `sable:volume` | m³ | 1.0 | 用于浮力计算 |
| `sable:restitution` | 0-1 | 0.0 | 弹性系数 |
| `sable:friction` | multiplier | 1.0 | 摩擦系数 |
| `sable:fragile` | boolean | false | 碰撞是否破坏 |
| `sable:floating_material` | string | null | 浮空方块材质 |
| `sable:floating_scale` | multiplier | 1.0 | 浮空材质乘数 |

#### 1.3 维度物理数据
通过 datapack 配置维度物理参数：

```json
{
    "dimension": "minecraft:overworld",
    "priority": 0,
    "base_gravity": [0.0, -11.0, 0.0],
    "base_pressure": 1.0,
    "universal_drag": 0.09,
    "magnetic_north": [0.0, 0.0, 0.0]
}
```

### API 关键类

#### Sub-Level API (CC: Sable)
```lua
-- ComputerCraft 提供的 Sub-Level API

sublevel.getUniqueId(): string           -- 获取子关卡唯一ID
sublevel.getMass(): number              -- 获取质量
sublevel.getVelocity(): vector          -- 获取速度
sublevel.getPosition(): vector          -- 获取位置
sublevel.getRotation(): quaternion       -- 获取旋转(四元数)
sublevel.getInertiaTensor(): number     -- 获取惯性张量
sublevel.getInverseInertiaTensor(): number -- 获取逆惯性张量
sublevel.applyForce(force: vector, origin?: vector) -- 施加力
sublevel.applyTorque(torque: vector)    -- 施加扭矩
```

#### Aerodynamics API
```lua
aero.getAirPressure(position: vector): number   -- 获取气压
aero.getGravity(): vector                       -- 获取重力
```

### 预定义标签
```json
{
    "#sable:super_light": "mass = 0.25",
    "#sable:light": "mass = 0.5",
    "#sable:heavy": "mass = 2.0",
    "#sable:super_heavy": "mass = 4.0",
    "#sable:half_volume": "volume = 0.5",
    "#sable:quarter_volume": "volume = 0.25",
    "#sable:slippery": "friction = 0.0",
    "#sable:bouncy": "restitution = 0.5"
}
```

### 兼容性
- **警告**: Sable 是一个侵入性极强的模组，大量使用 mixin
- **Create: Aeronautics**: 使用 Sable 作为物理后端
- **CC: Sable**: 提供 ComputerCraft 集成
- **Sable: True Impact**: 物理碰撞伤害扩展

---

## 2. Create: Aeronautics

### 基本信息

| 属性 | 值 |
|------|-----|
| **官方名称** | Create: Aeronautics |
| **作者** | gamerman2354235 |
| **支持版本** | 1.21.1 |
| **加载器** | NeoForge |
| **Wiki** | https://createaeronautics.wiki/ |
| **官方主页** | https://createaero.pro/ |
| **GitHub** | https://github.com/gamerman2354235/Create_Aeronautics |
| **Modrinth** | https://modrinth.com/mod/create-aeronautics |

### 核心概念

#### 2.1 飞艇构建流程
1. 使用 Create 的 superglue 和 chassis 方块构建普通机械
2. 添加 envelope（气囊）、blaze burner（烈焰燃烧器）、引擎等
3. 添加螺旋桨、方向舵等控制部件
4. 激活飞行

#### 2.2 工作原理
```
现实世界中的方块 → 移动到独立维度 → 独立维度内独立运行
                      ↓
            主世界中的飞行器实体
```

#### 2.3 关键组件

| 组件类型 | 功能 |
|---------|------|
| Envelope (气囊) | 提供升力 |
| Stirling Engine | 引擎类型 |
| Furnace Engine | 引擎类型 |
| Propeller (螺旋桨) | 推进力 |
| Rudder (方向舵) | 转向控制 |
| Blaze Burner | 热源 |

### 与 Kontraption 的关系

Create: Aeronautics 与 Kontraption 都提供飞船功能：

| 特性 | Create: Aeronautics | Kontraption (目标) |
|------|---------------------|-------------------|
| 物理引擎 | Sable | Sable |
| 推进方式 | 螺旋桨/引擎 | 离子/液氢推进器 |
| 控制方式 | 游戏手柄/键鼠 | 键盘+鼠标 |
| 复杂度 | 高 (机械系统) | 中 (能量系统) |
| 集成 | 独立使用 | 与 Mekanism 深度集成 |

### 集成策略

#### 兼容性层
- ID 冲突检查
- 方块位置冲突处理
- 事件冲突处理

#### 可选功能层
- 能量转换适配器
- 共享控制界面

#### 深度集成层 (可选)
- 混合飞船支持
- 交叉功能方块

---

## 3. Kotlin for Forge

### 基本信息

| 属性 | 值 |
|------|-----|
| **官方名称** | Kotlin for Forge / KotlinLangForge |
| **原作者** | thedarkcolour |
| **维护者** | btwonion |
| **支持版本** | 1.16.3 - 1.21.11 |
| **GitHub (原版)** | https://github.com/thedarkcolour/KotlinForForge |
| **GitHub (维护版)** | https://github.com/btwonion/KotlinLangForge |
| **支持版本** | ≥ 5.10.0 (NeoForge) |

### 版本对应表

| Minecraft 版本 | 语言提供者版本 | 支持加载器 |
|---------------|--------------|-----------|
| 1.16.5 | 1.0 | Forge |
| 1.17.1 - 1.20.4 | 2.0 | Forge, NeoForge |
| 1.20.5 - 1.21.x | 3.0 | NeoForge |
| **NeoForge 1.21.x** | **≥ 5.10.0** | **NeoForge** |

### 集成方式

#### mods.toml 配置
```toml
modLoader = "klf"
loaderVersion = "[1,)"
```

#### build.gradle.kts 依赖
```kotlin
repositories {
    maven("https://repo.nyon.dev/releases")
}

dependencies {
    modImplementation("dev.nyon:KotlinLangForge:$version-$kotlinVersion-$lpVersion+$loader")
}
```

### 核心功能

#### 3.1 @Mod 注解
```kotlin
@Mod(Kontraption.MOD_ID)
object Kontraption {
    // 支持 object 或 class (需 public 构造函数)
}
```

#### 3.2 @EventBusSubscriber
```kotlin
@EventBusSubscriber(Dist.CLIENT)
object ClientEvents {
    @SubscribeEvent
    fun onRenderWorld(event: RenderLevelStageEvent) {
        // 自动注册事件处理
    }
}
```

#### 3.3 构造函数参数
```kotlin
@Mod(Kontraption.MOD_ID)
class Kontraption(
    val eventBus: IEventBus,
    val modContainer: ModContainer,
    val kotlinModContainer: KotlinModContainer?,
    val dist: Dist
)
```

### 内置库

| 库 | 版本 |
|----|------|
| kotlin-stdlib | 2.2.20 |
| kotlinx-serialization-core | 1.9.0 |
| kotlinx-coroutines-core | 1.10.2 |
| kotlinx-datetime | 0.7.1-0.6.x-compat |
| kotlinx-io-core | 0.8.0 |
| atomicfu | 0.29.0 |

### NeoForge 迁移指南

对于 Forge 1.20.1 项目迁移：

```groovy
// build.gradle 变更
plugins {
    // 添加 Kotlin Gradle 插件
    id 'org.jetbrains.kotlin.jvm' version '1.9.22'
    id 'org.jetbrains.kotlin.plugin.serialization' version '1.9.22'
}

repositories {
    maven {
        name = 'Kotlin for Forge'
        url = 'https://thedarkcolour.github.io/KotlinForForge/'
    }
}

dependencies {
    implementation 'thedarkcolour:kotlinforforge-neoforge:4.10.0'
}
```

---

## 4. Mekanism

### 基本信息

| 属性 | 值 |
|------|-----|
| **官方名称** | Mekanism |
| **作者** | Aidan C. Brady (aidancbrady) |
| **支持版本** | 1.21.1+ |
| **GitHub** | https://github.com/mekanism/Mekanism |
| **官方 Wiki** | https://wiki.aidancbrady.com/wiki/Mekanism |
| **Discord** | https://discord.gg/nmSjMGc |
| **Maven** | https://modmaven.dev/ |
| **最低要求版本** | ≥ 10.7.17.83 |

### 模块结构

| 模块 | 描述 |
|------|------|
| **Mekanism** | 核心模块 (能量、化学、机器等) |
| **Mekanism: Generators** | 发电设备 |
| **Mekanism: Tools** | 工具和装备 |
| **Mekanism: Additions** | 塑料和装饰 |

### API 结构

#### 4.1 核心 API 包
```
mekanism.api/
├── actions/           # 动作控制
├── annotations/        # 注解
├── chemical/          # 化学系统 (气体/液体)
├── enchantments/      # 附魔
├── energy/           # 能量系统
├── fluid/            # 流体系统
├── heat/             # 热力系统
├── inventory/        # 物品栏系统
├── recipes/          # 配方系统
└── tex/              # 纹理工具
```

#### 4.2 关键接口

**能量接口**
```java
public interface IEnergyContainer {
    void setEnergy(double energy);
    double getEnergy();
    double getMaxEnergy();
    double getEnergyNeeded();
    double insert(double amount, Action action);
    double extract(double amount, Action action);
}
```

**化学接口**
```java
public interface IChemicalTank {
    void setStored(long amount);
    long getStored();
    long getCapacity();
    long insert(long amount, Action action);
    long extract(long amount, Action action);
}
```

**多方块数据**
```java
public class MultiblockData implements 
    IMekanismInventory,
    IMekanismFluidHandler,
    IMekanismStrictEnergyHandler,
    ITileHeatHandler,
    IMekanismChemicalHandler 
{
    public Set<BlockPos> locations;
    public MultiblockCache cache;
}
```

#### 4.3 机器系统

**基础机器类**
```java
public class TileEntityMultiblock<T extends MultiblockData> {
    T data;
    void onUpdateServer();
    void onUpdateClient();
}
```

**多方块结构**
```java
public class FormationProtocol {
    enum StructureRequirement {
        CENTER, MIDDLE, OUTER, ATOMIC;
    }
}
```

### Maven 集成

```groovy
repositories {
    maven { url 'https://modmaven.dev/' }
}

dependencies {
    // API 依赖
    compileOnly "mekanism:Mekanism:${mekanism_version}:api"
    
    // 完整模组 (运行时)
    runtimeOnly fg.deobf("mekanism:Mekanism:${mekanism_version}")
    runtimeOnly fg.deobf("mekanism:Mekanism:${mekanism_version}:additions")
    runtimeOnly fg.deobf("mekanism:Mekanism:${mekanism_version}:generators")
    runtimeOnly fg.deobf("mekanism:Mekanism:${mekanism_version}:tools")
}
```

### gradle.properties 配置
```properties
mekanism_version=1.21.1-10.7.15.80
```

### 与 Kontraption 的集成点

| 功能 | 使用的 Mekanism API |
|------|-------------------|
| 离子推进器 | 能量系统 (IEnergyContainer) |
| 液氢推进器 | 化学系统 (IGasTank, Hydrogen) |
| 大型多方块 | 多方块系统 (MultiblockData) |
| 工具枪 | 能量存储 (EnergyStorage) |

---

## 5. NeoForge

### 基本信息

| 属性 | 值 |
|------|-----|
| **官方名称** | NeoForge |
| **官方网站** | https://neoforged.net/ |
| **文档** | https://docs.neoforged.net/ |
| **GitHub** | https://github.com/neoforged/NeoForge |
| **支持版本** | 1.20.5+ |
| **目标版本** | 1.21.1 |

### 迁移指南 (Forge → NeoForge)

#### 5.1 关键差异

| 类别 | Forge | NeoForge |
|------|-------|----------|
| 包名 | `net.minecraftforge` | `net.neoforged` |
| Gradle 插件 | `net.minecraftforge.gradle` | `net.neoforged.gradle.userdev` |
| 仓库 | Minecraft Forge | NeoForge |
| Mixin 配置 | build.gradle | mods.toml |

#### 5.2 gradle.properties 配置
```properties
minecraft_version=1.21.1
neo_version=21.1.77
```

#### 5.3 build.gradle 变更

**旧 (Forge)**
```groovy
plugins {
    id 'net.minecraftforge.gradle' version '6.0.16'
}
```

**新 (NeoForge)**
```groovy
plugins {
    id 'net.neoforged.gradle.userdev' version '7.0.80'
}
```

#### 5.4 mods.toml Mixin 配置
```toml
[[mixins]]
config = "modid.mixins.json"
```

#### 5.5 API 包名变更

| 旧 (Forge) | 新 (NeoForge) |
|-----------|--------------|
| `net.minecraftforge.common.util.Constants` | `net.neoforged.neoforge.common.util.Constants` |
| `net.minecraftforge.eventbus.api` | `net.neoforged.neoforge.eventbus.api` |
| `net.minecraftforge.fml` | `net.neoforged.neoforge.fml` |
| `net.minecraftforge.network` | `net.neoforged.neoforge.network` |

### NeoForge 1.21 迁移要点

#### 5.6 ResourceLocation 变更
```kotlin
// 旧
ResourceLocation("namespace", "path")

// 新 (1.21+)
ResourceLocation.fromNamespaceAndPath("namespace", "path")
ResourceLocation.parse("namespace:path")
```

#### 5.7 标签文件夹变更
```json
// 旧
"tags/blocks"
"tags/items"
"tags/entity_types"

// 新 (1.21+)
"tags/block"
"tags/item"
"tags/entity_type"
```

#### 5.8 渲染系统变更

**顶点缓冲区**
```java
// 新 API
VertexConsumer buffer = bufferSource.getBuffer(RenderType.translucent());

// 使用 Tesselator
BufferBuilder buffer = Tesselator.getInstance().begin(
    VertexFormat.Mode.QUADS, 
    DefaultVertexFormat.POSITION_TEX_COLOR
);

// 上传
BufferUploader.drawWithShader(buffer.buildOrThrow());
```

### 官方文档资源

| 资源 | 链接 |
|------|------|
| 官方文档 | https://docs.neoforged.net/ |
| 1.21 迁移指南 | https://docs.neoforged.net/primer/docs/1.21/ |
| 1.21.1 迁移指南 | https://docs.neoforged.net/primer/docs/1.21.1/ |
| GitHub | https://github.com/neoforged/NeoForge |

---

## 6. JOML

### 基本信息

| 属性 | 值 |
|------|-----|
| **官方名称** | JOML - Java OpenGL Math Library |
| **GitHub** | https://github.com/JOML-CI/JOML |
| **官方网站** | http://www.joml.org/ |
| **Maven Central** | https://search.maven.org/artifact/org.joml/joml |
| **JavaDoc** | https://javadoc.io/doc/org.joml/joml/ |

### 核心类

#### 6.1 向量类

| 类 | 描述 |
|----|------|
| `Vector2f`, `Vector2d` | 2D 向量 |
| `Vector3f`, `Vector3d` | 3D 向量 |
| `Vector4f`, `Vector4d` | 4D 向量 |
| `Vector2i`, `Vector3i`, `Vector4i` | 整数向量 |

#### 6.2 矩阵类

| 类 | 描述 |
|----|------|
| `Matrix2f`, `Matrix2d` | 2x2 矩阵 |
| `Matrix3f`, `Matrix3d` | 3x3 矩阵 |
| `Matrix4f`, `Matrix4d` | 4x4 矩阵 |
| `Matrix4x3f`, `Matrix4x3d` | 4x3 矩阵 |

#### 6.3 四元数类

| 类 | 描述 |
|----|------|
| `Quaternionf` | 单精度四元数 |
| `Quaterniond` | 双精度四元数 |

### 常用 API 示例

#### 6.4 向量操作
```java
Vector3f v = new Vector3f(0, 1, 0);
Vector3f a = new Vector3f(1, 0, 0);

// 向量加法
v.add(a);

// 叉积
a.cross(v);

// 归一化
a.normalize();
```

#### 6.5 矩阵变换
```java
Vector3f v = new Vector3f(1, 2, 3);
Vector3f offset = new Vector3f(1, 0, 0);

// 创建变换矩阵
Matrix4f m = new Matrix4f();
m.translation(offset);
m.transformPosition(v);
```

#### 6.6 相机变换
```java
Matrix4f m = new Matrix4f()
    .perspective((float) Math.toRadians(45.0f), 1.0f, 0.01f, 100.0f)
    .lookAt(0, 0, 10,
            0, 0, 0,
            0, 1, 0);
```

#### 6.7 旋转
```java
// 绕轴旋转
Vector3f center = new Vector3f(0, 3, 4);
new Matrix4f()
    .translate(center)
    .rotate((float) Math.toRadians(90.0f), 1, 0, 0)
    .translate(center.negate())
    .transformPosition(pointToRotate);
```

#### 6.8 四元数旋转
```java
Quaterniond q = new Quaterniond()
    .rotateX(Math.toRadians(45.0))
    .rotateY(Math.toRadians(30.0));

Vector3d result = new Vector3d();
q.transform(result);
```

### 与 Minecraft 集成

```java
// Minecraft Vec3 → JOML Vector3f
Vector3f jomlVec = new Vector3f(mcVec.x, mcVec.y, mcVec.z);

// Minecraft Quaternion → JOML Quaternionf
Quaternionf jomlQuat = new Quaternionf(mcQuat.i(), mcQuat.j(), mcQuat.k(), mcQuat.r());
```

---

## 文档资源汇总

### 官方文档链接

| 模组/库 | 官方文档 | Wiki | GitHub |
|---------|---------|------|--------|
| **Sable** | - | [Wiki](https://github.com/ryanhcode/sable/wiki) | [仓库](https://github.com/ryanhcode/sable) |
| **Create: Aeronautics** | [Wiki](https://createaeronautics.wiki/) | [主页](https://createaero.pro/) | [仓库](https://github.com/gamerman2354235/Create_Aeronautics) |
| **Kotlin for Forge** | [README](https://github.com/thedarkcolour/KotlinForForge/blob/6.x/README.md) | - | [仓库](https://github.com/thedarkcolour/KotlinForForge) |
| **KotlinLangForge** | [README](https://github.com/btwonion/KotlinLangForge) | - | [仓库](https://github.com/btwonion/KotlinLangForge) |
| **Mekanism** | [Wiki](https://wiki.aidancbrady.com/wiki/Mekanism) | [API Docs](https://deepwiki.com/mekanism/Mekanism) | [仓库](https://github.com/mekanism/Mekanism) |
| **NeoForge** | [文档](https://docs.neoforged.net/) | [迁移指南](https://docs.neoforged.net/primer/docs/1.21/) | [仓库](https://github.com/neoforged/NeoForge) |
| **JOML** | [主页](http://www.joml.org/) | - | [仓库](https://github.com/JOML-CI/JOML) |

### 迁移检查清单

在开始 Kontraption 迁移前，请确认：

- [ ] 已阅读 [Sable Wiki](https://github.com/ryanhcode/sable/wiki)
- [ ] 已了解 [Create: Aeronautics](https://createaeronautics.wiki/)
- [ ] 已配置 [Kotlin for Forge ≥ 5.10.0](https://github.com/btwonion/KotlinLangForge)
- [ ] 已了解 [Mekanism 10.7.17.83+ API](https://deepwiki.com/mekanism/Mekanism)
- [ ] 已阅读 [NeoForge 迁移指南](https://docs.neoforged.net/primer/docs/1.21/)
- [ ] 已了解 [JOML API](https://javadoc.io/doc/org.joml/joml/)

---

*文档版本: v1.0.0*
