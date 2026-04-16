## 1. Spec Reading & L0 Validation

- [x] 1.1 读取 spec 文件 `specs/device-info-query/spec.md`
- [x] 1.2 验证 spec 包含 5 个 Requirement 块
- [x] 1.3 验证所有 scenario 使用 WHEN/THEN 格式
- [x] 1.4 验证 proposal capabilities 与 spec 文件对应
- [x] 1.5 验证无循环依赖（单一 capability，无 deps）
- [x] 1.6 确认所有需求完整且无歧义

> **验证来源:** `test-verification.md` → L0: Spec Completeness

## 2. Core Implementation

- [x] 2.1 改造 `AdbService.kt` 中的 `execute()` 方法
  - 新增可选参数 `serial: String? = null`
  - 有 serial 时在命令中插入 `-s <serial>`
  - 确保所有现有调用点无需修改（默认值为 null）
- [x] 2.2 在 `AdbService.kt` 中新增 `getDeviceInfo(serial: String? = null): String` 方法
  - 批量 getprop 获取设备属性（品牌、型号、产品名、Android 版本、芯片平台、硬件名）
  - cat /proc/meminfo 获取总内存
  - wm size + wm density 获取屏幕信息
  - df /data 获取存储信息
  - dumpsys battery 获取电池信息
  - 各部分解析为可读键值对，缺失值显示"未知"
  - 汇总为格式化文本返回
- [x] 2.3 在 `MCPServer.kt` 的 `initTools()` 中注册 `adb_get_device_info` 工具
  - 定义 Tool 对象：name, description, inputSchema（可选 serial）
  - 注册 handler：提取 serial 参数，调用 `AdbService.getDeviceInfo()`，返回 TextContent

## 3. L1 Static Validation (Gate: MUST PASS)

> **验证来源:** `test-verification.md` → L1: Static Validation

- [x] 3.1 执行编译验证: `./gradlew compileKotlin`
- [x] 3.2 检查代码风格与现有代码一致（命名、缩进、注释风格）
- [x] 3.3 确认无未使用的 import

## 4. L2 Unit Tests (Gate: MUST PASS)

> **验证来源:** `test-verification.md` → L2: Unit Tests

- [x] 4.1 验证 getprop 批量输出解析正确（正常值、缺失值）
- [x] 4.2 验证 meminfo 解析正确（MemTotal 提取和 KB→MB 转换）
- [x] 4.3 验证 df 输出解析正确（容量提取和 KB→GB 转换）
- [x] 4.4 验证 battery 解析正确（level、status、temperature）
- [x] 4.5 验证空输出和异常输出的边界处理
- [x] 4.6 执行 `./gradlew test` 确认现有测试不被破坏

## 5. L3 Integration Tests (Gate: MUST PASS)

> **验证来源:** `test-verification.md` → L3: Integration Tests

- [x] 5.1 连接真实设备，验证各 ADB 命令数据源可用性
- [x] 5.2 验证批量 getprop 返回非空有效数据
- [x] 5.3 验证 meminfo / wm / df / dumpsys battery 命令返回有效数据
- [x] 5.4 验证无设备连接时返回合理错误信息

## 6. L4 Contract Tests (Gate: MUST PASS)

> **验证来源:** `test-verification.md` → L4: Contract Tests

- [x] 6.1 验证 MCP Tool schema 定义正确（name, inputSchema, description）
- [x] 6.2 验证可选参数 serial 不传时单设备行为正确
- [x] 6.3 验证可选参数 serial 传入时行为正确

## 7. L5 E2E Tests (Gate: MUST PASS)

> **验证来源:** `test-verification.md` → L5: E2E Tests

- [x] 7.1 通过 MCP 客户端调用 `adb_get_device_info`（无参数），验证完整链路
- [x] 7.2 验证返回文本包含所有分类（设备信息、内存、屏幕、存储、电池）
- [x] 7.3 连接多设备，验证指定 serial 返回对应设备信息
- [x] 7.4 连接多设备不传 serial，验证返回多设备错误提示

## 8. L6 Regression Suite (Gate: MUST PASS)

> **验证来源:** `test-verification.md` → L6: Regression Suite

- [x] 8.1 执行 `./gradlew build` 确认构建全量通过
- [x] 8.2 验证现有 12 个工具功能不受影响
- [x] 8.3 验证服务器启动正常，新工具出现在工具列表中

## 9. Result Summary & Finalization

- [x] 9.1 将测试结果记录到 `test-verification.md`
- [x] 9.2 分类所有失败项（bug / flaky / expected / infra）
- [x] 9.3 如有阻塞性失败，修复后重跑受影响测试
- [x] 9.4 最终验证：所有 gate（L1-L6）均为 PASS 状态
