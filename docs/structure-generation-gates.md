# 结构生成解锁框架

`StructureGenerationGates` 为需要世界进度才能生成的结构提供集中式 Java DSL。它目前承载村庄解锁规则以及永久禁用的原版结构，也可以复用于其他原版或模组结构。

框架只控制候选结构能否在新生成的区块中开始生成；不会修改已有区块，也不会在条件达成后回填此前被跳过的结构。

## 当前规则

当前内置规则如下：

| 规则 ID | 维度 | 结构选择器 | 解锁条件 |
| --- | --- | --- | --- |
| `infx:village` | 主世界 | `#minecraft:village` | 生存第 60 天或之后，且全世界已制作铁级工具 |
| `infx:ancient_city` | 主世界 | `minecraft:ancient_city` | 永不解锁 |
| `infx:trial_chambers` | 主世界 | `minecraft:trial_chambers` | 永不解锁 |

实现位于 [`StructureGenerationGates.java`](../src/main/java/com/pixulse/infx/world/StructureGenerationGates.java)。村庄的铁级工具检测、农田枯萎和保险箱后处理仍位于 [`VillageProgression.java`](../src/main/java/com/pixulse/infx/world/VillageProgression.java)。`/infx villages` 仍可查询该规则的状态。

## 规则语义

每条 `StructureGate` 由四部分组成：稳定规则 ID、适用维度集合、结构选择器和解锁条件。

- 不匹配任何规则的候选结构始终允许生成。
- 结构与维度同时匹配多条规则时，所有匹配规则都必须解锁。
- 维度不匹配的规则不会限制结构。
- `afterDay(day)` 的边界是包含的，即当前世界天数 `>= day` 时通过；天数从 1 开始。
- 规则只影响条件达成后新生成的区块。

这个“匹配规则取逻辑 AND”的行为允许用标签写通用限制，再用精确结构 ID 追加更严格的限制。

## 添加新规则

在 `StructureGenerationGates` 的 `RULES` 列表中添加一个 `StructureGate`。下面的示例把主世界的掠夺者前哨站限制到第 90 天以后，并要求世界已征服末地：

```java
new StructureGate(
        InfiniteX.id("pillager_outpost"),
        Set.of(Level.OVERWORLD),
        StructureSelector.key(BuiltinStructures.PILLAGER_OUTPOST),
        Conditions.allOf(
                Conditions.afterDay(90L),
                Conditions.milestone(WorldMilestone.END_CONQUERED)))
```

规则 ID 必须唯一；维度集合不能为空。不要在单独的 Mixin 或结构处理器中复制条件判断，所有生成限制都应归入 `RULES`，以便叠加语义保持一致。

### 选择结构

`StructureSelector` 提供两种工厂方法：

```java
// 只匹配指定注册表键。
StructureSelector.key(BuiltinStructures.PILLAGER_OUTPOST)

// 匹配当前已加载标签中的任意结构。
StructureSelector.tag(StructureTags.VILLAGE)
```

精确键适合单个结构；标签适合一组同类结构。标签匹配使用动态注册表中已加载的标签，因此标签内容仍可由正常的数据包机制提供；本框架本身不引入新的结构解锁 JSON 格式。

### 组合条件

`Conditions` 只对不可变的 `WorldProgressSnapshot` 求值，可安全用于区块生成线程。

| API | 通过条件 |
| --- | --- |
| `afterDay(day)` | 世界天数达到 `day` |
| `never()` | 永不通过；用于永久禁用结构 |
| `milestone(milestone)` | 世界里程碑已完成 |
| `firstCompletion(key)` | 世界首次完成记录中存在 `key` |
| `allOf(a, b, ...)` | 每个子条件都通过 |
| `anyOf(a, b, ...)` | 至少一个子条件通过 |

`allOf` 和 `anyOf` 至少需要一个条件。`firstCompletion(key)` 的键与 `WorldData.recordFirst(...)` 写入的世界首次完成记录一致，而不是某个玩家的个人进度。

例如，下面的条件要求第 45 天以后，且全世界首次完成过 `build_library`：

```java
Conditions.allOf(
        Conditions.afterDay(45L),
        Conditions.firstCompletion("build_library"))
```

目前内置的 `WorldMilestone` 为：

- `IRON_TOOL_CRAFTED`
- `END_CONQUERED`

## 世界进度与线程模型

区块生成可能不在服务器线程执行，因此 `ChunkGeneratorMixin` 不读取 `ServerLevel`、`WorldData` 或玩家状态。它只调用：

```java
StructureGenerationGates.allows(dimension, candidateStructure)
```

该方法读取一个 `volatile` 发布的、不可变的 `WorldProgressSnapshot`。快照生命周期如下：

1. 服务器即将启动时重置为全锁定状态。
2. 服务器启动完成时从主世界刷新一次。
3. 每个服务器 tick 结束时刷新一次。
4. 服务器停止时再次清空为全锁定状态。

任何会立刻改变解锁状态的服务器线程逻辑，都应在更新 `WorldData` 后调用 `StructureGenerationGates.refresh(level)`；村庄的铁级工具制作事件就是现有示例。即使未主动刷新，下一次服务器 tick 也会发布新快照。

新条件需要持久化世界状态时，按以下顺序扩展：

1. 在 `WorldData` 中新增字段、编解码器字段和受控更新方法，保持旧存档的默认值兼容。
2. 将其投影到 `WorldProgressSnapshot`，或在已有里程碑/首次完成记录可表达时直接复用它们。
3. 在状态变更后刷新快照。
4. 使用新条件添加 `StructureGate`，并补充规则引擎测试。

不要让 `GateCondition` 捕获可变世界对象、玩家对象或注册表查询结果；条件应只检查传入的快照。

## 生成入口

[`ChunkGeneratorMixin.java`](../src/main/java/com/pixulse/infx/mixin/ChunkGeneratorMixin.java) 在原版 `ChunkGenerator.tryGenerateStructure` 的开头拦截候选结构。该最小 Mixin 保留的原因是原版公开事件没有同时提供候选结构和服务器进度。

候选结构不满足规则时，Mixin 返回 `false`，该次结构尝试不会生成。已经写入区块的结构不会因为规则后来改变而被删除或补充。

## 验证

规则引擎测试位于 [`StructureGenerationGatesTest.java`](../src/test/java/com/pixulse/infx/world/StructureGenerationGatesTest.java)，覆盖：

- 精确 ID 和标签选择器；
- 维度范围和无规则放行；
- 天数边界、里程碑与世界首次完成条件；
- `allOf` / `anyOf`；
- 多条重叠规则必须全部满足；
- 村庄解锁条件和 Mixin 注册。

修改框架或新增规则后，执行：

```sh
zsh ./gradlew test
zsh ./gradlew build
```

最后一项用于验证 Mixin 在游戏测试服务器中可以启动；实际体验验证时，应在条件未满足和满足后分别探索尚未生成的区块。
