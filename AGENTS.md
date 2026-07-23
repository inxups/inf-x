# InfiniteX Agent 工作规范

## 适用范围

- 本文件适用于整个仓库；子目录中的 `AGENTS.md` 可对其作用域追加或覆盖规则。
- 开始工作前检查 `git status`，保留用户已有修改，不覆盖、不回滚无关内容。

## Git 工作流

- 凡需要修改仓库的任务，必须使用独立 Git worktree 和 `codex/<任务名>` 分支。
- 如果当前已经处于该任务的独立 worktree，并且上文中没有旧任务，不再嵌套创建 worktree；如果上文中有旧任务且再次发起了新任务，创建新 worktree。
- 只提交与当前任务有关的文件。
- 凡产生仓库改动的任务，都必须同步更新 `CHANGELOG.md`；纯只读任务除外。
- 完成前必须执行适合改动范围的验证，然后创建提交并提交 PR。
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
- 添加属于mite的新方块或者新物品自动从 ‘mite-resource-pack’寻找材质
- 找不到 Minecraft 源码时，先检查 Gradle 下载的 sources JAR 或 NeoForge 附带源码；只有访问级别受限时才使用 Access Transformer。

## 资源与数据生成

- 可由 Data Generator 生成的资源必须修改对应 Provider，再运行数据生成；不要只手工修改生成结果。
- 新增材质只能来自项目所有者提供并确认授权的素材库，不得自行从不明网络来源获取。
- 已确认无需玩家侧署名的素材不必额外展示署名，但仍须维护现有来源清单、目标路径和 SHA-256，以便内部追溯。
- 未经用户明确要求，不得删除现有来源清单或第三方声明。

## 验证要求

根据改动范围执行：

- Java 逻辑：`./gradlew test`
- 最终构建：`./gradlew build`

最终回复必须列出实际执行的验证命令及结果；未执行的验证也必须说明。