# InfiniteX 当前结构战利品箱奖励统计

> 审计基线：Minecraft `26.1.2`、NeoForge `26.1.2.87`、InfiniteX 提交 `40dfa0b`（2026-07-28）。
>
> 本文统计的是玩家从结构生成的箱子、木桶或被替换的保险箱中能得到的**实际战利品**。它同时覆盖原版表保留内容、InfiniteX 的追加池、运行时替换和被禁用的表；不含生物掉落、考古刷取、普通方块掉落及丛林神庙的陷阱发射器。

## 结论速览

| 项目 | 数量 | 结论 |
| --- | ---: | --- |
| Minecraft `chests/` JSON 表 | 56 | 以 26.1.2 客户端数据包为准；其中含 1 个丛林神庙发射器表。 |
| 被 INF-X 取消加载的结构表 | 46 | 古城、堡垒遗迹、埋藏的宝藏、末地城、村庄等结构不再保留原版表奖励。 |
| 被取消加载的额外非结构表 | 1 | 出生奖励箱 `minecraft:chests/spawn_bonus_chest`。 |
| 保留的原版结构箱表 | 8 | 废弃矿井、普通地牢、沙漠神殿、丛林神庙、下界要塞、要塞走廊/交叉口/图书馆。 |
| INF-X 定向追加池挂接 | 7 | 5 个马铠池目标 + 2 个锈铁池目标；普通地牢同时命中两类池，并在 Underworld 还会额外获得远古金属池。 |
| 运行时改为固定奖励容器的结构 | 1 | 村庄原版箱/木桶表被禁用，但新生成村庄会放入一个铁保险箱。 |

原版“保留”不等于原样保留：`ModernProgressionLootFilter` 会在所有 `minecraft:chests/*` 表结算后移除现代进度物品，且**不会重掷**。因此，被移除的那一次抽取会留下空位。

## 统计口径与通用规则

### 表状态

`ModernContentAuditEvents` 会取消下列目标表的加载；这些容器在 INF-X 中没有该原版表提供的奖励：

- 古城、堡垒遗迹、埋藏的宝藏、末地城、雪屋、掠夺者前哨站、废弃传送门、沉船、试炼密室、水下遗迹、村庄、林地府邸；完整清单见“已禁用结构表”。
- 出生奖励箱，以及猪灵交易（后者不是结构箱）。

所有未被取消的 `minecraft:chests/*` 表还会经过如下过滤：

- 直接移除：原版铜/铁/金/钻石/下界合金材料与锭、绿宝石、下界合金升级模板、盾牌、附魔书、鞘翅、三叉戟、重核、试炼钥匙、收纳袋、图纸台、不死图腾、海洋之心、回响碎片、嗅探兽蛋以及各种原版桶等。
- 移除以 `wooden`、`stone`、`copper`、`iron`、`golden`、`diamond`、`netherite` 开头的原版工具、武器、护甲和马铠。
- INF-X 命名空间的追加物品不受该过滤器影响。

下文中的“期望栈数”只统计对应的 INF-X 追加池，单位是抽到的物品栈，**不**包含基础原版表，也不因背包/箱内堆叠而合并。`命中率`为该追加池至少产出一栈的概率；`2–5` 次表示等概率整数抽取 2、3、4、5 次。

### 保留基础表的原始抽取次数

每个分号分隔的数字对应一条原版 Loot Pool，物品与被过滤项在下文逐表列出。被过滤的结果不会重掷。

| 表 | 原版 Pool 抽取次数 |
| --- | --- |
| `abandoned_mineshaft` | `1`；`2–4`；`3` |
| `simple_dungeon` | `1–3`；`1–4`；`3` |
| `desert_pyramid` | `2–4`；`4`；`1` |
| `jungle_temple` | `2–6`；`1` |
| `nether_bridge` | `2–4`；`1` |
| `stronghold_corridor` | `2–3`；`1` |
| `stronghold_crossing` | `1–4` |
| `stronghold_library` | `2–10`；`1` |

## 可获得奖励的结构

### 废弃矿井

表：`minecraft:chests/abandoned_mineshaft`。普通矿井和恶地矿井均使用此表；INF-X 将两类矿井的 Underworld 生物群系标签改为仅指向 Underworld。

| 来源 | 实际奖励 |
| --- | --- |
| 保留的原版池 | 金苹果、附魔金苹果、命名牌；红石 `4–9`、青金石 `4–9`、煤炭 `3–8`、面包 `1–3`、发光浆果 `3–6`、西瓜/南瓜/甜菜种子 `2–4`；铁轨 `4–8`、动力/探测/激活铁轨 `1–4`、火把 `1–16`。 |
| 被过滤的原版池 | 随机附魔书、铁镐、铁锭、金锭、钻石。 |
| INF-X 追加 | 锈铁池：3 次，空权重 `150`；锈铁锁链权重 `5`；锈铁锹、手斧、斧、鹤嘴锄、镐、战锤各权重 `2`。期望 `0.305` 栈/箱，至少一栈概率 `27.54%`。 |

### 普通地牢（怪物房）

表：`minecraft:chests/simple_dungeon`。

| 来源 | 实际奖励 |
| --- | --- |
| 保留的原版池 | 皮革 `1–5`、金苹果、附魔金苹果、`Otherside`/`13`/`Cat` 唱片、命名牌；面包、小麦 `1–4`、红石 `1–4`、煤炭 `1–4`、西瓜/南瓜/甜菜种子 `2–4`；骨头、火药、腐肉、线各 `1–8`。 |
| 被过滤的原版池 | 原版各级马铠、随机附魔书、铁/金锭、桶。 |
| INF-X 追加：马铠 | 8 次；空 `147`、金马铠 `2`、铜马铠 `5`、铁马铠 `1`（总权重 `155`）。期望 `0.413` 栈/箱，命中率 `34.55%`。 |
| INF-X 追加：锈铁 | 8 次；空 `130`、锈铁剪刀 `3`，锈铁锹、锄、鹤嘴锄、匕首、剑、战斧、战锤各 `2`（总权重 `147`）。期望 `0.925` 栈/箱，命中率 `62.59%`。 |

#### Underworld 中的普通地牢加成

当且仅当上述表在 `infx:underworld` 维度中结算时，`UnderworldDungeonLootModifier` 额外执行 8 次独立抽取。它叠加在普通地牢的原版、马铠和锈铁奖励之上。

| 结果 | 单次权重/概率 | 每箱期望 |
| --- | ---: | ---: |
| 空 | `54` / `54%` | — |
| 远古金属粒 `1–4` | `10` / `10%` | `0.8` 栈，平均 `2.0` 粒 |
| 远古金属锭 `1–4` | `10` / `10%` | `0.8` 栈，平均 `2.0` 锭 |
| 远古金属硬币 | `5` / `5%` | `0.40` |
| 远古金属空桶 | `2` / `2%` | `0.16` |
| 四张 INF-X 唱片（每张） | 每张 `1` / `1%` | 每张 `0.08`，合计 `0.32` |
| 远古金属马铠 | `5` / `5%` | `0.40` |
| 远古金属镐、锹、斧、剑、战锤、弓、锁链头盔、锁链胸甲、锁链护腿、锁链靴（每种） | 每种 `1` / `1%` | 每种 `0.08`，合计 `0.80` |

该池合计期望 `3.680` 栈/箱，至少出现一栈的概率为 `99.28%`。数据生成也会输出等价的 `infx:chests/underworld_dungeon` 表；运行时实际由上述全局 Loot Modifier 的同一权重逻辑追加，而不是由容器直接引用该 INF-X 表。

### 沙漠神殿

表：`minecraft:chests/desert_pyramid`。INF-X 还会为新生成的神殿宝箱上方添加安全火把；这不改变战利品。

| 来源 | 实际奖励 |
| --- | --- |
| 保留的原版池 | 骨头 `4–6`、蜘蛛眼 `1–3`、腐肉 `3–7`、皮革 `1–5`、金苹果、附魔金苹果；骨头/火药/腐肉/线/沙子各 `1–8`；沙丘盔甲纹饰锻造模板 `2`。 |
| 被过滤的原版池 | 钻石、铁/金锭、绿宝石、全部原版马铠、随机附魔书。 |
| INF-X 追加：马铠 | `2–6` 次；空 `65`，铁/银/金马铠各 `1`（总权重 `68`）。期望 `0.176` 栈/箱，命中率 `16.34%`。 |
| INF-X 追加：创世之书 | 满足下文条件时，每次开箱有 `10%` 概率尝试加入一本。 |

### 丛林神庙

表：`minecraft:chests/jungle_temple`。

| 来源 | 实际奖励 |
| --- | --- |
| 保留的原版池 | 竹子 `1–3`、骨头 `4–6`、腐肉 `3–7`、皮革 `1–5`、荒野盔甲纹饰锻造模板 `2`。 |
| 被过滤的原版池 | 钻石、铁/金锭、绿宝石、全部原版马铠、30 级随机附魔书。 |
| INF-X 追加：马铠 | `2–6` 次；空 `60`，铁/银/金马铠各 `1`（总权重 `63`）。期望 `0.190` 栈/箱，命中率 `17.53%`。 |
| INF-X 追加：创世之书 | 满足下文条件时，每次开箱有 `25%` 概率尝试加入一本。 |

`minecraft:chests/jungle_temple_dispenser` 只向陷阱发射器装填 `1–2` 次箭矢；它位于 `chests/` 命名空间但不是奖励箱，故不计入本表。

### 下界要塞

表：`minecraft:chests/nether_bridge`。

| 来源 | 实际奖励 |
| --- | --- |
| 保留的原版池 | 打火石与钢、下界疣 `3–7`、鞍、黑曜石 `2–4`、肋骨盔甲纹饰锻造模板。 |
| 被过滤的原版池 | 钻石、铁/金锭、金剑、金胸甲、全部原版马铠。 |
| INF-X 追加：马铠 | `2–5` 次；空 `50`、金/铜/铁马铠分别为 `8/5/3`（总权重 `66`）。期望 `0.848` 栈/箱，命中率 `60.31%`。 |

### 要塞

要塞仍生成在主世界，以维持末地流程；它使用三张不同的箱表。

| 组件/表 | 保留的原版奖励 | 过滤/INF-X 追加 |
| --- | --- | --- |
| 走廊 `stronghold_corridor` | 末影珍珠、红石 `4–9`、面包/苹果 `1–3`、金苹果、皮革 `1–5`、`Otherside` 唱片、眼眸盔甲纹饰锻造模板 | 过滤钻石、铁/金锭、铁制装备、原版马铠和随机附魔书；另加马铠池：`2–3` 次，空 `188`、铜/铁马铠各 `1`（总权重 `190`），期望 `0.026` 栈/箱、命中率 `2.61%`。 |
| 交叉口 `stronghold_crossing` | 红石 `4–9`、煤炭 `3–8`、面包/苹果 `1–3` | 过滤铁/金锭、铁镐和 30 级随机附魔书；无专属 INF-X 追加池。 |
| 图书馆 `stronghold_library` | 书 `1–3`、纸 `2–7`、地图、指南针、眼眸盔甲纹饰锻造模板 | 过滤 30 级随机附魔书；创世之书条件满足时，每次开箱有 `50%` 概率尝试加入一本。 |

### 村庄：固定铁保险箱，不使用原版村庄表

所有 `minecraft:chests/village/*` 原版表均被取消加载。对于一个新生成的村庄起始区块，INF-X 按区块扫描顺序把遇到的首个原版箱子或木桶替换为铁保险箱，并写入以下固定四格奖励：

| 槽位 | 奖励 | 数量 | 平均数量 |
| ---: | --- | ---: | ---: |
| 1 | 铁粒 | `4–12` | `8` |
| 2 | 铜粒 | `8–20` | `14` |
| 3 | 银粒 | `2–8` | `5` |
| 4 | 铜币 | `1` | `1` |

村庄只会在主世界第 `60` 天及以后、且世界已经制造过铁级或更高级的镐/战锤后生成；因此该保险箱也受这个生成门槛限制。

## 创世之书的共同限制

沙漠神殿、丛林神庙和要塞图书馆的书池均要求：

1. 世界天数至少为第 `40` 天；
2. 世界已拥有 `bookcase` 进度；
3. 世界尚未集齐全部九本创世之书。

可获得的标题为 `Boat`、`Crypt`、`Crystal`、`Dragon`、`Globe`、`Serpent`、`Sphinx`、`Star`、`Temple`，每个标题在一个世界中至多出现一次。每次开箱先按该表概率掷骰；成功后以“战利品表 ID + 区块坐标”锁定该组件，故同一表、同一块内最多成功一次。若此前开箱掷骰失败，之后开启同组件的另一个箱子仍会再次尝试，直到首次成功或九本书已被取完。

## 已禁用结构表

下表的总数为 `46` 张 Minecraft 26.1.2 结构容器表。它们由 `ModernContentAuditEvents` 取消加载，因此没有原版表奖励，也没有 INF-X 追加表挂接到它们。

| 结构 | 被取消的表 ID | 数量 |
| --- | --- | ---: |
| 古城 | `chests/ancient_city`、`chests/ancient_city_ice_box` | 2 |
| 堡垒遗迹 | `chests/bastion_bridge`、`chests/bastion_hoglin_stable`、`chests/bastion_other`、`chests/bastion_treasure` | 4 |
| 埋藏的宝藏 | `chests/buried_treasure` | 1 |
| 末地城 | `chests/end_city_treasure` | 1 |
| 雪屋 | `chests/igloo_chest` | 1 |
| 掠夺者前哨站 | `chests/pillager_outpost` | 1 |
| 废弃传送门 | `chests/ruined_portal` | 1 |
| 沉船 | `chests/shipwreck_map`、`chests/shipwreck_supply`、`chests/shipwreck_treasure` | 3 |
| 试炼密室 | `chests/trial_chambers/{corridor, entrance, intersection, intersection_barrel, supply, reward, reward_common, reward_rare, reward_unique, reward_ominous, reward_ominous_common, reward_ominous_rare, reward_ominous_unique}` | 13 |
| 水下遗迹 | `chests/underwater_ruin_big`、`chests/underwater_ruin_small` | 2 |
| 村庄 | `chests/village/*`（16 张职业/房屋表；见上文的运行时保险箱替换） | 16 |
| 林地府邸 | `chests/woodland_mansion` | 1 |

出生奖励箱 `chests/spawn_bonus_chest` 也被取消，但不是结构表，故未计入上表。古迹废墟使用的是考古可疑方块而不是 `chests/` 箱表；当前 26.1.2 数据包中没有 `chests/trail_ruins_*` 文件，所以不把它计为“零奖励箱”。

## 数据来源与复核入口

| 内容 | 代码/数据来源 |
| --- | --- |
| 全局追加池、挂接目标和优先级 | `src/main/java/com/pixulse/infx/data/ModGlobalLootModifierProvider.java` |
| 马铠权重与次数 | `src/main/java/com/pixulse/infx/data/ModHorseArmorLootSubProvider.java` |
| 锈铁权重与次数 | `src/main/java/com/pixulse/infx/data/ModRustedIronLootSubProvider.java` |
| Underworld 普通地牢的实际追加逻辑 | `src/main/java/com/pixulse/infx/loot/UnderworldDungeonLootModifier.java` |
| 创世之书概率、门槛和唯一性 | `src/main/java/com/pixulse/infx/loot/CreationBookLootModifier.java`、`src/main/java/com/pixulse/infx/world/R196WorldData.java` |
| 原版进度物品过滤 | `src/main/java/com/pixulse/infx/loot/ModernProgressionLootFilter.java` |
| 禁用表清单 | `src/main/java/com/pixulse/infx/progression/ModernContentAuditEvents.java` |
| 村庄保险箱和生成门槛 | `src/main/java/com/pixulse/infx/world/R196VillageProgression.java`、`src/main/java/com/pixulse/infx/mixin/ChunkGeneratorMixin.java` |
| 26.1.2 原版基础表 | Gradle 缓存中的 `minecraft_26.1.2_client.jar`：`data/minecraft/loot_table/chests/*.json` |

生成数据由 `ModDataGenerators` 注册。若后续调整结构表、全局 Loot Modifier、过滤器或村庄保险箱物品，应同时更新本文的数量、权重和命中率。
