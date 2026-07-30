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
| 普通地牢 `simple_dungeon` | 添加 InfiniteX 坐骑护甲和锈铁装备；在自定义下界（Underworld）中额外加入古代金属八次抽取池。 |
| 下界要塞 `nether_bridge` | 添加 InfiniteX 坐骑护甲。 |
| 沙漠神殿 `desert_pyramid` | 添加 InfiniteX 坐骑护甲；满足第 40 天且世界完成 `bookcase` 后，有机会获得世界唯一创建之书。 |
| 丛林神庙 `jungle_temple` | 添加 InfiniteX 坐骑护甲；满足创建之书条件后，有机会获得世界唯一创建之书。 |
| 要塞走廊、图书馆 `stronghold_corridor`、`stronghold_library` | 走廊添加 InfiniteX 坐骑护甲；图书馆满足条件后有机会获得世界唯一创建之书。 |
| 废弃矿井 `abandoned_mineshaft` | 添加锈铁链和锈铁工具装备。 |
| 古城、堡垒遗迹、埋藏的宝藏、末地城、雪屋、掠夺者前哨站、残破传送门、沉船、`trail_ruins_*`、试炼密室、海底废墟、村庄、林地府邸 | [`ModernContentAuditEvents`](../src/main/java/com/pixulse/infx/player/ModernContentAuditEvents.java) 会在战利品表加载时取消对应的原版结构箱表，避免现代进度物品绕过 InfiniteX 规则。 |
| 所有原版结构箱 | [`ModernProgressionLootFilter`](../src/main/java/com/pixulse/infx/loot/ModernProgressionLootFilter.java) 会移除绕过 InfiniteX 材料和制作进度的原版物品，例如原版矿物锭、钻石装备、鞘翅和试炼密室钥匙。 |

结构战利品由 [`ModGlobalLootModifierProvider`](../src/main/java/com/pixulse/infx/datagen/ModGlobalLootModifierProvider.java) 及其对应的 Loot Modifier/Data Provider 生成。

## 规则语义

- 结构与维度同时匹配多条门禁时，所有门禁都必须满足。
- 没有匹配门禁的结构仍按原版规则生成。
- 玩家进度门禁由服务器线程刷新为不可变快照，区块生成线程只读取快照。
- 详细的门禁 DSL、线程模型和测试说明见 [`structure-generation-gates.md`](structure-generation-gates.md)。
