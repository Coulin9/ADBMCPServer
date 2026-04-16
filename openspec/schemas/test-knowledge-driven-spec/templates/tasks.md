## 1. Spec Reading & L0 Validation

- [ ] 1.1 读取所有 `specs/` 目录下的 spec 文件
- [ ] 1.2 验证每个 spec 包含 `### Requirement:` 和 `#### Scenario:` 块
- [ ] 1.3 验证所有 scenario 使用 WHEN/THEN 格式
- [ ] 1.4 验证 proposal capabilities 与 spec 文件一一对应
- [ ] 1.5 验证无循环依赖
- [ ] 1.6 标记不完整或模糊的需求以便澄清

> **验证来源:** `test-verification.md` → L0: Spec Completeness

## 2. Core Implementation

<!-- 根据 specs 和 design 生成具体的实现任务 -->
<!-- 每个实现任务后应引用 test-verification.md 中对应的验证级别 -->

- [ ] 2.1 <!-- 实现核心功能 A -->
- [ ] 2.2 <!-- 实现核心功能 B -->
- [ ] 2.3 <!-- 实现辅助工具 -->

## 3. L1 静态验证 (Gate: MUST PASS)

> **验证来源:** `test-verification.md` → L1: Static Validation

<!-- 使用 test-verification.md 中定义的 L1 验证命令 -->

- [ ] 3.1 执行代码风格检查: `./gradlew checkstyleMain checkstyleTest`
- [ ] 3.2 执行编译验证: `./gradlew compileJava compileTestJava`
- [ ] 3.3 执行静态分析: `./gradlew spotbugsMain spotbugsTest`
- [ ] 3.4 修复所有 lint/type 错误后重新验证

## 4. L2 单元测试 (Gate: MUST PASS)

> **验证来源:** `test-verification.md` → L2: Unit Tests

<!-- 使用 test-verification.md 中定义的 L2 测试代码和验证命令 -->

- [ ] 4.1 编写单元测试代码 (参考 `test-verification.md` L2 中的测试代码模板)
- [ ] 4.2 执行单元测试: `./gradlew test`
- [ ] 4.3 执行指定测试类: `./gradlew test --tests "<TestClassName>"`
- [ ] 4.4 验证所有新功能有对应的通过的单元测试
- [ ] 4.5 验证边界条件已覆盖
- [ ] 4.6 达到最低覆盖率阈值 (>= 80%)
- [ ] 4.7 生成覆盖率报告: `./gradlew jacocoTestReport`

## 5. L3 集成测试 (Gate: MUST PASS)

> **验证来源:** `test-verification.md` → L3: Integration Tests

<!-- 使用 test-verification.md 中定义的 L3 测试代码和验证命令 -->

- [ ] 5.1 编写集成测试代码 (参考 `test-verification.md` L3 中的测试代码模板)
- [ ] 5.2 执行集成测试: `./gradlew integrationTest`
- [ ] 5.3 验证跨模块交互正常
- [ ] 5.4 验证数据库/API 集成通过
- [ ] 5.5 验证跨边界错误处理

## 6. L4 契约测试 (Gate: MUST PASS)

> **验证来源:** `test-verification.md` → L4: Contract Tests

<!-- 使用 test-verification.md 中定义的 L4 测试代码和验证命令 -->

- [ ] 6.1 编写契约测试代码 (参考 `test-verification.md` L4 中的测试代码模板)
- [ ] 6.2 执行契约测试: `./gradlew contractTest`
- [ ] 6.3 验证 API 请求/响应 schema 匹配 spec
- [ ] 6.4 验证与现有消费者的向后兼容性
- [ ] 6.5 验证数据模型契约满足

## 7. L5 端到端测试 (Gate: MUST PASS)

> **验证来源:** `test-verification.md` → L5: E2E Tests

<!-- 使用 test-verification.md 中定义的 L5 测试代码和验证命令 -->

- [ ] 7.1 编写 E2E 测试代码 (参考 `test-verification.md` L5 中的测试代码模板)
- [ ] 7.2 执行 E2E 测试: `./gradlew e2eTest`
- [ ] 7.3 **[用户确认]** 用户旅程 1: 展示验证证据，询问用户是否符合预期
- [ ] 7.4 **[用户确认]** 用户旅程 2: 展示验证证据，询问用户是否符合预期
- [ ] 7.5 **[用户确认]** 用户旅程 3: 展示验证证据，询问用户是否符合预期
- [ ] 7.6 验证所有用户旅程获得用户明确确认
- [ ] 7.7 验证 UI/UX 流程端到端正常
- [ ] 7.8 验证错误状态正确展示给用户

> **重要:** 每个用户旅程完成后必须等待用户确认后才能继续下一个旅程

## 8. L6 回归测试 (Gate: MUST PASS)

> **验证来源:** `test-verification.md` → L6: Regression Suite

<!-- 使用 test-verification.md 中定义的 L6 验证命令 -->

- [ ] 8.1 执行完整测试套件: `./gradlew clean test integrationTest`
- [ ] 8.2 验证无现有测试被破坏
- [ ] 8.3 验证性能基准在阈值内
- [ ] 8.4 验证无新增安全漏洞: `./gradlew dependencyCheckAnalyze`
- [ ] 8.5 生成综合报告: `./gradlew jacocoTestReport`

## 9. 结果汇总与收尾

> **验证来源:** `test-verification.md` → Failure Analysis & Next Steps

- [ ] 9.1 将所有测试结果记录到 `test-verification.md`
- [ ] 9.2 对失败项进行分类 (bug / flaky / expected / infra)
- [ ] 9.3 如有阻塞性失败: 修复并重新运行受影响的测试级别
- [ ] 9.4 最终验证: 所有 Gate (L1-L6) 在 `test-verification.md` 中显示 PASS
- [ ] 9.5 验证所有跳过的测试已获得用户批准并记录
- [ ] 9.6 验证所有 E2E 用户旅程已获得用户确认

## 10. 架构变更检查 (持续进行)

> **规则来源:** apply instruction - Architecture Change Detection Rule

**在修复测试失败时，如果修改涉及以下内容，必须执行架构变更检查：**
- 数据库选型或数据模型变更
- API 设计变更
- 核心模块拆分或重组
- 新增或移除外部依赖
- 安全方案变更
- 性能优化策略变更

- [ ] 10.1 检查修复方案是否偏离 design.md 中的架构设计
- [ ] 10.2 检查修复方案是否偏离 tasks.md 中的任务结构
- [ ] 10.3 如有偏离: 暂停实现，通知用户偏离详情
- [ ] 10.4 用户选择: 更新文档后重新执行 / 坚持原设计另寻方案
- [ ] 10.5 如需更新: 修改 design.md 和 tasks.md，从 Task 1.1 重新开始
- [ ] 10.6 记录所有架构变更到 `test-verification.md` Architecture Change Tracking 部分
