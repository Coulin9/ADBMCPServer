## Test Verification Summary

| Level | Name | Status | Blocking | Last Run |
|-------|------|--------|----------|----------|
| L0 | Spec Completeness | PENDING | No | - |
| L1 | Static Validation | PENDING | Yes | - |
| L2 | Unit Tests | PENDING | Yes | - |
| L3 | Integration Tests | PENDING | Yes | - |
| L4 | Contract Tests | PENDING | Yes | - |
| L5 | E2E Tests | PENDING | Yes | - |
| L6 | Regression Suite | PENDING | Yes | - |

**Overall Status: NOT READY** (All blocking levels must show PASS)

---

## Critical Rules

### E2E User Journey Confirmation Rule
**For L5 E2E tests, each user journey MUST be confirmed by the user:**
1. After each journey verification completes, present evidence (screenshots, logs, outputs)
2. Ask: "用户旅程 '[Journey Name]' 已完成验证。请确认是否符合您的预期？(符合/不符合)"
3. Only mark journey PASS when user explicitly confirms
4. If user says "不符合", document issues and fix before re-verifying

### No Automatic Skip Rule
**Tests MUST NEVER be automatically skipped. If skip is needed:**
1. PAUSE and explain WHY to the user
2. Ask: "测试 '[Test Name]' 需要跳过，原因: [具体原因]。是否允许跳过？(允许/不允许)"
3. Only skip with explicit user permission
4. Record skip reason and user approval below

### Architecture Change Detection Rule
**If test fix requires architecture changes:**
1. Compare fix against design.md and tasks.md
2. If deviation detected, PAUSE and inform user
3. User must choose: update docs + restart, or find alternative fix

---

## Verification Method Selection Guide

**测试手段不限于编写测试代码。Agent 需根据实际场景和测试级别选择合适的验证方案：**

| 验证方式 | 适用场景 | 示例 |
|----------|----------|------|
| **测试代码** | 业务逻辑、数据转换、工具函数等适合用代码断言的场景 | JUnit 单元测试、pytest 函数 |
| **工具驱动** | UI 实现验证、设备交互等需要平台工具辅助的场景 | adb 截图/布局 dump、Appium |
| **自动化脚本** | API 流程、用户旅程等可通过脚本串联的场景 | curl 脚本、Selenium 流程 |
| **手动引导** | 自动化不可行时，引导用户手动操作，Agent 观察验证 | 复杂 UI 交互、需要人机验证 |

**Agent 决策逻辑：**
1. 这是逻辑/数据验证？ → 编写测试代码 + 执行
2. 这是 UI/视觉验证？ → 使用平台工具 (adb, 截图, 布局 dump)
3. 这是用户旅程？ → 设计路径 → 尝试自动化 → 降级为引导式手动操作
4. 始终捕获验证证据（截图、日志、输出）用于记录

---

## L0: Spec Completeness

**Status:** PENDING
**Blocking:** No (informational, fix specs if issues found)

### Verification Path
<!-- 定义 spec 完整性验证的具体步骤 -->

1. 读取所有 `specs/` 目录下的 spec 文件
2. 验证每个 spec 包含 `### Requirement:` 和 `#### Scenario:` 块
3. 验证所有 scenario 使用 WHEN/THEN 格式
4. 验证 proposal 中的 capabilities 与 spec 文件一一对应

### Checks Performed
- [ ] All specs have `### Requirement:` blocks
- [ ] All requirements have `#### Scenario:` blocks
- [ ] All scenarios use WHEN/THEN format
- [ ] Proposal capabilities match spec files
- [ ] No circular dependencies

### Issues Found
<!-- List any spec completeness issues -->

---

## L1: Static Validation

**Status:** PENDING
**Blocking:** YES - Must pass before proceeding to L2

### Command
<!-- 完整的静态验证路径，包含具体的验证命令 -->

```
步骤 1: 执行代码风格检查
./gradlew checkstyleMain checkstyleTest

步骤 2: 执行类型检查和编译验证
./gradlew compileJava compileTestJava

步骤 3: 执行静态分析 (如 SpotBugs/PMD)
./gradlew spotbugsMain spotbugsTest

验证标准: 以上所有命令执行成功，无错误输出
```

### Run Log
<!-- Append each run result -->
<!-- Format: [TIMESTAMP] RESULT - details -->

### Current Issues
<!-- List lint/type errors that need fixing -->

---

## L2: Unit Tests

**Status:** PENDING
**Blocking:** YES - Must pass before proceeding to L3

### Verification Method: Test Code

<!-- 业务逻辑验证 - 编写单元测试代码并执行 -->

### Command
<!-- 完整的单元测试验证路径，包含测试代码和执行命令 -->

#### Requirement: <!-- requirement name from spec -->

**测试代码:**
```java
// 例如: 为 UserService.updateUserName 方法编写单元测试
@Test
void shouldUpdateUserNameSuccessfully() {
    // Given - 准备测试数据
    User user = new User(1L, "oldName");
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

    // When - 执行被测方法
    User result = userService.updateUserName(1L, "newName");

    // Then - 验证结果
    assertEquals("newName", result.getName());
    verify(userRepository).findById(1L);
    verify(userRepository).save(user);
}

@Test
void shouldThrowExceptionWhenUserNotFound() {
    // Given
    when(userRepository.findById(999L)).thenReturn(Optional.empty());

    // When & Then
    assertThrows(UserNotFoundException.class, () -> {
        userService.updateUserName(999L, "newName");
    });
}
```

**执行命令:**
```bash
# 执行所有单元测试
./gradlew test

# 或执行指定测试类
./gradlew test --tests "com.example.service.UserServiceTest"

# 生成测试覆盖率报告
./gradlew jacocoTestReport
```

**验证标准:**
- 所有单元测试通过
- 测试覆盖率 >= 80%
- 无跳过的测试

### Run Log

| Run | Total | Passed | Failed | Skipped | Coverage | Timestamp |
|-----|-------|--------|--------|---------|----------|-----------|
| - | - | - | - | - | - | - |

### Failed Tests
<!-- List specific test failures with error messages -->

---

## L3: Integration Tests

**Status:** PENDING
**Blocking:** YES - Must pass before proceeding to L4

### Verification Method: Test Code + Service Interaction

<!-- 服务间交互验证 - 编写集成测试代码，验证跨模块调用 -->

### Command
<!-- 完整的集成测试验证路径 -->

#### Requirement: <!-- requirement name from spec -->

**测试代码:**
```java
// 例如: 用户名更新的集成测试
@SpringBootTest
@Testcontainers
class UserControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldUpdateUserNameViaAPI() {
        // Given
        UpdateUserNameRequest request = new UpdateUserNameRequest("newName");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getValidToken());

        // When
        ResponseEntity<UserResponse> response = restTemplate.exchange(
            "/api/users/1/name",
            HttpMethod.PUT,
            new HttpEntity<>(request, headers),
            UserResponse.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("newName", response.getBody().getName());
    }
}
```

**执行命令:**
```bash
# 执行集成测试 (需要启动测试数据库)
./gradlew integrationTest

# 或使用 test 容器
./gradlew test --tests "*IntegrationTest"
```

**验证标准:**
- 所有集成测试通过
- 数据库交互正常
- API 端到端调用成功

### Run Log

| Run | Total | Passed | Failed | Skipped | Timestamp |
|-----|-------|--------|--------|---------|-----------|
| - | - | - | - | - | - |

### Failed Tests
<!-- List specific test failures -->

---

## L4: Contract Tests

**Status:** PENDING
**Blocking:** YES - Must pass before proceeding to L5

### Verification Method: Test Code + Schema Validation

<!-- API 契约验证 - 编写契约测试确保接口一致性 -->

### Command
<!-- 完整的契约测试验证路径 -->

#### Requirement: <!-- requirement name from spec -->

**测试代码:**
```java
// 例如: API 契约测试 (使用 Pact 或 Spring Cloud Contract)
@PactTestFor(pactVersion = PactVer3)
class UserApiContractTest {

    @Pact(consumer = "frontend-app")
    public RequestResponsePact updateUserPact(PactDslWithProvider builder) {
        return builder
            .given("user with id 1 exists")
            .uponReceiving("update user name request")
            .path("/api/users/1/name")
            .method("PUT")
            .body("{\"name\": \"newName\"}")
            .willRespondWith()
            .status(200)
            .body("{\"id\": 1, \"name\": \"newName\"}")
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "updateUserPact")
    void testUpdateUserContract(MockServer mockServer) {
        // 验证契约
        HttpResponse response = HttpClient.newHttpClient().send(
            HttpRequest.newBuilder()
                .uri(URI.create(mockServer.getUrl() + "/api/users/1/name"))
                .PUT(HttpRequest.BodyPublishers.ofString("{\"name\": \"newName\"}"))
                .build(),
            HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, response.statusCode());
    }
}
```

**执行命令:**
```bash
# 执行契约测试
./gradlew contractTest

# 发布契约到 Pact Broker (如使用)
./gradlew pactPublish
```

**验证标准:**
- 所有 API 契约测试通过
- 请求/响应 schema 匹配
- 向后兼容性验证通过

### Run Log

| Run | Total | Passed | Failed | Skipped | Timestamp |
|-----|-------|--------|--------|---------|-----------|
| - | - | - | - | - | - |

### Failed Tests
<!-- List contract violations -->

---

## L5: E2E Tests

**Status:** PENDING
**Blocking:** YES - Must pass before proceeding to L6

### Verification Method Selection

<!-- Agent 根据场景选择验证方式:
     1. Web 应用: 优先使用 Selenium/Playwright 自动化
     2. Android/iOS 应用: 优先使用 adb/Appium 工具驱动
     3. 复杂交互/不可自动化场景: 降级为手动引导
-->

### Step 1: Design Critical User Journeys

<!-- 先设计关键用户旅程路径，再选择验证方式 -->

| # | User Journey | Priority | Automation Feasible |
|---|--------------|----------|---------------------|
| 1 | <!-- 用户登录 → 浏览商品 → 加购 → 结算 --> | High | <!-- Yes/No --> |
| 2 | <!-- 用户注册流程 --> | High | <!-- Yes/No --> |
| 3 | <!-- 其他关键路径 --> | Medium | <!-- Yes/No --> |

### Step 2: Verification Implementation

<!-- 根据可行性选择自动化或手动引导 -->

#### Journey 1: <!-- Journey Name -->

**自动化可行: Yes**

<!-- 选择 A: 测试代码 (Web E2E) -->
**测试代码:**
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserManagementE2ETest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void shouldCompleteUserNameUpdateJourney() {
        // Step 1: 用户登录
        LoginResponse loginResponse = webTestClient.post()
            .uri("/api/auth/login")
            .bodyValue(new LoginRequest("testuser", "password"))
            .exchange()
            .expectStatus().isOk()
            .expectBody(LoginResponse.class)
            .returnResult()
            .getResponseBody();

        // Step 2: 更新用户名
        webTestClient.put()
            .uri("/api/users/me/name")
            .header("Authorization", "Bearer " + loginResponse.getToken())
            .bodyValue(new UpdateNameRequest("updatedName"))
            .exchange()
            .expectStatus().isOk();

        // Step 3: 验证更新成功
        webTestClient.get()
            .uri("/api/users/me")
            .header("Authorization", "Bearer " + loginResponse.getToken())
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.name").isEqualTo("updatedName");
    }
}
```

**执行命令:**
```bash
./gradlew bootRun &
./gradlew e2eTest
```

---

#### Journey 2: <!-- Journey Name (Android UI Example) -->

**自动化可行: Yes (Tool-based)**

<!-- 选择 B: 工具驱动验证 (Android UI) -->

**验证脚本:**
```bash
# 1. 部署到设备
./gradlew installDebug

# 2. 启动应用并导航到目标页面
adb shell am start -n com.example/.ProfileActivity
sleep 2

# 3. 截图验证初始状态
adb shell screencap -p /sdcard/before.png && adb pull /sdcard/before.png

# 4. 通过 UI 自动化触发修改用户名操作
adb shell input text "NewUserName"
adb shell input keyevent 66  # Enter
adb shell input tap 540 1200  # 点击保存按钮位置

# 5. 截图验证结果
sleep 1
adb shell screencap -p /sdcard/after.png && adb pull /sdcard/after.png

# 6. 获取布局验证 UI 状态
adb shell uiautomator dump /sdcard/ui.xml && adb pull /sdcard/ui.xml
```

**验证标准:**
- 对比 before/after 截图，用户名文本已更新
- 检查 ui.xml 中目标 TextView 的 text 属性为 "NewUserName"
- adb logcat 无异常日志

**证据采集:**
- `/sdcard/before.png` - 操作前截图
- `/sdcard/after.png` - 操作后截图
- `/sdcard/ui.xml` - 操作后布局 dump

---

#### Journey 3: <!-- Journey Name (Manual Fallback) -->

**自动化可行: No → 手动引导**

<!-- 选择 C: 手动引导验证 (自动化不可行时) -->

**【请用户手动执行以下步骤】**

1. 打开应用，点击"注册"按钮
2. 填写邮箱: test@example.com
3. 填写密码并确认
4. 点击"发送验证码"
5. 检查邮箱获取验证码并输入
6. 点击"完成注册"

**【Agent 验证手段】**
- 监听应用日志: `adb logcat -s RegistrationService:*`
- 检查注册成功后的页面跳转: `adb shell uiautomator dump`
- 验证新用户已存在于后端数据库 (通过 API 调用)

**验证标准:**
- 用户操作完成后，日志无报错
- 页面跳转到主页
- API 返回新用户信息

---

### Verification Summary

| Journey | Method | Status | Evidence | User Confirmed |
|---------|--------|--------|----------|----------------|
| <!-- Journey 1 --> | Test Code | PENDING | - | NO |
| <!-- Journey 2 --> | ADB Tools | PENDING | screenshots/ui.xml | NO |
| <!-- Journey 3 --> | Manual | PENDING | logs/screenshots | NO |

### User Journey Confirmation Log

**Each journey requires explicit user confirmation before marking PASS.**

| Journey | Timestamp | Evidence Presented | User Response | Status |
|---------|-----------|-------------------|---------------|--------|
<!-- | Journey 1: 用户登录流程 | 2024-01-01 10:00 | before.png, after.png, logs | 符合 | PASS | -->
<!-- | Journey 2: 资料修改流程 | 2024-01-01 10:05 | ui.xml, screenshots | 不符合: 保存按钮位置错误 | FAIL | -->

### Run Log

| Run | Total | Passed | Failed | Skipped | Timestamp |
|-----|-------|--------|--------|---------|-----------|
| - | - | - | - | - | - |

### Failed Tests
<!-- List E2E failures with screenshots/logs if available -->

---

## L6: Regression Suite

**Status:** PENDING
**Blocking:** YES - Must pass for implementation to be considered complete

### Command
<!-- 完整的回归测试验证路径 -->

```
步骤 1: 执行完整测试套件
./gradlew clean test integrationTest

步骤 2: 执行性能基准测试
./gradlew jmh

步骤 3: 执行安全扫描
./gradlew dependencyCheckAnalyze

步骤 4: 生成综合报告
./gradlew jacocoTestReport
```

**验证标准:**
- 所有测试通过 (单元 + 集成 + E2E)
- 性能指标在阈值内
- 无新增安全漏洞
- 测试覆盖率达标

### Run Log

| Run | Total | Passed | Failed | Skipped | Duration | Timestamp |
|-----|-------|--------|--------|---------|----------|-----------|
| - | - | - | - | - | - | - |

### Failed Tests
<!-- List any regressions introduced -->

---

## Failure Analysis

<!-- Categorize failures and track resolution -->

| Test | Level | Category | Root Cause | Resolution | Status |
|------|-------|----------|------------|------------|--------|
<!-- | test-name | L2 | bug | null pointer in handler | fixed in abc123 | resolved | -->

**Categories:**
- **bug**: Actual defect in implementation → fix required
- **flaky**: Intermittent failure, not reproducible → investigate or quarantine
- **expected**: Test needs updating for new behavior → update test
- **infra**: Environment/infrastructure issue → fix environment

---

## Skipped Tests Log

**No tests should be skipped without explicit user approval.**

| Test | Level | Skip Reason | User Approved | Approval Time | Resolution Plan |
|------|-------|-------------|---------------|---------------|-----------------|
<!-- | L3 Database Integration | L3 | 测试数据库不可用 | 是 | 2024-01-01 11:00 | 待环境恢复后补测 | -->
<!-- | L5 Payment Flow | L5 | 支付网关测试账号未配置 | 是 | 2024-01-01 11:30 | 配置后重新验证 | -->

**Skip Approval Template:**
```
测试 [Test Name] 需要跳过
原因: [具体原因，如：环境不可用、外部依赖未配置等]
影响: [跳过此测试可能导致的风险]
建议: [何时/如何补测]

是否允许跳过？(允许/不允许)
```

---

## Architecture Change Tracking

**If implementation deviates from design.md/tasks.md, record here:**

| Timestamp | Deviation | Original Design | Actual Implementation | User Decision | Action Taken |
|-----------|-----------|-----------------|----------------------|---------------|--------------|
<!-- | 2024-01-01 12:00 | 数据库选型变更 | PostgreSQL | MongoDB | 更新文档 | 重新执行 Task 1.1 | -->

---

## Next Steps

<!-- Based on test results, what actions are needed -->
<!-- Example: Fix 2 L2 failures, re-run L2-L6 -->
