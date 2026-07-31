# 已修改的 Minecraft 结构

本文记录 InfiniteX 当前对 Minecraft 原版结构的实际修改。这里的“修改”包括生成门禁、永久禁用、结构集或生物群系资格调整，以及结构生成后的专属处理；未列出的原版结构，其生成位置和生成条件没有被 InfiniteX 主动改写。

## 生成门禁

以下规则只影响门禁生效后新生成的区块。已经生成的区块不会回填结构，也不会因为玩家后来满足或失去条件而改变。

| 结构 | 原版结构键或范围 | 修改后的生成条件 |
| --- | --- | --- |
| 村庄 | `#minecraft:village`，主世界 | 生存第 60 天或之后，且全世界至少制作过一件铁级工具。村庄内部还保留 InfiniteX 的农田枯萎和保险箱后处理。 |
| 掠夺者前哨站 | `minecraft:pillager_outpost`，主世界 | 与村庄共享第 60 天和铁级工具条件。 |
| 林地府邸 | `minecraft:mansion`，主世界 | 任意存活在线玩家当前经验总值至少为 `100,000`。经验消费、默认死亡导致的经验下降或离线后，如果没有其他玩家达标，门禁重新锁定；经验不会跨玩家累计，也不写入世界进度存档。 |
| 海底神殿 | `minecraft:monument`，主世界 | 任意玩家进入过任意下界要塞后解锁。 |
| 主世界残破传送门 | `ruined_portal_standard`、`ruined_portal_desert`、`ruined_portal_jungle`、`ruined_portal_mountain`、`ruined_portal_ocean`、`ruined_portal_swamp` | 任意玩家进入过下界后解锁。下界残破传送门不受这条主世界门禁影响。 |
| 沉船、搁浅沉船 | `minecraft:shipwreck`、`minecraft:shipwreck_beached`，主世界 | 任意玩家击杀过普通守卫者后解锁。 |
| 古城 | `minecraft:ancient_city`，主世界 | 永不解锁，后续新区块不生成。 |
| 试炼密室 | `minecraft:trial_chambers`，主世界 | 永不解锁，后续新区块不生成。 |

门禁由 [`StructureGenerationGates`](../src/main/java/com/pixulse/infx/world/StructureGenerationGates.java) 统一注册，并由 [`ChunkGeneratorMixin`](../src/main/java/com/pixulse/infx/mixin/ChunkGeneratorMixin.java) 在原版结构尝试开始前拦截。

## 结构集和生物群系资格

| 结构 | 修改内容 |
| --- | --- |
| 要塞 | 重新注册原版 `minecraft:strongholds` 结构集，使用同心环参数：间距 `220`、分离 `3`、每环扩散 `128`；`HAS_STRONGHOLD` 生物群系标签明确覆盖主世界生物群系。 |
| 普通人工矿井、恶地人工矿井 | 主世界保留原版 `HAS_MINESHAFT` 和 `HAS_MINESHAFT_MESA` 资格；自定义下界不再被添加到这两个标签，因此不会因该改动在下界生成人工矿井。 |

上述标签由 [`ModBiomeTagsProvider`](../src/main/java/com/pixulse/infx/datagen/ModBiomeTagsProvider.java) 生成，结构集由 [`ModWorldGen`](../src/main/java/com/pixulse/infx/datagen/ModWorldGen.java) 注册。

## 结构生成后处理

| 结构 | 修改内容 |
| --- | --- |
| 沙漠神殿 | 新区块加载后，识别结构内的宝箱；宝箱上方空间可替换且墙上火把能够存活时，放置一支朝向神殿中心的墙上火把，避免宝箱位置过暗。 |

实现位于 [`StructureSafetyEvents`](../src/main/java/com/pixulse/infx/event/StructureSafetyEvents.java)。

## 结构战利品

以下是结构宝箱的额外或过滤规则，不改变结构本身的空间生成位置：

| 结构或宝箱表 | 修改内容 |
| --- | --- |
| 旧结构 `simple_dungeon`、`abandoned_mineshaft`、`nether_bridge`、沙漠/丛林神庙和要塞箱 | 追加 MITE 对应奖励池，保留 MITE 的权重、滚数和数量范围；现有马铠、锈铁补充表继续单独追加，重复条目从 MITE 池排除。 |
| 古城、堡垒遗迹、埋藏的宝藏、末地城、雪屋、前哨站、残破传送门、沉船、海底废墟和林地府邸 | 使用 MITE 地牢、矿井、下界堡垒或要塞主题奖励池追加战利品。 |
| 村庄职业箱 | 48 个实际结构箱目标均有 `AddTableLootModifier`；铁匠、制图师、制箭师、石匠和食物职业箱使用各自的 MITE 子池。 |
| 试炼密室 `reward`、`reward_ominous` | 分别追加普通/稀有和不祥奖励；只修改两个实际入口，不对子表 `reward_common`、`reward_rare` 等重复执行。 |
| MITE 资源映射 | 铜/金粒和锭转换为铜币/金币，铁粒和锭转换为银币，银资源保留为 InfX 银粒/银锭，完整钻石/绿宝石转换为 1--5 个碎片；丝线、空碗、洋葱、奶酪、桶和装备使用对应 InfX/原版兼容物品。 |
| 所有原版结构箱 | [`ModernProgressionLootFilter`](../src/main/java/com/pixulse/infx/loot/ModernProgressionLootFilter.java) 先把铁/钻石工具、武器、板甲、马铠、长矛、鹦鹉螺甲和原版锁链甲转换为远古金属装备，再移除剩余原版进度物品。[`MiteProgressionLootFilter`](../src/main/java/com/pixulse/infx/loot/MiteProgressionLootFilter.java) 在补充表之后执行第 10/20 天与 `Y=48` 门槛。 |
| 古迹废墟 | 不存在 `chests/trail_ruins_*` 箱表修改；`archaeology/trail_ruins_common` 和 `archaeology/trail_ruins_rare` 不修改。 |

结构战利品由 [`ModGlobalLootModifierProvider`](../src/main/java/com/pixulse/infx/datagen/ModGlobalLootModifierProvider.java) 及其对应的 Loot Modifier/Data Provider 生成。

## 规则语义

- 结构与维度同时匹配多条门禁时，所有门禁都必须满足。
- 没有匹配门禁的结构仍按原版规则生成。
- 玩家进度门禁由服务器线程刷新为不可变快照，区块生成线程只读取快照。
- 详细的门禁 DSL、线程模型和测试说明见 [`structure-generation-gates.md`](structure-generation-gates.md)。
