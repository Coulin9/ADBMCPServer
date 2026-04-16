## Why

AI Agent 在自动化测试和调试过程中，经常需要了解目标设备的硬件和系统配置，以便做出合理决策（如判断设备性能是否满足测试需求、确认 Android 版本兼容性、评估存储空间是否充足等）。当前 ADBMCPServer 缺少获取设备基本信息的能力，Agent 只能通过 `adb_shell` 执行原始命令手动拼凑信息，体验差且容易出错。

## What Changes

- 新增 MCP 工具 `adb_get_device_info`，一次性获取设备的基本信息并以可读文本返回
- 信息覆盖：品牌、型号、产品名、Android 版本、芯片平台、硬件名、总内存、屏幕分辨率/DPI、内部存储容量、电池状态
- 支持多设备场景：通过可选 `serial` 参数指定目标设备
- 改造 `AdbService.execute()` 支持可选 `serial` 参数，为其他工具的多设备扩展打下基础
- **不包含**摄像头信息（格式不稳定，差异大）

## Capabilities

### New Capabilities

- `device-info-query`: 查询设备基本信息，包括设备属性、内存、屏幕、存储、电池，支持多设备

### Modified Capabilities

<!-- 无已有 capability 的需求变更 -->

## Impact

- **AdbService.kt**: `execute()` 方法签名增加可选 `serial` 参数（默认 null，不影响现有调用）
- **MCPServer.kt**: `initTools()` 中注册新工具 `adb_get_device_info`
- **无新依赖**: 复用现有 ADB 命令基础设施，无新增第三方库
