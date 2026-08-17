# InfiniteX Agent 工作规范

## 适用范围

- 本文件适用于整个仓库；子目录中的 `CLAUDE.md` 可对其作用域追加或覆盖规则。
- 开始工作前检查 `git status`，保留用户已有修改，不覆盖、不回滚无关内容。

## 命名规则
- 当重写某原版注册时,以InfX为开头(java源码文件或注册)
- 写入mixin方法,方法名无需额外附加(如 r196,infx)

## Git 工作流

- 凡需要修改仓库的任务，必须使用独立 Git worktree 和 `claude/<任务名>` 分支。
- 如果当前已经处于该任务的独立 worktree，并且上文中没有旧任务，不再嵌套创建 worktree；如果上文中有旧任务且再次发起了新任务，创建新 worktree。
- 开工前先 `git fetch origin`，并检查 `git branch -a`、`git ls-remote --heads origin`、`gh pr list --state all --head <分支>`；已有同名或等价任务（已合并、已有分支或 PR）时复用并继续，禁止重复创建分支、提交或 PR。
- 只提交与当前任务有关的文件。
- 一个任务只允许一个分支和一个 PR；从最新 `origin/master` 创建 worktree 与分支：`git worktree add <路径> -b codex/<任务名> origin/master`。
- 任务分支内禁止 `git merge master` 或 `git pull origin master`；需要同步主线时只允许 `git rebase origin/master`，冲突逐条解决。
- 凡产生仓库改动的任务，都必须同步更新 `CHANGELOG_CN.md`；纯只读任务除外。
- 完成前必须执行适合改动范围的验证，然后创建提交并提交 PR。
- PR 统一使用 Rebase and merge 合并并删除分支：`gh pr merge <编号> --rebase --delete-branch`；网页操作时选择 Rebase and merge 并勾选自动删除分支。
- PR 合并后必须立即清理，否则任务不算完成：`git worktree remove <路径>`（目录已不存在则 `git worktree prune`）→ `git branch -D codex/<任务名>` → 远端未删除则 `git push origin --delete codex/<任务名>` → `git fetch --prune origin`。
- 若 `git cherry origin/master <分支>` 已无未合并补丁，或改动与 master 中已有提交内容等价，禁止再建重复分支/PR。
- 无法创建提交或 PR 时，明确说明阻塞原因，不得宣称任务已经完成。

## Minecraft 与 NeoForge 修改原则

- 禁止直接修改 Minecraft、NeoForge、Gradle 缓存、反编译产物或依赖 JAR。
- 实现功能时依次优先采用：
  1. NeoForge/Minecraft 公共 API、事件、注册表和数据驱动机制；
  2. Access Transformer，仅用于放宽必要成员的访问权限；
  3. Mixin，用于没有公共扩展点的原版行为修改；
  4. Coremod，仅在公共 API、Access Transformer 和 Mixin 都无法实现时使用。
- 修改 InfiniteX 自身代码时可正常编辑，不要求使用 Mixin 或 Coremod。
- 修改 Minecraft 或 NeoForge 原有行为时优先使用最小范围的 Mixin，并说明注入原因。
- 添加指令以/infx xxx xxx 格式
- 修改原版世界结构生成规则,参考 ‘docs/structure-generation-gates.md’
- 找不到 Minecraft 源码时，先检查 Gradle 下载的 sources JAR 或 NeoForge 附带源码；只有访问级别受限时才使用 Access Transformer。

## MITE 移植规范

- 移植 MITE（MC 1.6.4）机制前，先对照 vanilla 26.1.2.94 反编译源码（`build/moddev/artifacts/minecraft-patched-26.1.2.94-sources/net/minecraft/`）确认机制是否已原生实现，判定「新机制 / 参数调整 / 已原生」；已原生则调参或直接继承，禁止重复实现（僵尸首领 `handleAttributes`、转化 Normal 50% `killedEntity`、南瓜头 `finalizeSpawn` 均为已原生误移植的先例）。
- 完整移植方法遵循《MITE 移植工程指南》：`./docs/MITE移植指南.md`（vanilla 1.6.4 源码四源对照 + 七层对齐 + 可玩性校验 + 测试策略）。
- 禁止双入口实现同一机制：`Mob.finalizeSpawn` 会连带触发 `FinalizeSpawnEvent`，事件内已做的逻辑勿再显式调用；同一机制只从一个事件/mixin 入口实现。
- 一个机制的多个消费方（伤害/移速/破门/红眼/远程CD 等）必须走统一配置谓词（如 `MonsterEvents.isBloodMoonFrenzied`）；每个机制都有关闭开关，且能真正关停全部子效果。
- 数值从 MITE/1.6.4 照搬前先换算：old-AI 移动速度 ×0.375（new-AI 直接抄）；减速每级 15%（amplifier 5 = 90% 近似硬控）；护甲减伤 1.6.4 线性 vs 现代非线性、难度乘数 1.6.4 整数档 vs 现代 `specialMultiplier`——相对强度以现代公式校准。
- 移植完成须过可玩性校验：相对强度三问（几下打死玩家 / 玩家几下打死它 / 反制资源当时可得），并跑真实新档验证（跨月相、过血月、下矿、进下界）。
- 测试：纯逻辑/数值加边界值单测；机制行为加 GameTest 并做破坏断言验证（先故意改错一条确认会失败）；`runGameTestServer` 失败集必须是既有失败的真子集。

## 资源与数据生成

- 可由 Data Generator 生成的资源必须修改对应 Provider，再运行数据生成；不要只手工修改生成结果。
- 新增材质只能来自项目所有者提供并确认授权的素材库，不得自行从不明网络来源获取。
- 未经用户明确要求，不得删除现有来源清单或第三方声明。

## 验证要求(仅做分析时,无需执行)

根据改动范围执行：

- Java 逻辑：`./gradlew test`
- 游戏测试：`./gradlew runGameTestServer`
- 最终构建：`./gradlew build`

最终回复必须列出实际执行的验证命令及结果；未执行的验证也必须说明。