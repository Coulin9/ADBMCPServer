## Context

ADBMCPServer 是一个基于 Kotlin + Ktor 的 MCP Server，通过 MCP 协议将 ADB 命令封装为 AI Agent 可调用的工具。现有 11 个工具覆盖设备操作、应用管理、屏幕观察等场景，但缺少查询设备运行状态的能力。

在自动化测试和调试中，AI Agent 需要确认某个 Android 组件是否以前台服务（Foreground Service）方式运行。例如：
- 验证音乐播放器是否保持后台播放（需要前台服务通知）
- 检查推送服务是否以前台服务方式启动
- 调试前台服务被系统杀死的问题

Android 提供 `dumpsys activity services` 命令可查询所有服务状态，其中包含 `isForeground=true` 标记标识前台服务。

## Goals / Non-Goals

**Goals:**
- 提供一个 MCP 工具 `adb_foreground_services`，查询设备上的 Foreground Service
- 支持可选 `packageName` 参数按包名过滤
- 返回过滤后的完整服务记录（仅 `isForeground=true` 的记录）
- 复用现有 `AdbService.execute()` 基础设施，无新增外部依赖

**Non-Goals:**
- 不解析 dumpsys 输出为结构化数据（JSON 等），保持原始文本输出
- 不提供服务启动/停止等写操作，仅查询
- 不过滤服务记录中的子字段（返回该服务的完整 dumpsys 记录块）

## Decisions

### Decision 1: 过滤策略 — 多行记录块级过滤

**选择**: 按 `* ServiceRecord` 边界切分 dumpsys 输出，逐块检查 `isForeground=true`，拼接匹配的完整块。

**原因**: dumpsys 输出中每个服务记录跨越多行，`* ServiceRecord{...}` 是记录起始标记，后续缩进行属于该记录。按此边界切分可以保留完整的服务上下文信息（intent、app、createTime、foregroundNoti 等）。

**备选方案**:
- **逐行 grep `isForeground=true`**: 丢失服务名、intent 等上下文信息，不满足"完整信息"需求
- **返回全部 dumpsys 输出不过滤**: 噪声太多，AI 需要自行解析大量无关服务

### Decision 2: ADB 命令选择

**选择**: `adb shell dumpsys activity services [packageName]`

**原因**: 这是 Android 官方的服务状态查询命令，包含 `isForeground=true` 标记。有包名时追加包名参数可减少输出量。

**备选方案**:
- `dumpsys activity services | grep isForeground`: 需要在 shell 层过滤，但多行记录无法简单 grep
- `dumpsys notification --noredact`: 从通知角度查询前台服务，但不直接关联到 ServiceRecord

### Decision 3: 无结果时的输出

**选择**: 当过滤后无前台服务时，返回提示文本 `"没有正在运行的前台服务"` 而非空字符串。

**原因**: 空字符串可能被 AI Agent 误判为命令执行失败。明确的提示信息消除歧义。

## Risks / Trade-offs

**[Risk] dumpsys 输出格式差异** → 不同 Android 版本的 dumpsys 输出格式可能略有差异（缩进、字段排列），但 `* ServiceRecord` 和 `isForeground=true` 这两个关键标记在所有版本中稳定存在。过滤逻辑依赖这两个标记，兼容性良好。

**[Risk] dumpsys 输出量大** → 不传包名时可能返回大量服务记录。但经过 `isForeground=true` 过滤后，前台服务通常数量很少（常见 1-5 个）。传输和处理开销可控。

**[Risk] 超时风险** → `dumpsys activity services` 在设备负载高时可能较慢。使用 `AdbService` 默认 10 秒超时，与现有工具一致。如需调整可后续增加。
