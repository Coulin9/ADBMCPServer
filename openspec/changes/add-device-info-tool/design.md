## Context

ADBMCPServer 是一个基于 Kotlin + Ktor 的 MCP Server，通过 MCP 协议将 ADB 命令封装为 AI Agent 可调用的工具。现有 12 个工具覆盖设备操作、应用管理、屏幕观察等场景，但缺少一次性获取设备硬件和系统概览的能力。

AI Agent 在自动化测试和调试中需要了解目标设备配置：品牌型号决定兼容性行为差异，内存和存储影响测试策略，Android 版本决定 API 可用性，电池状态影响测试时长决策。当前 Agent 只能通过 `adb_shell` 执行零散命令手动拼凑，体验差。

## Goals / Non-Goals

**Goals:**
- 提供一个 MCP 工具 `adb_get_device_info`，一次调用获取设备基本信息
- 信息覆盖：设备属性、内存、屏幕、存储、电池
- 支持多设备场景：通过可选 `serial` 参数指定目标设备
- 返回可读格式化文本，便于 AI Agent 直接理解
- 改造 `AdbService.execute()` 支持可选 `serial` 参数，不破坏现有调用

**Non-Goals:**
- 不包含摄像头信息（dumpsys media.camera 格式不稳定，厂商差异大）
- 不提供结构化 JSON 返回（当前所有工具均返回 TextContent，保持一致）
- 不自动检测并提示用户选择设备（多设备时由调用方传入 serial）
- 不提供设备信息的实时监控或变更通知

## Decisions

### Decision 1: ADB 命令策略 — 批量 getprop + 独立命令

**选择**: 使用 5 次 ADB shell 调用，其中属性类信息合并为一次批量 getprop。

| 调用 | 命令 | 获取信息 |
|------|------|----------|
| ① | `getprop k1; getprop k2; ...` (1次 shell) | 品牌、型号、产品名、Android 版本、芯片平台、硬件名 |
| ② | `cat /proc/meminfo` | 总内存 |
| ③ | `wm size; wm density` | 屏幕分辨率、DPI |
| ④ | `df /data` | 内部存储容量 |
| ⑤ | `dumpsys battery` | 电池电量、状态、温度 |

**原因**: 批量 getprop 将 6 个属性查询合并为 1 次 shell 调用，减少 ADB 连接开销。各维度信息通过独立命令获取，解析逻辑互不干扰，单个命令失败不影响其他信息的获取。

**备选方案**:
- **cat /system/build.prop 一次性获取**: 速度快，但摄像头/电池/存储不在其中，且部分设备 /system 只读挂载，权限受限
- **每个属性单独 getprop**: 最灵活但 6 次 shell 调用，开销大
- **全部合并为 1 次 shell**: 速度快但解析复杂，某个命令失败会导致全部失败

### Decision 2: 多设备支持 — 为 execute() 加 serial 参数

**选择**: `AdbService.execute()` 增加可选参数 `serial: String? = null`，有值时在 adb 后插入 `-s <serial>`。

```
无 serial:  adb shell getprop ...
有 serial:  adb -s ABC123 shell getprop ...
```

**原因**: 这是最小侵入的改造方式。默认值为 null，所有现有调用点无需修改。未来其他工具需要多设备支持时，也只需在 handler 层传入 serial。

**备选方案**:
- **新建 executeForDevice() 方法**: 方法更多，但逻辑重复
- **在 MCPServer handler 层拼接 -s 参数**: 不改 AdbService，但破坏了 AdbService 作为 ADB 命令统一入口的职责边界

### Decision 3: 多设备无 serial 时的行为

**选择**: 不传 serial 时直接执行 ADB 命令，由 ADB 本身处理多设备错误。

- 单设备: 正常执行
- 多设备: ADB 返回 `error: more than one device/emulator`，通过错误检查传递给 AI Agent

**原因**: AI Agent 看到此错误后会自然地先调用 `adb_list_devices` 获取序列号，再带 serial 重试。无需在工具层做额外的设备列表检测，减少不必要的 ADB 调用。

### Decision 4: 存储路径 — /data

**选择**: 使用 `df /data` 获取内部存储信息。

**原因**: `/data` 是 Android 内部存储挂载点，反映设备真实存储容量。`/sdcard` 在现代 Android 上通常是指向 `/data/media` 的符号链接，且部分设备无外部 SD 卡。

### Decision 5: 返回格式 — 可读文本

**选择**: 返回带分类标题和键值对的格式化文本。

**原因**: 与现有工具一致使用 TextContent，AI Agent 可直接阅读理解。设备信息天然是结构化的，但用文本展示对 LLM 更友好。需要结构化数据的场景可通过 `adb_shell` 自行获取。

## Risks / Trade-offs

**[Risk] getprop 属性在不同厂商设备上可能缺失** → 解析时对每个属性使用安全取值，缺失则显示"未知"。不影响其他信息的获取。

**[Risk] dumpsys battery 在模拟器上可能返回异常值** → 解析时做基本的数值校验，异常值显示原始文本。

**[Risk] df 输出格式在不同 Android 版本间有差异** → 使用 `/data` 行最后一列的通用解析方式，取 Size/Used/Available 列。

**[Trade-off] 5 次 ADB 调用耗时约 1-3 秒** → 可接受。将所有命令合并为 1 次 shell 调用可进一步优化，但牺牲了错误隔离性，当前不做此优化。

**[Trade-off] 不包含摄像头信息** → 摄像头信息获取复杂（dumpsys media.camera 输出格式不稳定，需要 root 权限的设备较多）。用户可通过 `adb_shell` 按需查询。
