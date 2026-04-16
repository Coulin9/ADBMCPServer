## Why

当前 ADBMCPServer 提供了 11 个工具覆盖设备操作、应用管理、屏幕观察等场景，但缺少查询设备运行状态的能力。在自动化测试和调试场景中，AI Agent 经常需要确认某个服务是否以前台服务（Foreground Service）方式运行——例如验证音乐播放器是否保持后台播放、检查推送服务是否正常启动。新增前台服务查询工具可以填补这一空白。

## What Changes

- 新增 MCP 工具 `adb_foreground_services`，查询设备上正在运行的 Android Foreground Service
- 支持可选参数 `packageName`：传入时只查询指定包的前台服务，不传则查询所有前台服务
- 返回过滤后的完整 dumpsys 输出（仅保留 `isForeground=true` 的服务记录）
- 新增 `AdbService.getForegroundServices()` 方法封装 ADB 命令执行与输出过滤

## Capabilities

### New Capabilities

- `foreground-service-query`: 查询设备上正在运行的 Android Foreground Service，支持按包名过滤，返回过滤后的完整服务记录

### Modified Capabilities

（无）

## Impact

- **新增文件**: 无
- **修改文件**:
  - `src/main/kotlin/adb/AdbService.kt` — 新增 `getForegroundServices()` 方法及输出过滤逻辑
  - `src/main/kotlin/mcp/MCPServer.kt` — 在 `initTools()` 中注册新工具
- **依赖**: 无新增外部依赖，使用现有 `dumpsys activity services` 命令
- **兼容性**: 纯增量改动，不影响现有工具
