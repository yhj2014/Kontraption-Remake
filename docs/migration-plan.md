# Kontraption 迁移计划书

> **版本**: v1.0.0  
> **日期**: 2026-05-24  
> **目标**: 从 Forge 1.20.1 → NeoForge 1.21.1，物理引擎从 Valkyrien Skies 2 → Sable 1.2.2+，新增 Create: Aeronautics 1.1.0+ 支持

---

## 目录

1. [概述](#概述)
2. [技术栈变更](#技术栈变更)
3. [迁移阶段](#迁移阶段)
4. [详细迁移步骤](#详细迁移步骤)
5. [风险与缓解](#风险与缓解)
6. [测试计划](#测试计划)

---

## 概述

### 迁移目标

将 Kontraption 从现有的架构迁移到：
- **MC 版本**: 1.21.1
- **加载器**: NeoForge (替代 Forge)
- **物理引擎**: Sable 1.2.2+ (替代 Valkyrien Skies 2)
- **新增支持**: Create: Aeronautics 1.1.0+
- **依赖更新**: 
  - Mekanism ≥ 10.7.17.83
  - Kotlin for Forge ≥ 5.10.0

### 迁移范围

| 范围 | 说明 |
|------|------|
| 构建系统 | Gradle 配置、NeoForge Gradle 插件 |
| 核心 API | NeoForge 与 Forge API 差异 |
| 物理系统 | Valkyrien Skies → Sable 完全重写 |
| 第三方集成 | Create: Aeronautics 集成 |
| 配置系统 | 保持不变，扩展支持新选项 |
| Mixin 系统 | 重构所有现有 Mixins |

---

## 技术栈变更

### 旧技术栈 vs 新技术栈

| 组件 | 旧版本 | 新版本 | 变更类型 |
|------|--------|--------|---------|
| Minecraft | 1.20.1 | 1.21.1 | 版本升级 |
| 加载器 | Forge 47.2.0 | NeoForge (TBD) | 平台迁移 |
| Kotlin for Forge | 4.11.0 | ≥ 5.10.0 | 版本升级 |
| 物理引擎 | Valkyrien Skies 2.4.6+ | Sable 1.2.2+ | 引擎迁移 |
| Mekanism | 10.4.0+ | ≥ 10.7.17.83 | 版本升级 |
| 新增模组 | N/A | Create: Aeronautics ≥ 1.1.0 | 新集成 |

### 主要 API 变更

#### 1. NeoForge vs Forge 差异

- 包名变更: `net.minecraftforge` → `net.neoforged`
- 注册表系统变更
- 事件系统变更
- 数据生成器变更
- 混合器(Mixin)架构变更

#### 2. Valkyrien Skies vs Sable 差异

| 特性 | Valkyrien Skies 2 | Sable 1.2.2+ |
|------|------------------|-------------|
| 飞船概念 | Ship | 可能不同的命名 |
| 附件系统 | ShipAttachment | TBD |
| 物理监听器 | ShipPhysicsListener | TBD |
| 力的应用方式 | applyForce() | TBD |
| 旋转表示 | Quaterniond | TBD |
| 坐标系 | 自定义坐标系 | 可能不同 |
| 质量计算 | 方块质量系统 | TBD |
| 组装系统 | 内置组装 | 可能不同 |

#### 3. Create: Aeronautics 新增集成

Create: Aeronautics 提供:
- 内置的飞艇系统
- 帆、螺旋桨、引擎等组件
- 可能与 Kontraption 的功能有重叠或互补

---

## 迁移阶段

### 阶段 1: 构建系统和环境准备 (优先级: 最高)

**目标**: 建立 NeoForge 1.21.1 的构建环境

1. 更新 `gradle.properties`
2. 更新 `build.gradle` 或迁移到 `build.gradle.kts`
3. 更新 `settings.gradle`
4. 配置 NeoForge 依赖
5. 更新 Kotlin 版本
6. 更新 Kotlin for Forge 到 5.10.0+
7. 更新 Java 工具链 (1.21 可能需要 Java 21)
8. 验证构建环境

**验收标准**: 能成功执行 `./gradlew build` 和 `./gradlew runClient` (无内容或最小内容)

---

### 阶段 2: 核心系统迁移 (优先级: 高)

**目标**: 迁移不依赖物理系统的核心功能

1. 更新包名 (Forge → NeoForge)
2. 更新注册系统
3. 更新事件系统
4. 重构所有 Mixins
5. 迁移方块和物品
6. 迁移方块实体
7. 迁移网络系统
8. 迁移配置系统
9. 迁移客户端渲染 (非物理相关)

**验收标准**: 所有非物理功能能正常工作，游戏能正常启动，方块能放置和交互

---

### 阶段 3: 物理系统完全重写 (优先级: 最高)

**目标**: 将 Valkyrien Skies 系统替换为 Sable 系统

#### 子阶段 3.1: Sable 核心集成

1. 添加 Sable 1.2.2+ 依赖
2. 研究 Sable API
3. 设计新的物理控制架构
4. 迁移基础工具类 (VSUtils → SableUtils)

#### 子阶段 3.2: 推进器系统重写

1. 迁移 `ThrusterInterface`
2. 重写 `KontraptionThrusterControl`
3. 适配 Sable 的力应用方式
4. 迁移推进器方块实体

#### 子阶段 3.3: 陀螺仪系统重写

1. 重写 `KontraptionGyroControl`
2. 适配 Sable 的旋转系统

#### 子阶段 3.4: 玩家控制重写

1. 迁移 `KontraptionSeatedControllingPlayer`
2. 迁移驾驶数据包
3. 迁移 `TileEntityShipControlInterface`

#### 子阶段 3.5: 工具枪重写

1. 研究 Sable 的组装/创建机制
2. 重写工具枪功能
3. 迁移选择区域渲染

**验收标准**: 基本的飞船能创建、控制、推进、旋转

---

### 阶段 4: 大型多方块结构迁移 (优先级: 高)

**目标**: 迁移大型离子环、液氢推进器、电磁炮

1. 迁移大型离子环
2. 迁移液氢推进器
3. 迁移电磁炮 (如可用)
4. 更新多方块结构验证

---

### 阶段 5: Create: Aeronautics 集成 (优先级: 中)

**目标**: 提供与 Create: Aeronautics 的兼容性和可选集成

1. 研究 Create: Aeronautics API
2. 设计集成策略 (兼容性或增强)
3. 实现集成代码
4. 可选: 提供交叉操作的方块
5. 更新文档

---

### 阶段 6: 性能优化和 bug 修复 (优先级: 中)

**目标**: 确保在新环境下的性能和稳定性

1. 性能测试
2. 内存泄漏检查
3. 崩溃修复
4. 逻辑错误修复
5. 优化网络包
6. 优化渲染

---

### 阶段 7: 文档和发布准备 (优先级: 低)

**目标**: 完成迁移并准备发布

1. 更新 README
2. 更新 wiki (如果有)
3. 准备变更日志
4. 准备版本发布
5. 社区公告

---

## 详细迁移步骤

### 详细步骤: 阶段 1 - 构建系统

#### 文件变更

**1. 更新 `gradle.properties`**

```properties
# 旧
minecraft_version=1.20.1
forge_version=47.2.0

# 新
minecraft_version=1.21.1
neoforge_version=[LATEST_VERSION]
```

**2. 完全重写 `build.gradle`**

建议迁移到 `build.gradle.kts` (Kotlin DSL)，因为:
- 更好的类型安全
- 更好的 IDE 支持
- NeoForge 社区趋势

主要变更点:
- 替换 ForgeGradle 为 NeoGradle
- 更新 Kotlin 插件
- 更新 Kotlin for Forge 到 5.10.0+
- 更新 Mekanism 到 10.7.17.83+
- 添加 Sable 依赖
- 添加 Create: Aeronautics 可选依赖
- 更新 Mixin 配置
- 更新运行配置

---

### 详细步骤: 阶段 2 - 核心系统

#### 2.1 包名和导入

- 搜索替换: `import net.minecraftforge.` → `import net.neoforged.`
- 搜索替换: `net.minecraftforge.` → `net.neoforged.` (在注解等地方)

#### 2.2 注册表系统

NeoForge 可能对注册系统有变更，需要:
- 更新 `DeferredRegister` 使用方式
- 更新事件注册方式
- 检查 `RegistrySupplier` 等变更

#### 2.3 事件系统

NeoForge 事件系统可能:
- 有不同的事件总线
- 有不同的注解
- 有不同的取消机制
- 有不同的优先级系统

#### 2.4 Mixins 重构

所有现有的 Mixins 可能需要:
- 更新 `@Mixin` 目标 (类名可能变化)
- 更新注入点 (方法描述符可能变化)
- 更新 Mixin 配置文件 (`kontraption.mixins.json`)
- 可能需要完全重写某些 Mixins

**Mixin 清单**:
- MixinGameRenderer
- MixinGender
- MixinLaser
- MixinRadiationManager
- MixinRenderMechanicalPipe
- MixinRenderPressureTube
- MixinRenderUniversalCable
- MixinSoundHandler
- MixinTeleporter
- MixinThermalEvapRender
- MixinThreadMinerSearch
- MixinTileEntityDigitalMiner

#### 2.5 方块和物品系统

需要检查:
- 方块属性系统变更
- 方块状态系统变更
- 物品属性系统变更
- 创造模式标签系统变更
- 模型系统变更

#### 2.6 方块实体系统

需要检查:
- BlockEntity 注册变更
- Tick 系统变更
- 网络同步变更
- 保存/加载变更

#### 2.7 网络系统

需要检查:
- SimpleChannel 系统变更
- 数据包编解码变更
- 自定义负载变更

#### 2.8 配置系统

- 更新配置 API (如果有变更)
- 保持现有配置项兼容性
- 添加新的配置项 (如需要)

---

### 详细步骤: 阶段 3 - 物理系统

#### 3.1 研究 Sable API (关键步骤!)

在开始编码前，需要:
- 阅读 Sable 官方文档
- 查看 Sable 示例模组
- 理解 Sable 的核心概念:
  - 飞船/物理对象是如何表示的
  - 如何附加自定义数据
  - 如何应用力和扭矩
  - 如何监听物理事件
  - 坐标系系统
  - 质量和惯量计算

#### 3.2 设计新的物理架构

| 旧组件 (VS2) | 新组件 (Sable) | 设计考虑 |
|-------------|--------------|---------|
| `KontraptionThrusterControl` | `KontraptionThrusterControl` | 完全重写，适配 Sable API |
| `KontraptionGyroControl` | `KontraptionGyroControl` | 完全重写 |
| `KontraptionSeatedControllingPlayer` | `KontraptionSeatedControllingPlayer` | 保留概念，适配 Sable |
| `KontraptionVSUtils` | `KontraptionSableUtils` | 完全重写 |
| `ThrusterInterface` | `ThrusterInterface` | 保持接口，重写实现 |

#### 3.3 推进器系统重写

1. **移除 VS2 导入**
2. **添加 Sable 导入**
3. **重写力的应用方式**
   - 在 VS2 中: `physShip.applyWorldForce(force)`
   - 在 Sable 中: 需要查找对应方法

4. **重写推进器注册机制**
   - VS2 使用 ShipAttachment
   - Sable 可能使用不同的系统

5. **重写物理 tick 处理**

#### 3.4 工具枪重写

1. **研究 Sable 的飞船创建机制**
2. **重写组装功能**
3. **重写移动功能**
4. **重写选择区域渲染**

---

### 详细步骤: 阶段 5 - Create: Aeronautics 集成

1. **可选依赖配置**
   ```kotlin
   compileOnly("...") // Create: Aeronautics
   runtimeOnly("...") // 可选
   ```

2. **兼容性检查**
   - 检测模组是否加载
   - 避免 ID 冲突
   - 避免方块/物品冲突

3. **集成点设计**
   - 推进器与 Create 引擎的交互
   - 控制界面与 Create 系统的交互
   - 可选: 共享的物理系统 (如果兼容)

4. **配置选项**
   - 启用/禁用集成
   - 集成行为配置

---

## 风险与缓解

| 风险 | 影响 | 概率 | 缓解策略 |
|-----|------|------|---------|
| Sable API 与 VS2 差异过大 | 高 | 高 | 1. 提前研究 Sable API<br>2. 分阶段迁移，每个阶段都有回退点<br>3. 保持核心逻辑独立于物理引擎 |
| NeoForge API 与 Forge 差异过大 | 中 | 高 | 1. 使用兼容性层<br>2. 分步骤迁移，每次只改一部分<br>3. 参考 NeoForge 官方迁移指南 |
| Mekanism 10.7.17.83 API 变更 | 中 | 中 | 1. 研究 Mekanism 更新日志<br>2. 使用适配层<br>3. 保持与 Mekanism 的通信渠道 |
| Create: Aeronautics 与 Kontraption 功能重叠 | 低 | 中 | 1. 提供配置选项切换<br>2. 设计成互补而非竞争<br>3. 清晰的文档说明 |
| 性能下降 | 中 | 中 | 1. 性能测试贯穿始终<br>2. 优化热点代码<br>3. 可能需要使用新的优化技术 |
| 社区反馈负面 | 低 | 低 | 1. 提前公告<br>2. 提供迁移指南<br>3. 保持旧版本可用一段时间 |

---

## 测试计划

### 1. 单元测试 (如存在)

- 保持现有单元测试通过
- 为新代码添加单元测试

### 2. 集成测试

| 测试项 | 验收标准 |
|-------|---------|
| 方块放置 | 所有方块能正确放置 |
| 方块交互 | 所有交互功能正常 |
| 飞船创建 | 工具枪能正常创建飞船 |
| 飞船控制 | WASD、鼠标控制正常 |
| 推进器工作 | 推进器能正确工作和消耗资源 |
| 陀螺仪工作 | 陀螺仪能正确稳定飞船 |
| 多方块组装 | 大型多方块能正确组装和工作 |
| Create 集成 | 与 Create: Aeronautics 兼容性良好 |
| 客户端渲染 | 无渲染错误，视觉效果正常 |
| 网络同步 | 多人游戏同步正常 |

### 3. 性能测试

- 单飞船性能
- 多飞船性能
- 内存使用
- 网络带宽使用

---

## 时间估计

| 阶段 | 估计工作量 |
|------|----------|
| 阶段 1: 构建系统 | 1-2 天 |
| 阶段 2: 核心系统 | 3-5 天 |
| 阶段 3: 物理系统 (含研究) | 7-14 天 |
| 阶段 4: 多方块 | 3-5 天 |
| 阶段 5: Create 集成 | 2-4 天 |
| 阶段 6: 优化和修复 | 3-7 天 |
| 阶段 7: 文档和发布 | 1-2 天 |
| **总计** | **~20-39 天** |

---

## 资源需求

- **开发人员**: 1-2 人，熟悉 Kotlin、Forge、物理系统
- **测试人员**: 1-2 人，负责游戏测试
- **文档人员**: 0.5 人，负责更新文档

---

## 回退计划

如果迁移失败，保持 `1.20.1-forge` 分支作为 LTS 分支，继续维护现有版本。

---

## 参考资源

1. [NeoForge 官方文档](https://docs.neoforged.net/)
2. [NeoForge 迁移指南](https://docs.neoforged.net/docs/gettingstarted/migrating/)
3. [Sable 官方文档/仓库](TBD)
4. [Create: Aeronautics 官方文档](TBD)
5. [Mekanism 官方文档](https://docs.mekanism.net/)
6. [Kotlin for Forge 文档](https://thedarkcolour.github.io/KotlinForForge/)

---

*文档版本: v1.0.0*
