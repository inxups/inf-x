# MITE 怪物机制移植差距分析

> 《MITE 移植指南》的配套机制状态总表。判定以 MITE 源码为准（优先于三份分析文档的描述），InfX 现状以仓库代码为准。
> 源码路径基准与指南 §0.2 一致：MITE = `mc/mite/src/net/minecraft/`，vanilla 1.6.4 = `mc/1.6.4-src/src/minecraft/net/minecraft/src/`，vanilla 26.1.2 = `inf-x/build/moddev/artifacts/minecraft-patched-26.1.2.94-sources/net/minecraft/`，InfX 实现 = `inf-x/src/main/java/com/pixulse/infx/`（下文 InfX 路径省略前缀）。
> 更新：2026-08-17。巨型僵尸按既定决策排除在本文之外。

## 一、缺口状态总表

| # | 机制 | 状态 | InfX 主要入口 | MITE 源 |
|---|---|---|---|---|
| 1 | 蜘蛛 `EntityWeb` 网投射物 | 部分实现（即时放网近似，无投射物） | `entity/InfxSpider.java` | `entity/EntityWeb.java`、`entity/EntityArachnid.java` |
| 2 | 骨王召唤（安全位置/双连招/名额回收/防消失） | 部分实现（统御与概率已有，召唤与回收缺失） | `entity/InfxSkeleton.java` | `entity/EntityBoneLord.java` |
| 3 | 仙人掌击杀计数（根部沙地 0-7 + 衰减） | 部分实现（起爆窗口有，计数体系缺失） | `entity/InfxCreeper.java`、`entity/MonsterEvents.java` | `block/BlockCactus.java`、`entity/EntityCreeper.java` |
| 4 | 骷髅三型分派 + 下界凋灵骷髅生成路径 | 已对齐（0/2 型已实现，凋灵型由原版实体承担，残留参数差） | `entity/InfxSkeleton.java` + 原版 `WitherSkeleton` | `entity/EntitySkeleton.java` |
| 5 | 银甲覆盖率统一规则（毒/吸取/视暗三消费方） | 部分实现（仅夜翼视暗局部实现） | `entity/InfxBat.java` | `entity/EntityLivingBase.java` |
| 6 | 相位蜘蛛闪避伤害过滤 | 部分实现（按来源实体过滤，行为偏差） | `entity/InfxSpider.java` | `entity/EntityPhaseSpider.java` |
| 7 | Shadow / 隐形潜伏者 16 格寻灯 Goal | 部分实现（熄灯有，寻灯移动缺失） | `entity/InfxZombieBase.java` | `entity/ai/EntityAISeekLitTorch.java` |
| 8 | 火元素主动攻击村民 | 完全缺失（有意排除，见注释） | `entity/FireElemental.java` | `entity/EntityFireElemental.java` |

状态口径：**完全缺失** = 无对应代码；**部分实现** = 已有框架但行为与 MITE 有偏差或缺子机制；**已对齐** = 无需主体工作（仅记录残留差异）。展开见 §二（1/2/3/5/6/7/8）与 §三（4 及其余已对齐项）。

## 二、缺口详情

### 2.1 蜘蛛 `EntityWeb` 网投射物

**MITE 规格**

- 发射方（`EntityArachnid.java:107-140, 144-162`）：`num_webs > 0` 且每 `getTicksBetweenWebThrows()`（普通蜘蛛 500 tick / 洞穴·地狱蜘蛛 200 tick，:86-89）判定一次；目标 ≤8 格；眼→眼射线无方块阻挡（被挡则改瞄目标脚 +0.25 高度重验），实体命中恰为目标才发射；弹道预判 lead 10 tick、初速 0.8、零散布、拉弓音效；地狱蜘蛛或本体燃烧时网 `setFire(10)`（:156-159）。
- 弹药：出生 `rand(4)`（0-3），洞穴/地狱蜘蛛不减，其余再 -1，相位蜘蛛恒 0（:27-35）；`num_webs` 写 NBT（:56-69）。
- 投射物碰撞（`EntityWeb.java`）：
  - 命中实体（:32-92）：燃烧网先 `setFire(5)`；按「预测位（lead 4 tick）→ 当前脚位 → 包围盒逐格」三级尝试落网，任一成功即消失；
  - 命中方块（:93-117）：命中格可替换（仅空气/雪层 metadata 0）则落网，否则邻格，再否则 y+1；
  - 水面/液体（:118-126）：水花特效后消失，不落网；
  - 火/岩浆邻格（:128-136）：`burned_up_in_lava` 特效后消失；
  - 燃烧网落网后在 6 向空气格随机点火并点燃网内实体 5 秒（:154-215）。

**当前实现证据**：`entity/InfxSpider.java:273-279`（弹药 `websRemaining`、500/200 节奏、≤8 格、视线，均已对齐）与 `snareTarget`（:286-299）——直接在目标脚格放 `COBWEB`（要求该格为空），地狱蜘蛛改为点燃目标 6 秒；:283-285 注释自认「The complete EntityWeb projectile is not yet available in 26.2」。弹药 NBT（:362-367）与死亡掉丝（:352-358）已有。

**缺失范围**：投射物实体本体与全部碰撞分支（弹道、实体三级落网、方块/邻格判定、水面消失、岩浆焚毁、燃烧网点火），以及「目标脚格非空则网完全无效」的近似偏差。

**推荐实现入口**：注册 `WebProjectile`（继承 `ThrowableItemProjectile`），蜘蛛 `customServerAiStep` 由 `snareTarget` 改为发射；命中逻辑按 MITE 三级落网 + 方块/液体/火分支移植；带火网用投射物 `isOnFire` 状态驱动。

**配置/持久化**：新增 `mobs.spiderWebThrow` 开关（关停时含带火网在内的全部子效果消失）；`num_webs` NBT 已有，沿用。

**验收**：单测——弹药节奏边界（200/500 tick、0-3 发、相位恒 0）；GameTest——网命中奔跑实体落其预测位、命中水面消失不落网、燃烧网落网点火、弹药耗尽不再发射（破坏断言：改 lead 为 0 确认预测位断言会失败）。

### 2.2 骨王召唤（安全位置、50% 双连招、6 名额回收、防消失、20 tick 统御）

**MITE 规格**（`EntityBoneLord.java`）

- 触发（:123-143）：每 20 tick；目标死亡/距离 >16/不可见则清空；`num_troops_summoned < 6` 且目标为玩家时按 `rand(8) < 7 - count` 掷召（名额越空概率越高，7/8 → 2/8）；首召成功后若仍 <6 且 `rand.nextBoolean()`（50%）再召一只（双连招）。
- 安全召唤位置（`trySummonTroop` :190-292）：水平 2-12 格内 `tryCreateNewLivingEntityCloseTo`，尝试上限 `48 - count×8`；落点 4 格内无易感玩家；不得比任何存活玩家更靠近该点（防贴脸召唤）；骷髅眼位→骨王腿（0.25h）/头（0.75h）raycast ≤16 格无阻挡，或可寻路至骨王且终点距离 ≤√2；弓手型须能看见目标，近战型（type 2）另须可寻路到目标。
- 防消失（:294）：召唤成功 `refreshDespawnCounter(-9600)` ≈ 8 分钟不消失；另 :118-121 骨王有玩家目标时保持不消失。
- 统御刷新（:145-185）：16×8×16 内可见骨王的骷髅每秒 `heal(1.0)`；非骨王者 `setFrenziedByBoneLord(target)`（20 tick 刷新，独立于血月狂暴、可叠加）；同型爪牙若「无目标 + 满血 + 5%」或即将自然消失 → 强制 `tryDespawnEntity`，成功则 `--num_troops_summoned`（名额回收）。
- 兵种（:103-106）：普通骨王 → `EntitySkeleton`，远古骨王 → `EntityLongdead`；名额 NBT `num_troops_summoned`（:71-88）。

**当前实现证据**：`entity/InfxSkeleton.java:544-586`——每 20 tick 统御（治疗 1.0、`inspire()` 狂暴并缩短弓 CD 至 40、为无目标爪牙设目标）、16/8/16 范围 + 视线检查（:564-568）均已对齐；`summonedTroops < 6` 与 `rand(8) < 7 - count`（:582-583）、NBT（:634-644）已有。`summonTroop`（:612-632）：±4 格随机落点 + `noCollision` 检查后生成。

**缺失范围**：① 安全召唤位置全套（2-12 格选位、4 格无玩家、不比玩家更近、raycast/寻路可达、尝试上限、分型可见性条件）；② 50% 双连招；③ 名额回收（当前 `summonedTroops` 只增不减，爪牙死亡或闲置不回收，满 6 后永久停召）；④ 防消失（无 -9600 等价处理）；⑤ 召唤未限定玩家目标。

**推荐实现入口**：重写 `summonTroop`（选位 + 可达性校验）；爪牙死亡/强制回收时 `--summonedTroops`（`MobDeathEvent` 或统御 tick 内扫描）；爪牙 `setPersistence` 类处理。

**配置/持久化**：`mobs.boneLord.summon` 开关（同时关停统御治疗/狂暴）；名额、双连招概率入配置。

**验收**：单测——双连招概率函数边界（count=0/5）；GameTest——满 6 不再召、爪牙死亡后名额回收可再召、召唤点 4 格内无玩家、统御治疗与狂暴生效（破坏断言：去掉回收逻辑确认「再召」断言失败）。

### 2.3 仙人掌击杀计数（根部沙地 0-7 + 衰减）与苦力怕起爆窗口

**MITE 规格**

- 计数存储（`BlockCactus.java:215-233`）：仙人掌「根部沙地」方块 metadata 低 3 位（mask 7，取值 0-7）；`getYCoordOfSandBeneath`（:266-277）从仙人掌向下跳过仙人掌列找到沙方块，非沙地（如种在花盆/土上）不计数。
- 递增（:201-206, 236-249）：仙人掌伤害扎死实体（`entityWasDestroyed`）→ 根沙计数 +1，上限 7。
- 衰减（:45-58, 251-264）：随机刻且顶部无仙人掌（成株停止生长）→ 50% 概率 -1。
- 苦力怕窗口（`EntityCreeper.java:195-203` + `EntityAICreeperSwell.java:39-42, 87-90`）：受仙人掌伤害时，若**该仙人掌根沙 killCount > 1** 且 `rand(2)==0`（50%）→ `recently_took_damage_from_conspicuous_cactus = 120` tick；窗口内 1 格内有仙人掌 → 无视目标直接起爆（拆仙人掌）。

**当前实现证据**：`entity/InfxCreeper.java:22,38,62-63,124-135`（`cactusFuseTicks=120`、1 格仙人掌检查）+ `entity/MonsterEvents.java:198-208`（CACTUS 伤害 + `nextBoolean` 50% 布防）+ `entity/InfxCreeperSwellGoal.java:19,42`（swell goal 接入）。**缺 killCount > 1 门与整个计数体系**——当前任何仙人掌扎一下都可能触发，与 MITE「只针对战果 >1 的显眼仙人掌」不同。

**缺失范围**：killCount 递增/衰减/根部沙地存储全套（0-7 边界、随机刻 50% 衰减、非沙地不计）。

**推荐实现入口**：现代仙人掌/沙无 metadata 可复用——建议以仙人掌方块为锚的 `BlockEntity` 计数或 SavedData「沙地位 → 计数」表（26.1 沙无附加状态）；递增挂在苦力怕/生物被 `CACTUS` 伤害致死的事件上，衰减挂仙人掌 `randomTick`。

**配置/持久化**：`world.cactusKillMemory` 开关；计数需随区块持久化（旧档缺字段回 0）。

**验收**：单测——计数 0/1/7/8 边界、衰减概率；GameTest——killCount=1 的仙人掌扎苦力怕不布防、=2 时 50% 统计入单测；窗口内靠近仙人掌必爆（破坏断言：把门改成 >0 确认 =1 用例失败）。

### 2.4 骷髅三型分派与下界凋灵骷髅生成路径（已对齐，残留参数差）

**MITE 规格**：`getRandomSkeletonType`（`EntitySkeleton.java:292-295`）——下界恒为 1（凋灵型；vanilla 1.6.4 为 `nextInt(5) > 0` 即 80%，见 `mc/1.6.4-src/.../EntitySkeleton.java:250`）；否则 Longdead 50% / 普通骷髅 25% 掷为 2（近战型）。type 1：铁剑（poor）+ 攻击重置 4 + 免疫火/岩浆 + 近战附加凋零（:130, 302-308, 485-493）；type 2：木棒起手，day≥10 可换锈铁剑/匕首（:276-283）。

**当前实现证据**：普通骷髅 25% 近战 / 75% 弓分派（`entity/InfxSkeleton.java:198-213`，按张力细化武器，超出 MITE 但方向一致）；Longdead 弓/剑对半（:147-150）；凋灵型由原版 `WitherSkeleton` 承担——下界要塞结构生成原生保留（`docs/structure-generation-gates.md` 仅做 `NETHER_FORTRESS_ENTERED` 进度门控，不改要塞怪物表），装备/掉落由 `mixin/world/entity/monster/WitherSkeletonDropsMixin.java`、`VanillaMobEquipmentMixin.java` 调整；GameTest `ModMonsterGameTests.java:683-691` 断言下界 biome 池为 MITE 同款四项（恶魂/僵尸猪灵/岩浆怪/土元素），凋灵骷髅走要塞 override 与 vanilla 26.1 一致。

**结论**：无主体缺口。残留差异：MITE 凋灵型为骷髅变体（下界内任何骷髅 100% 凋灵化），InfX 用独立原版实体等价承担；若后续要逐参数对齐（poor 铁剑 vs 原版武器、凋零时长 200 tick vs 原版），在 `WitherSkeletonDropsMixin`/属性层微调即可，不新开机制。

### 2.5 银甲覆盖率统一规则（毒 / Wight 经验吸取 / Shadow·Nightwing 视暗）

**MITE 规格**（`EntityLivingBase.java`）

- 统一谓词 `getSilverArmorCoverage()`（:1657-1690）：Σ 每件银质护甲/马铠 `coverage × damageFactor`（按损耗折减）。
- 三个消费方，抗性恒 `coverage × 0.5`（:1692-1710）：
  - **毒**：`getResistanceToPoison`——中毒时长 `scaleDuration(1 - 抗性)` 缩短（:826-828；满银甲 ≈ 毒时长减半）；
  - **Wight 经验吸取**：`getDrainAfterResistance` = `round(drain × (1 - 抗性))`（:1712-1715；消费于 `EntityWight.java:62-66`）；
  - **视暗**：`getResistanceToShadow`——`getAmountAfterResistance(2.0F, 4)`（Shadow，`EntityShadow.java:90-94`）/ `(1.25F, 4)`（Nightwing，`EntityNightwing.java:93-97`）。

**当前实现证据**：仅夜翼局部实现——`entity/InfxBat.java:167-192`（`nightwingDimmingAmount`：coverage = 各银件 `durabilityComponents/24|48 × 耐久因子`，`1.25 × (1 - clamp(coverage)×0.5)`），语义与 MITE 等价但为私有逻辑。其余消费方缺失：蜘蛛/蠹虫毒 `addEffect` 直接给满时长（`entity/InfxSpider.java:245-254`）；Wight 吸经验无减免（`entity/Wight.java:66-70` 直接 `giveExperiencePoints(-max(20,(level+1)*10))`）；Shadow 改用标准 `MobEffects.DARKNESS` 120 tick 且无银甲减免（`entity/Shadow.java:64-72`）。

**缺失范围**：统一覆盖率工具（含损耗折减与满银 =1.0 校准）；毒时长、Wight 吸取、Shadow 视暗三处接入。

**推荐实现入口**：抽 `SilverArmorCoverage`（静态工具：`LivingEntity → float`，复用 `InfxBat` 现有折算并迁移为唯一实现）；消费点分别接入 `InfxSpider.doHurtTarget`（毒时长）、`Wight.doHurtTarget`（drain）、`Shadow.doHurtTarget`（视暗量/时长按 `×(1-coverage×0.5)`）。

**配置/持久化**：`mobs.silverArmorResistance` 开关；无新增持久化。

**验收**：单测——coverage 边界（0、半银、满银、高损耗件、非银件忽略）；GameTest——满银甲玩家被蜘蛛咬中毒时长减半、Wight 吸取减半、Shadow 命中视暗减半（破坏断言：临时固定 coverage=0 确认减半断言失败）。

### 2.6 相位蜘蛛闪避伤害过滤

**MITE 规格**（`EntityPhaseSpider.java:218-244`）：`attackEntityFrom` 中**坠落/火焰/中毒三类伤害明确不闪避**（`isFallDamage || isFireDamage || isPoison → can_evade=false`）；其余伤害在 `num_evasions > 0` 时**先扣次数再尝试** `tryTeleportAwayFrom(威胁方, 3.0)`（传送失败伤害照吃、次数已耗）。弹药式次数：初始 `rand(3)+2`（2-4），每 100 tick 回复 1（上限），写 NBT（:16-17, 25, 41-52, 67-70）。

**当前实现证据**：`entity/InfxSpider.java:311-349`——`source.getEntity() != null` 才进入闪避（坠落/着火/岩浆/中毒等无实体来源天然不触发 ✓ 近似）；2-4 次/每 100 tick 回复/NBT 已对齐（:56-66, 346-348, 362-374）。**偏差**：① 过滤维度是「来源实体」而非「伤害类型」——火附魔武器的实体攻击会被闪避（MITE 中火焰伤害不闪避）；② 次数只在传送成功时扣（MITE 失败也扣）。

**推荐实现入口**：`hurtServer` 过滤条件改为 `source.is(DamageTypeTags.IS_FALL) || source.is(DamageTypeTags.IS_FIRE) || 毒来源` 跳过；消耗时机对齐为「进入闪避分支即扣」。

**配置**：随相位蜘蛛既有开关，无新增。

**验收**：GameTest——火附魔剑命中不被闪避、坠落/火/毒不消耗次数、箭矢命中触发且无论传送成败次数递减（破坏断言：移除火焰过滤确认火伤用例失败）。

### 2.7 Shadow 与隐形潜伏者 16 格寻灯 Goal

**MITE 规格**（`entity/ai/EntityAISeekLitTorch.java`）：`shouldExecute` 每 tick 掷 `rand(40)`（Shadow）/`rand(200)`（其他）≤0；以自身眼高为中心 16 格（垂直 ±4）找火把/激活红石火把/南瓜灯，取最近 8 候选；逐候选寻路（≤16 格），终点周围 `isNearLitTorch`（3×3×(2+身高) 含灯，`EntityLiving.java:1938-1957`）则 `setPath` 走过去；抵达后由每 tick 的 `tryDisableNearbyLightSource`（`EntityLiving.java:1978-2022`：4 格内无玩家 + 未被击中，火把/红石火把清除并掉落本体，南瓜灯变南瓜并额外掉一根火把）熄灯。注册于 `EntityShadow.java:28`、`EntityInvisibleStalker.java:23`。

**当前实现证据**：熄灯侧已对齐——`entity/InfxZombieBase.java:97-122`（`disableNearbyLight`：含壁挂火把/红石火把 `destroyBlock` 掉落、南瓜灯→雕刻南瓜+掉火把、MOB_GRIEFING 门、每 tick 至多 1 个）+ 触发条件（`entity/Shadow.java:89-92`、`entity/InvisibleStalker.java:78-82`：4 格无玩家 + 100 tick 未被击中）。**寻灯移动完全缺失**：两者都只会熄 1 格内的灯，不会主动走向 16 格内的光源。

**推荐实现入口**：新 `SeekLitTorchGoal`（`canUse` 按 1/40|1/200 节流；16 格方块扫描取 8 候选——沿 MITE `getNearestBlockCandidates` 语义，注意扫描频率上限，参考指南 L3 节流要求）；注册进 `Shadow`、`InvisibleStalker` 的 goalSelector，与既有熄灯衔接。

**配置**：`mobs.lightSeekers` 开关（关停时寻灯与熄灯一起关——统一出口）。

**验收**：GameTest——16 格内放火把，Shadow 在期望时间内走近并熄灭；无可达路径时不启动；节流断言（扫描次数/时长上限）。破坏断言：临时把范围改 4 格确认 16 格用例失败。

### 2.8 火元素主动攻击村民

**MITE 规格**：`EntityFireElemental.java:21-22` 注册近战追击 `EntityAIAttackOnCollide(this, EntityVillager.class, 1.0D, true)`（Shadow/Wight/隐形潜伏者同款模式，见 `EntityShadow.java:21`、`EntityWight.java:18`、`EntityInvisibleStalker.java:16`）。注：MITE targetTasks 仅显式索敌玩家（:26），村民目标经由上述近战 goal 及其余入口（如反击链）消费；三份分析文档将「攻击村民」列为该族怪物的标准行为。

**当前实现证据**：`entity/FireElemental.java:59-68`——`registerGoals` 仅 `HurtByTargetGoal` + `NearestAttackableTargetGoal(Player)`，:65 注释明确「villagers are not sought out」。对照组：InfX 隐形潜伏者已主动索敌村民（`entity/InvisibleStalker.java:72`）。

**缺失范围**：火元素对村民的攻击路径整体缺失。

**推荐实现入口**：`targetSelector` 增加 `NearestAttackableTargetGoal<>(this, Villager.class, true)`（沿用 InfX 潜伏者既有做法，行为不弱于 MITE）。

**配置**：`mobs.fireElemental.huntVillagers` 开关。

**验收**：GameTest——附近存在村民与玩家时火元素可将村民设为目标并近战（破坏断言：移除 goal 确认用例失败）。

## 三、原生基础与已对齐机制（动手前先查本表）

指南 §四.1 要求先判定「新机制 / 参数调整 / 已原生」。下表为已核实项，**禁止重复实现**：

| 机制 | 判定 | 证据 |
|---|---|---|
| 僵尸首领属性掷骰 | vanilla 26.1 原生（`Zombie.handleAttributes`）；InfX 0t23 已移除重复 | CHANGELOG 0t23 |
| 僵尸转化 Normal 50% | vanilla 原生（`Zombie.killedEntity`）；0t23 移除双入口 | CHANGELOG 0t23 |
| 万圣节南瓜头 | vanilla 26.2 原生（`finalizeSpawn`）；0t22 移除重复 | CHANGELOG 0t22 |
| 蜘蛛出生药水掷骰（隐身不可达） | MITE 与 InfX 分布一致（移速 50%/力量 25%/再生 25%） | `InfxSpider.java:207-211` ↔ `SpiderEffectsGroupData.java:14-27`；勘误见 §四.2 |
| 骷髅 0/2 型分派、Longdead 50% 近战 | 已实现 | `InfxSkeleton.java:147-150, 198-213` |
| 下界凋灵骷髅自然生成 | vanilla 要塞 `StructureSpawnOverride` 原生；InfX 仅进度门控要塞 + mixin 调装备 | `ModMonsterGameTests.java:683-691`、`WitherSkeletonDropsMixin.java` |
| 火元素湿润 fizz / 水磨损 / 火焰附魔免伤门 | 已实现（`blazeAccepts` 同形免疫） | `FireElemental.java:84-100`、`MobDamageRules.java:125-130` |
| Shadow / Nightwing 免疫门、阳光秒杀、暗处回血 | 已实现 | `Shadow.java:57-88`、`InfxBat.java:75-82, 149-159`、`MobDamageRules.java:156-164` |
| Shadow / 潜伏者身旁熄灯 | 已实现（含南瓜灯→南瓜+掉火把） | `InfxZombieBase.java:97-122` |
| Nightwing 视暗 + 银甲减免 | 已实现（MITE `1.25 × (1-coverage×0.5)` 等价） | `InfxBat.java:126-129, 167-192` |
| Wight 吸经验本体（40%、`max((L+1)×10,20)`） | 已实现（缺银甲减免，见 §2.5） | `Wight.java:66-70` |
| 骷髅拾骨修武器 | 已实现 | `InfxSkeleton.java:224-229`（`MoveToBoneRepairGoal`） |
| 骨王统御治疗/狂暴（`inspire`）与血月叠加 | 已实现 | `InfxSkeleton.java:550-580` |
| 蜘蛛弹药/投掷节奏/死亡掉丝 | 已实现 | `InfxSpider.java:62, 142-152, 352-358` |

## 四、非缺口 / 勘误

1. **巨型僵尸**：按既定决策明确排除，不在移植范围。
2. **普通蜘蛛隐身（勘误）**：MITE 将 vanilla 的 `nextInt(5)` 改为 `nextInt(4)`（`mc/mite/.../SpiderEffectsGroupData.java:14` ↔ `mc/1.6.4-src/.../SpiderEffectsGroupData.java:11`），`var2 ∈ 0-3` 永不满足 `<= 4` 分支——**invisibility 在 MITE 源码中不可达**，实际分布为移速 50%/力量 25%/再生 25%。《MITE 怪物机制深潜》§4.5（`mc/mite/MITE怪物机制深潜.md:230`）所列「移速/力量/再生/隐身」中「隐身」应删去；InfX 现行实现（`InfxSpider.java:207-211`，注释「never invisibility」）与 MITE 一致，不列为缺口。
3. **火元素湿润额外 fizz 伤害、火焰附魔免伤、雷暴光照行为**：不列为缺口——前两项已实现（§三）；雷暴光照按既定计划不收录。
4. **Ghoul 减速 III**：MITE 为 `moveSlowdown` 50 tick、amplifier 5（≈90% 减速，`EntityGhoul.java:55-72`）；InfX 为 amplifier 2（45%，`entity/Ghoul.java:57`）——0t23 有意平衡削弱（「不再近乎硬控」，CHANGELOG 0t23）。作为既有平衡差异记录，不纳入待实现。
