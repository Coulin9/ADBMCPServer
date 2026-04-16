## 1. Spec Reading & L0 Validation

- [x] 1.1 Read spec file `specs/foreground-service-query/spec.md`
- [x] 1.2 Verify spec has 3 requirements: Query all, Query by package, Filter output
- [x] 1.3 Verify all scenarios use WHEN/THEN format
- [x] 1.4 Verify proposal capabilities match spec file
- [x] 1.5 Verify no circular dependencies (single capability, no deps)
- [x] 1.6 Confirm all requirements are complete and unambiguous

## 2. Test Task Generation

- [x] 2.1 Map spec scenario "Device has foreground services running" → manual test with device
- [x] 2.2 Map spec scenario "No foreground services running" → manual test with device (no foreground svc)
- [x] 2.3 Map spec scenario "Package has foreground services running" → manual test with packageName arg
- [x] 2.4 Map spec scenario "Package has no foreground services" → manual test with packageName having no FG svc
- [x] 2.5 Map spec scenario "Invalid package name" → manual test with invalid packageName
- [x] 2.6 Map spec scenario "Mixed foreground and background services" → verify filter logic correctness

## 3. Core Implementation

- [x] 3.1 在 `AdbService.kt` 中新增 `getForegroundServices(packageName: String? = null): String` 方法
  - 使用 `execute()` 执行 `dumpsys activity services [packageName]`
  - 实现 `filterForegroundRecords()` 过滤逻辑：按 `* ServiceRecord` 切分块，保留含 `isForeground=true` 的块
  - 无前台服务时返回 `"没有正在运行的前台服务"`
- [x] 3.2 在 `MCPServer.kt` 的 `initTools()` 中注册 `adb_foreground_services` 工具
  - 定义 Tool 对象：name, description, inputSchema (可选 packageName)
  - 注册 handler：提取 packageName 参数，调用 `AdbService.getForegroundServices()`，返回 TextContent

## 4. L1 Static Validation (Gate: MUST PASS)

- [x] 4.1 运行 `./gradlew compileKotlin` 确认编译通过
- [x] 4.2 检查代码风格与现有代码一致（命名、缩进、注释风格）
- [x] 4.3 确认无未使用的 import

## 5. L2 Unit Tests (Gate: MUST PASS)

- [x] 5.1 手动验证 `filterForegroundRecords()` 对示例 dumpsys 输出的过滤结果正确
- [x] 5.2 验证空输入、无前台服务、全部前台服务等边界情况
- [x] 5.3 运行 `./gradlew test` 确认现有测试不被破坏

## 6. L3 Integration Tests (Gate: MUST PASS)

- [ ] 6.1 连接真实设备，运行 `adb_foreground_services` 工具（无参数），验证返回结果
- [ ] 6.2 连接真实设备，运行 `adb_foreground_services` 工具（带 packageName），验证按包名过滤
- [ ] 6.3 验证返回结果中仅包含 `isForeground=true` 的服务记录

## 7. L4 Contract Tests (Gate: MUST PASS)

- [ ] 7.1 验证工具 MCP schema 定义正确（name, inputSchema, description）
- [ ] 7.2 验证可选参数 packageName 不传时行为正确
- [ ] 7.3 验证可选参数 packageName 传入时行为正确

## 8. L5 E2E Tests (Gate: MUST PASS)

- [ ] 8.1 通过 MCP 客户端调用 `adb_foreground_services`，验证完整链路通信正常
- [ ] 8.2 验证 AI Agent 可以正确理解和使用该工具

## 9. L6 Regression Suite (Gate: MUST PASS)

- [x] 9.1 运行 `./gradlew build` 确认构建全量通过
- [ ] 9.2 验证现有 11 个工具功能不受影响
- [ ] 9.3 验证服务器启动正常，新工具出现在工具列表中

## 10. Result Summary & Finalization

- [x] 10.1 将测试结果记录到 `test-verification.md`
- [ ] 10.2 分类所有失败项（bug / flaky / expected / infra）
- [ ] 10.3 如有阻塞性失败，修复后重跑受影响测试
- [ ] 10.4 最终验证：所有 gate（L1-L6）均为 PASS 状态
