# MITE R196 vs InfiniteX 动物机制对比

本文将 **MITE 原始源码**（`codex/reference/mite-src`）中的动物实现，与 **InfiniteX (infx)** 当前代码（`src/main/java/com/pixulse/infx/entity` 等）进行逐一对比。

**对比范围**：牛、鸡、羊、猪、马、豹猫、狼（含变体）。

> **2026-07-26 更新**：第 1 节和第 9 节的表格反映当前实现。后续动物细节中仍提及 `diseased`、旧剪毛门槛或旧粪肥门槛的段落是迁移前的对比记录，已由本次 MITE 1.6 对齐取代。

---

## 1. 整体架构差异

| 维度               | MITE (原版)                              | InfiniteX (infx)                              | 备注 |
|--------------------|------------------------------------------|-----------------------------------------------|------|
| 基类               | EntityLivestock（Cow/Chicken/Sheep/Pig） | 原版 Animal（Cow/Chicken/Sheep/Pig）+ 事件叠加 | infx 不修改原版实体类 |
| 需求模型           | food/water/freedom 三个 0~1 浮点         | 同样持久化三个浮点，自然生成以 0.8~1.0 开始 | 使用 26.2 的 PersistentData |
| 健康判定           | isWell() = min(...) >= 0.25              | isWell() = min(food, water, freedom) >= 0.25 | 客户端同步标志用于病皮 |
| 产出 gating        | isWell() + production_counter            | 三项健康度限制牛奶/鸡蛋/羽毛；不健康时仅移除肉类掉落 | 生产模型仍有差异 |
| 繁殖 gating        | isWell() 才能进入 love 模式              | canBreed() = isWell()                         | 不再另加月相限制 |
| 疾病系统           | 无显式疾病（仅 well 值下降）             | 无显式疾病；不健康映射为病皮          | 已移除传播/自愈与专用指令 |
| AI 实现            | 原生 EntityAI* 任务（SeekFood 等）       | 自定义 NeedsGoal（寻路到水/食物/安全点）+ AvoidEnemy | 效果相似，实现不同 |
| 马的地位           | 独立复杂系统（不继承 Livestock）         | 原版 AbstractHorse + 轻量规则（避开、冷却、掉牛肉） | 保真度最低 |
| 豹猫               | 完整驯服/攻击/繁殖逻辑                   | 无自定义（仅丛林生成）                        | infx 未移植 |
| 狼变体             | DireWolf / Hellhound                     | DireWolf / Hellhound（基于 R196Wolf）         | 机制类似 |
| 月相影响           | Blood/Blue 影响生成、驯服、狼行为        | 生成、驯服、狼行为等世界机制保留月相影响 | 家畜 wellness 不再受月相干预 |
| 数据持久化         | 原生 dataWatcher + NBT                   | PersistentData（infx_* 键）                   | 现代方式 |

---

## 2. 牛（Cow）

### MITE
- `produceGoods()` 将 `production_counter` 累加到 `milk`（0~100）。
- 幼崽返回 0 奶。
- 仅 `isWell()` 时掉牛肉。
- 食物来源：草 + 黄色花。
- 使用桶挤奶（有材质区分）。

### InfiniteX
- 无累加奶值。
- 每天最多 4 单位奶（`MILK_DAY` + `MILK_UNITS`）。
- 仅 `isProductive(cow)` 时允许挤奶（包括原版桶和 R196 材质桶）。
- 健康时掉牛肉；不健康仍保留皮革掉落。
- 注册 NeedsGoal + 健康时把 MAX_HEALTH 设为 20。

### 关键差异
- MITE 是“奶量累加”模型；infx 是“每日配额”模型。
- 挤奶限制更严格（同一天内多次挤奶被拒绝）。

---

## 3. 鸡（Chicken）

### MITE
- 双轨产出：
  - 羽毛：生产计数 >= 100 时小概率掉（gainFeather 管理库存）。
  - 蛋：生产计数 >= 200 时小概率掉。
- `num_feathers` / `max_num_feathers` 控制死亡掉毛量。
- 受伤/跳跃时可能掉羽毛。
- 粪便周期 ×16。
- 仅 well 时掉鸡肉。

### InfiniteX
- 蛋：非 productive / 幼年 / 鸡骑士 时锁定 eggTime = 1200。
- 满月加速下蛋、新月减速。
- 羽毛：productive 时每 96_000 tick 掉 1 根（NEXT_FEATHER）。
- 受伤时加速下次掉羽毛。
- 粪便周期 384_000（最长）。
- productive 时才掉羽毛/允许正常下蛋。

### 关键差异
- MITE 用“生产计数 + 库存”模型；infx 用“定时 + 月相调速”模型。
- infx 没有羽毛库存概念，直接定时掉落。
- 蛋延迟机制更激进（直接锁 eggTime）。

---

## 4. 羊（Sheep）

> 当前实现：羊毛只由是否已剪毛决定；不健康仅移除羊肉掉落，仍保留羊毛和皮革。

### MITE
- `produceGoods()` 置零 → 不产东西。
- 产出 = 手动剪毛。
- 火伤 / 胶质方块 / 凝胶球 会强制剪毛。
- well 时掉羊肉 + 可能掉羊毛（未剪）+ 皮革。

### InfiniteX
- 剪毛仅取决于未被剪的羊毛状态，不受 `isWell()` 限制。
- 火伤 或 酸性伤害 强制剪毛（`setSheared(true)`）。
- 死亡时可能额外掉皮革；不健康时不掉羊肉。

### 关键差异
- 核心 gating 从 MITE 的“未剪毛状态”变成了 infx 的“productive 状态”。
- 强制剪毛触发条件几乎一致（火 + 胶质类）。
- infx 增加 diseased 阻断剪毛。

---

## 5. 猪（Pig）

### MITE
- `produceGoods()` 置零。
- 粪便周期 ×2。
- 大量不同材质胡萝卜钓竿可诱惑。
- 闪电变猪僵尸。
- well 时掉猪肉。

### InfiniteX
- 粪便周期 48_000（与 MITE ×2 一致）。
- 仅保留原版行为 + productive 时掉肥料。
- 死亡掉猪肉（无额外规则）。
- 无胡萝卜钓竿或闪电特殊处理记录。

### 关键差异
- infx 对猪几乎无改动（仅肥料）。
- MITE 的骑乘/诱惑细节未特别强化。

---

## 6. 马（Horse / AbstractHorse）

### MITE（EntityHorse，独立系统）
- 不继承 Livestock，没有 food/water/freedom。
- temper + 叛逆计数（eating 4000 tick / riding 4000 tick）。
- 完整驯服、喂食、繁殖、护甲、箱子、跳跃蓄力、死亡掉箱内物品。
- 注册避险 AI（SeekShelterFromRain 等）。
- 僵尸马/骷髅马/骡有特殊限制。

### InfiniteX
- 使用原版 AbstractHorse。
- 仅叠加：
  - 未驯服时玩家靠近会逃（AvoidEntityGoal）。
  - 驯服失败有 4000 tick 冷却（HORSE_RETRY）。
  - 死亡掉牛肉（1 + rand(3)）。
- 无 temper、护甲、箱子、叛逆等移植。
- 护甲掉落由单独的 HorseArmorLootSubProvider 处理。

### 关键差异
- **保真度最低** 的动物。
- MITE 是完整独立系统；infx 只做了“最小必要规则”覆盖。
- 很多 MITE 特色（叛逆、护甲种类、跳跃蓄力细节）尚未移植。

---

## 7. 豹猫（Ocelot）

### MITE
- 完整驯服逻辑（鱼诱惑）。
- 仅已驯服个体可繁殖。
- 攻击 Chicken / Bat。
- 丛林生成 + 特殊生成规则（1/3 概率拒绝，Y>=63，脚下草/树叶）。
- 驯服后可换皮肤，名称显示 Cat。

### InfiniteX
- **完全无自定义代码**。
- 仅在 R196SpawnsBiomeModifier 中丛林额外生成 1 只（`EntityTypes.OCELOT`）。
- 无驯服、繁殖、攻击、皮肤等规则变更。

### 关键差异
- MITE 有完整驯服生态；infx 仅保留原版 + 生成调整。
- 目前 infx 完全未移植 Ocelot 的 MITE 行为。

---

## 8. 狼（Wolf 及变体）

### MITE
- Wolf：血月时未驯服变敌对；蓝月影响驯服成功率。
- 攻击目标：Chicken/Sheep/Pig/Cow + 僵尸系。
- DireWolf：非蓝月夜间主动攻击玩家，属性更高。
- Hellhound：继承 Wolf + IMob，清空任务后重新注册攻击玩家/动物，免疫火/岩浆。

### InfiniteX
- 使用原版 Wolf + 自定义 R196Wolf。
- **Hellhound**：永久敌对、着火免疫、攻击概率点燃。
- **DireWolf**：可驯服，驯服后生命/跟随范围提升。
- 血月：未驯服狼敌对玩家 + 驯服成功率降低。
- 蓝月：驯服更容易。
- 生成：R196SpawnsBiomeModifier 在森林/针叶林/雪地生成 Wolf + DireWolf。

### 关键差异
- 变体命名与属性基本对齐。
- MITE 的 Hellhound 通过清空任务实现；infx 通过独立实体类型 + 自定义行为。
- 月相影响在两边都很强，但 infx 更系统化（R196MoonPhase 统一处理）。

---

## 9. 其他共性机制对比

| 机制         | MITE                          | InfiniteX                              |
|--------------|-------------------------------|----------------------------------------|
| 粪肥         | 成年、food >= 0.05 时推进倒计时 | 同样使用持久倒计时与 food >= 0.05 条件 |
| 恐慌传播     | 受伤后附近 Livestock 恐慌     | R196Livestock.panic() 传播 400~799 tick，且不改变 wellness |
| 疾病         | 无显式状态                    | 无显式状态，仅 wellness 下降            |
| 繁殖月相     | well 时可繁殖                 | well 时可繁殖，无额外月相门槛           |
| 生成限制     | 原版群系规则 + 月相调整       | 完全替换群系表 + 仅蓝月生成动物        |
| 掉落修改     | well 时掉肉，其他掉落保留     | 同样只移除不健康家畜的肉类掉落          |

---

## 10. 总结与保真度评估

| 动物   | 保真度 | 主要缺失 / 差异 |
|--------|--------|-----------------|
| 牛     | 高     | 奶模型从“累加”改为“每日配额” |
| 鸡     | 高     | 羽毛从库存改为定时 + 月相调速 |
| 羊     | 高     | 胶质类强制剪毛仍是近似实现 |
| 猪     | 中     | 几乎无改动（仅肥料） |
| 马     | 低     | 仅轻量规则，缺少完整驯服/叛逆/护甲/箱子系统 |
| 豹猫   | 低     | 完全未移植（仅生成） |
| 狼     | 高     | 变体 + 月相影响基本对齐，实现方式不同 |

**总体评价**：
- Livestock 核心需求/产出/月相 gating **高度对齐**（只是实现从原生 AI 迁移到事件 + NeedsGoal）。
- **马和豹猫** 是当前最大的保真度缺口。
- infx 引入了 **diseased** 状态，这是对 MITE 的扩展而非直接移植。
- 生成与月相系统在 infx 中被大幅强化和统一。

---

**来源**：
- MITE：`codex/reference/mite-src/net/minecraft/entity/{EntityLivestock,EntityCow,...}.java`
- InfiniteX：`src/main/java/com/pixulse/infx/entity/{R196Livestock,R196AnimalEvents,R196ManureEvents,R196Moon*}.java` + 群系修改器
- 文档生成日期：2026-07-24
