# InfiniteX

InfiniteX 是一个不太简单的Mod。

> 你不再只是砍树、挖矿、升级装备。

你要学会活下来。


## 开发
开发时可通过 JVM 启动参数 `-Dinfx.testMode=true` 启用 test 模式。测试模式保留原版 OP、
`ops.json`、本地控制台、RCON 与 JSON-RPC；客户端只能连接到使用相同测试模式的服务端。

普通模式下所有玩家均不具有 OP 权限，新建专用服不会自动生成空 `ops.json`。本地控制台仅允许
`ban`、`ban-ip`、`pardon`、`pardon-ip`、`kick`、`whitelist`、`stop`、`save-off`、`save-on`、
`help`（`?`）、`list`、`seed`、`say`、`me`、`msg`/`tell`/`w`、`scoreboard`、`team`、`tag`、
`bossbar`、`recipe`、`datapack`、`reload`、`schedule`、`particle`、`playsound`、`title`、
`tellraw`、`teammsg`（`tm`）、`debug`、`jfr`、`perf`、`random` 和 `save-all`；其他命令被拒绝。
RCON 与 JSON-RPC 仍被禁用。

## 其他
部分材质来自YF101,qf
部分声音文件取自MITE
