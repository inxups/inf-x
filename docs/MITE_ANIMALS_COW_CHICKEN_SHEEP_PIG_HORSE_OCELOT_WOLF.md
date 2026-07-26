# MITE R196 动物机制详解（基于源码）

本文基于 `codex/reference/mite-src` 中的原始 MITE 源码，详细分析以下动物：

**牛 (Cow)**、**鸡 (Chicken)**、**羊 (Sheep)**、**猪 (Pig)**、**马 (Horse)**、**豹猫 (Ocelot)**、**狼 (Wolf 及变体)**。

---

## 1. 通用家畜系统（EntityLivestock）

所有主要家畜（Cow / Chicken / Pig / Sheep）都继承自 `EntityLivestock extends EntityAnimal`。

### 核心属性（0~1 浮点）
- `food`（饥饿）
- `water`（口渴）
- `freedom`（空间/自由度）
- `isWell()` = `min(food, water, freedom) >= 0.25`

### 需求维持与产出
- 每 100 tick 以 90% 概率调用 `updateWellness()`：
  - 靠近食物/水/不拥挤 → +0.1
  - 否则 → -0.005
- `production_counter` 仅在上述更新成功且 `isWell() && !child` 时增长。
- 抽象方法 `produceGoods()` 由子类实现具体产出。

### 繁殖守卫
```java
public void func_110196_bT() {
    if (this.isWell())          // 必须三个需求都满足
        super.func_110196_bT(); // 才真正进入 love 模式
}
```

### 粪肥（Manure）
- 非幼年、非极度饥饿时周期掉落。
- 默认周期 24000 tick。
- Pig / Sheep：周期 ×2。
- Chicken：周期 ×16。

### 共同 AI（Livestock 自动添加）
- `EntityAISeekFoodIfHungry`
- `EntityAISeekWaterIfThirsty`
- `EntityAISeekOpenSpaceIfCrowded`
- `EntityAIAvoidPotentialPredators`
- `EntityAISeekShelterFromRain`
- `EntityAIGetOutOfWater`
- + 原版 `EntityAIMate`、`EntityAITempt` 等

### 月相影响（World 层面）
- **Blood Moon**（每 32 天夜间）：敌对生成加强、狼敌对。
- **Blue Moon**（每 128 天）：影响驯服成功率、Squid 行为、DireWolf 主动性。

---

## 2. 牛（EntityCow extends EntityLivestock）

### 基础
- 尺寸 0.9×1.3，生命 20，移速 ~0.2。
- 初始 `setMilk(100)`。

### 产奶机制
- `produceGoods()`：
  ```java
  this.setMilk(this.getMilk() + this.production_counter);
  this.production_counter = 0;
  ```
- 幼崽返回 0 奶。
- 奶量存储在 dataWatcher（0~100）。

### 食物来源
- 草 + 黄色花（`Block.plantYellow`）。

### 掉落
- 总是掉皮革。
- **仅 `isWell()` 时** 掉生/熟牛肉（数量受击杀加成影响）。

### 交互
- 使用空桶可挤奶（源码中通过 `interact` / 桶机制实现，牛奶桶有材质区分）。

### AI
- 标准 Livestock AI + `EntityAITempt(wheat)`、`EntityAIFollowParent`、`EntityAIFleeAttackerOrPanic`。

---

## 3. 鸡（EntityChicken extends EntityLivestock）

### 基础
- 尺寸 0.3×0.7，生命 4，移速 0.25。
- 粪便周期是普通家畜的 **16 倍**（`setManurePeriod(*16)`）。

### 产出（双轨）
```java
public void produceGoods() {
    if (production_counter >= 100 && rand.nextInt(500) == 0) {
        gainFeather();                    // 羽毛
        production_counter -= 100;
    } else if (production_counter >= 200 && rand.nextInt(20) == 0) {
        dropItem(Item.egg);               // 下蛋
        production_counter -= 200;
    }
}
```

### 羽毛系统
- `max_num_feathers`（1~2，随机）。
- `num_feathers` 记录当前库存。
- `gainFeather()`：库存未满则 +1，否则直接掉落。
- 受伤被击退、跳跃（非水下、非幼年）时可能掉羽毛。

### 掉落
- 死亡掉当前 `num_feathers` 数量的羽毛。
- **仅 `isWell()` 时** 掉生/熟鸡肉。

### 特殊
- 不会受到摔落伤害。
- 飞行动画独立更新（`field_70886_e` 等）。

---

## 4. 羊（EntitySheep extends EntityLivestock）

### 基础
- 尺寸 0.9×1.3，生命 8，移速 ~0.23。
- 粪便周期 ×2。

### 产出机制
- `produceGoods()` 直接置零 → **不通过生产计数产出**。
- 产出 = **手动剪毛**（由玩家使用剪刀触发）。

### 强制剪毛
```java
public void onEntityDamaged(DamageSource ds, float amount) {
    if (ds.isFireDamage() && !getSheared()) { ... setSheared(true); }
    else if (ds.getResponsibleEntity() instanceof EntityGelatinousCube) { setSheared(true); }
    else if (ds.isGelatinousSphereDamage()) { setSheared(true); }
}
```

### 掉落
- 未剪毛且未燃烧时可能掉对应颜色羊毛。
- **仅 `isWell()` 时** 掉生/熟羊肉。
- 总是可能额外掉 1 皮革（50% 概率）。

### 其他
- 可染色（`tryDyeing`）。
- 有 `aiEatGrass` 任务。

---

## 5. 猪（EntityPig extends EntityLivestock）

### 基础
- 尺寸 0.9×0.9，生命 10，移速 0.25。
- 粪便周期 ×2。

### 产出
- `produceGoods()` 直接置零 → **几乎无产出**。
- 主要作用：粪肥 + 骑乘 + 胡萝卜钓竿控制。

### 特殊交互
- 大量不同材质的胡萝卜钓竿（Flint ~ Adamantium）都可诱惑。
- 骑乘时使用胡萝卜钓竿控制。

### 掉落
- 死亡掉生/熟猪肉（`isWell()` 时数量更多）。
- 鞍存在时额外掉鞍。

### 其他
- 被闪电击中会变成猪僵尸（EntityPigZombie）。

---

## 6. 马（EntityHorse extends EntityAnimal）—— 独立复杂系统

**重要**：马 **不继承 EntityLivestock**，因此没有 food/water/freedom / isWell() 系统。

### 核心状态
- `temper`（0~100）：驯服进度。
- `rebellious_for_eating_counter`（4000 tick）：未驯服时拒绝喂食。
- `rebellious_for_riding_counter`（同上）：拒绝上马。
- `hasReproduced`、`isChested`、`isHorseSaddled` 等 flag。

### 驯服与喂食
- 未驯服时喂食（小麦、糖、面包、苹果、金胡萝卜、金苹果、干草等）可：
  - 回血
  - 增加成长
  - 增加 temper
- 喂食后若仍未驯服，会设置 4000 tick 叛逆计数。
- 成功驯服后清零两个叛逆计数。

### 繁殖限制
```java
private boolean canHorseMateAtThisMoment() {
    return riddenByEntity == null && ridingEntity == null &&
           isTame() && isAdultHorse() && !canHorseNeverBreed() &&
           getHealth() >= getMaxHealth();
}
```
- 僵尸马 / 骷髅马 / 骡 不能繁殖。
- 只能同种或普通马+驴 → 骡。

### 骑乘与跳跃
- 必须已驯服 + 已上鞍。
- `jumpPower` 由玩家蓄力控制（0.4 ~ 1.0）。
- 后蹄立起（rearing）会影响控制与跳跃。
- 骑乘时移速受类型影响（普通马最快）。

### 护甲与箱子
- 普通马可装备马铠（铜~远古金属）。
- 驴/骡可装箱子（17 格）。
- 死亡时掉落箱内物品。

### 掉落
- 皮革 + 生/熟牛肉（普通马）。
- 僵尸马掉腐肉/骨头，骷髅马掉骨头。

### AI 与行为
- 注册了 Livestock 风格的避险 AI（SeekShelterFromRain、GetOutOfWater、AvoidPotentialPredators）。
- 害羞（未驯服且 temper 低）时会拒绝吃草。
- 有“最接近马”繁殖选择器。

### 月相
- 没有 Livestock 的 isWell 限制，但仍受整体月相生成/驯服规则影响。

---

## 7. 豹猫（EntityOcelot extends EntityTameable）

### 基础
- 尺寸 0.6×0.8。
- 目标：Chicken、Bat（750 tick 冷却）。

### 驯服
- 使用生鱼 / 大鱼诱惑。
- 驯服后可设置不同皮肤（dataWatcher 18）。
- 驯服后名称显示为“Cat”。

### 繁殖
- **仅已驯服** 的豹猫才能进入 love 模式并繁殖。
- 幼崽继承主人和皮肤。

### AI
- `EntityAITempt`（鱼）
- `EntityAIAvoidEntity`（玩家，16 格）
- `EntityAIOcelotSit`
- `EntityAIOcelotAttack`
- 随机生成时有 1/7 概率同时生成 2 只幼崽。

### 生成
- `getCanSpawnHere`：Y >= 63，脚下为草或树叶，1/3 概率直接拒绝。

### 食物
- 仅生鱼和大鱼。

---

## 8. 狼（EntityWolf 及变体）

### 基础（EntityWolf extends EntityTameable）
- 生命：驯服 12，未驯服 8。
- 攻击 3，移速 0.4。
- 跟随范围：驯服 32，未驯服 16。

### 攻击目标
- 非驯服时：Chicken / Sheep / Pig / Cow + 僵尸系。
- `preysUpon(EntityAnimal)`。

### 驯服
- 使用骨头。
- `getTamingOutcome` 受随机 + 玩家等级影响。
- 蓝月夜间失败有更高概率攻击玩家。

### 血月行为
```java
if (!this.isTamed() && this.worldObj.isBloodMoon(true)) {
    this.setHostileToPlayers(true);
}
```

### 其他
- 项圈可染色。
- 可坐下。
- 被主人攻击时不会反击。
- 驯服后生命/属性会重新应用。

### 变体

#### DireWolf
- 继承 Wolf。
- 未驯服时生命 16、攻击 5。
- 驯服后生命 24、跟随 32。
- 非蓝月夜间会主动寻找玩家攻击。

#### Hellhound（implements IMob）
- 继承 Wolf。
- 生命 20、攻击 4、移速 0.4。
- **清空所有任务**后重新注册：
  - 攻击玩家
  - 攻击动物
- 免疫火、岩浆。
- 永远敌对。

---

## 9. 快速对比表

| 动物     | 继承 Livestock? | 核心产出          | 健康 gating | 特殊机制                     | 月相关键影响          |
|----------|-----------------|-------------------|-------------|------------------------------|-----------------------|
| Cow     | 是             | 奶（0~100）      | 是         | 挤奶                         | 繁殖/产出             |
| Chicken | 是             | 蛋 + 羽毛        | 是         | 羽毛库存、跳跃掉毛           | 繁殖/产出             |
| Sheep   | 是             | 羊毛（手动剪）   | 仅肉        | 火/胶质强制剪毛              | 繁殖/肉质量           |
| Pig     | 是             | 无（仅粪肥）     | 是         | 大量胡萝卜钓竿、闪电变猪僵尸 | 繁殖                  |
| Horse   | **否**         | 无               | 无         | 叛逆计数、护甲、箱子、跳跃蓄力 | 驯服/生成（间接）     |
| Ocelot  | 否（Tameable） | 无               | 无         | 鱼诱惑、仅驯服可繁殖、攻击鸡/蝙蝠 | 生成概率              |
| Wolf    | 否（Tameable） | 无               | 无         | 血月敌对、蓝月驯服惩罚       | 血月敌对、蓝月驯服    |

---

## 10. 总结

MITE 的动物设计核心思想是：

1. **需求驱动（Livestock）**：food / water / freedom → isWell() → 产出 + 繁殖。
2. **健康 gating**：只有“健康”的家畜才能产出高质量资源和繁殖。
3. **月相生态**：Blood/Blue Moon 深刻影响生成、驯服、敌对行为。
4. **马的独立性**：作为最复杂的交通/战斗工具，使用完全不同的叛逆 + 驯服 + 装备系统。
5. **狼的威胁性**：通过血月 + 变体（Dire / Hellhound）制造持续的野外压力。

以上机制共同构成了 MITE R196 极具挑战性的生存动物生态。
