# Changelog

## 0v3
### 模组问题修复（第二批）
- 补充红砂岩→玻璃的熔炉烧炼配方，与砂岩→玻璃一致。
- 补回玻璃瓶合成配方（3 玻璃→3 玻璃瓶）。
- 移除古尸、古尸守卫、远古骨王的远古金属锭掉落（仍保留其远古金属装备掉落）。
- 狼、惧狼、地狱犬恢复 MITE 经验掉落（狼 5、惧狼 10、地狱犬 15）；其他动物仍不提供经验。
- 新增 9 种金属/材料箭的无序分解配方（1 箭→1 粒/碎片），与 MITE 拆解规则一致。
- 肥料右键耕地/作物时交互在双端消费：客户端挥臂反馈并保证请求到达服务器，服务器记录耕地肥力。
- 修复金苹果、水瓶放入附魔台时右侧等级选项不亮起的问题（转换选项现在按费用与货币正常亮起）。
- 驴与骡死亡时与马一致掉落牛肉（1-3 个），屠宰附魔对其同样生效。

## 0v2
### 模组问题修复（作物与家畜）
- 新增洋葱作物：洋葱本身即可作为种子种植，成熟收获 2 个，并有 25% 几率额外获得 1 个。
- 羊死亡时羊毛掉落率从 100% 调整为 MITE 的 50%（羊皮仍为 50%）。
- 原版山羊现在可以用空桶（含金属空桶）挤奶，与牛共用每日 4 单位的奶配额。
- 猪现在会吃棕色蘑菇（繁殖、诱惑与就近觅食均生效）。

### 模组问题修复（合成配方）
- 补回被删除的原版基础配方：碗（3 木板→4）、羊毛（4 线→1）。
- 恢复 16 色主染料与组合染料配方（骨粉→白、墨囊→黑、植物→各色、双色/三色合成）。
- 补回粗铜/粗铁/粗金块的 3×3 合成与拆回 9 个原材料的双向配方。
- 补回甜菜汤、兔肉煲、曲奇（8 个）、西瓜种子、小麦种子配方。

### 模组问题修复（方块与挖掘）
- 矮枯草丛、高枯草丛、灌木丛、萤火虫灌木丛、垂泪藤、缠怨藤现在拥有 0.02 硬度。
- 剪刀左键现在只拦截非剪刀有效方块；树叶、羊毛、植物与蜘蛛网等可用剪刀左键破坏。
- 创造模式下右键剪刀可正常精准采集掉落，且不消耗剪刀耐久。
- 创造模式手持剑不再能破坏普通方块，只保留剑有效方块的破坏能力。
- 蜘蛛网可以被空手或剑破坏但不再掉落；只有剪刀破坏时才掉落 1 根线。
- 用剪刀雕刻南瓜现在只掉落 1 个南瓜种子。

### 模组问题修复（食物与生存）
- 腐肉（80% 几率）与生鸡肉（30% 几率）食用后现在会附加 MITE 中毒 I（200 tick），并保留饥饿效果。
- 中毒生效间隔改为 MITE 的 100 >> 等级 tick/次，被毒死时显示专属死亡文案“毒发身亡”。
- 物品实体落在点燃的营火上时会以 1 进度/tick 烹饪，100 进度烤熟；熟食在营火上不再烧毁。
- 牛奶桶中文名由“牛奶”统一改为“奶”（如铁奶桶）。

### 模组问题修复（附魔与装备）
- 恢复原版锋利、横扫之刃、迅捷潜行附魔：剑与镰刀可附锋利/横扫，靴子可附迅捷潜行。
- 剑与镰刀攻击现在自带横扫效果（50% 伤害），横扫之刃每级 +25%。
- 银制武器对亡灵 +25% 伤害的提示现在常驻显示；银制盔甲每件使负面效果时长缩短 15%。
- 战锤与短木棒/木棒对骷髅类生物额外 +2 伤害，并在物品提示中说明。

## 0v1
### Progress
- Added a new advancement tree.
### World
- The underground world may be bigger and more complex now.
- Ancient cities may now also appear at the bottom of the deep dark in the underground world.
- Cave spiders now also spawn in lush caves.
- Loot chests have been reset/reshuffled.
- Gravel can now only be mined with a flint shovel or better, and replaces sand generation in some biomes.
- Woodland mansions now require any online player to have reached 100,000 XP.
- Ocean monuments now require any player to have entered a Nether fortress.
- The six types of overworld ruined portals now require any player to have entered the Nether.
- Shipwrecks and beached shipwrecks now require any player to have killed a guardian.
- Pillager outposts now share the day-60 requirement with villages, plus the condition that an iron-tier tool has been crafted worldwide.
### Commands
- `/infx` shows all newly added commands.
### Items
- Crafting flint/obsidian now requires stripped logs.
- Metal coins can be used directly with a right-click.
### Server
- Permissions can only be fully opened in test mode.

### Git 工作流

- Git 工作流统一为 Rebase and merge，禁止直接提交 master，PR 合并后强制清理分支与 worktree；本次同时清理了历史遗留的重复分支与失效 worktree。

---

## 0t1-0t4
- Basically finished re-implementing MITE content.
