# Changelog

## 0v2
### 模组问题修复（作物与家畜）
- 新增洋葱作物：洋葱本身即可作为种子种植，成熟收获 2 个，并有 25% 几率额外获得 1 个。
- 羊死亡时羊毛掉落率从 100% 调整为 MITE 的 50%（羊皮仍为 50%）。
- 原版山羊现在可以用空桶（含金属空桶）挤奶，与牛共用每日 4 单位的奶配额。
- 猪现在会吃棕色蘑菇（繁殖、诱惑与就近觅食均生效）。

### 模组问题修复（方块与挖掘）
- 矮枯草丛、高枯草丛、灌木丛、萤火虫灌木丛、垂泪藤、缠怨藤现在拥有 0.02 硬度。
- 剪刀左键现在只拦截非剪刀有效方块；树叶、羊毛、植物与蜘蛛网等可用剪刀左键破坏。
- 创造模式下右键剪刀可正常精准采集掉落，且不消耗剪刀耐久。
- 创造模式手持剑不再能破坏普通方块，只保留剑有效方块的破坏能力。
- 蜘蛛网可以被空手或剑破坏但不再掉落；只有剪刀破坏时才掉落 1 根线。
- 用剪刀雕刻南瓜现在只掉落 1 个南瓜种子。

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
