## Test Verification Summary

| Level | Name | Status | Blocking | Last Run |
|-------|------|--------|----------|----------|
| L0 | Spec Completeness | PENDING | No | - |
| L1 | Static Validation | PENDING | Yes | - |
| L2 | Unit Tests | PENDING | Yes | - |
| L5 | E2E Tests | PENDING | Yes | - |
| L6 | Regression Suite | PENDING | Yes | - |

**Overall Status: NOT READY**

> 注：本变更主要涉及 CI 配置文件和 Homebrew Formula（均为新增文件），不涉及应用逻辑代码变更，因此 L3 (Integration) 和 L4 (Contract) 不适用。

---

## L0: Spec Completeness

**Status:** PENDING
**Blocking:** No

### Verification Path

1. 读取 `specs/release-workflow/spec.md` 和 `specs/homebrew-formula/spec.md`
2. 验证每个 spec 包含 `### Requirement:` 和 `#### Scenario:` 块
3. 验证所有 scenario 使用 WHEN/THEN 格式
4. 验证 proposal 中的 2 个 capabilities 与 spec 文件一一对应

---

## L1: Static Validation

**Status:** PENDING
**Blocking:** YES

### Command

```bash
# 步骤 1: 验证项目仍可编译（确认未破坏构建配置）
./gradlew compileKotlin

# 步骤 2: 验证 GitHub Actions workflow YAML 语法
# 使用 actionlint 或 yamllint（如果可用）
python3 -c "import yaml; yaml.safe_load(open('.github/workflows/release.yml'))"

# 步骤 3: 验证 Homebrew Formula Ruby 语法
ruby -c Formula/adbmcp-server.rb
```

**验证标准:** 编译成功，YAML 和 Ruby 语法无误。

---

## L2: Unit Tests

**Status:** PENDING
**Blocking:** YES

### Command

#### Requirement: 版本号与 tag 一致

**验证方式:** 验证 `build.gradle.kts` 中版本号可从环境变量注入。

```bash
# 验证默认版本号（无环境变量时）
./gradlew shadowJar
ls build/libs/ | grep "SNAPSHOT"
# 预期: ADBMCPServer-1.0-SNAPSHOT-all.jar 存在

# 验证环境变量注入版本号
VERSION=1.0.0 ./gradlew shadowJar
ls build/libs/ | grep "1.0.0"
# 预期: ADBMCPServer-1.0.0-all.jar 存在
```

#### Requirement: Wrapper 脚本设置 JAVA_HOME

**验证方式:** 检查生成的 wrapper 脚本内容。

```bash
# 验证 wrapper 脚本包含 JAVA_HOME 设置
grep "JAVA_HOME" Formula/adbmcp-server.rb
# 预期: 包含 openjdk@21 的路径设置

# 验证 wrapper 脚本包含 adb 检测
grep "command -v adb" Formula/adbmcp-server.rb
# 预期: 包含 adb 可用性检测逻辑
```

#### Requirement: Caveat 提示 ADB 安装

```bash
# 验证 Formula 包含 caveats 方法
grep -A 5 "def caveats" Formula/adbmcp-server.rb
# 预期: 包含 android-platform-tools 安装提示
```

---

## L5: E2E Tests

**Status:** PENDING
**Blocking:** YES

### Step 1: Design Critical User Journeys

| # | User Journey | Priority | Automation Feasible |
|---|--------------|----------|---------------------|
| 1 | 完整发版流程：tag → 构建 → Release | High | 部分（本地模拟构建） |
| 2 | 用户安装流程：tap → install → 运行 | High | 部分（本地 brew 测试） |

### Step 2: Verification Implementation

#### Journey 1: 发版构建流程

**自动化可行: 部分**

```bash
# 模拟 CI 构建流程（本地验证）
# 步骤 1: 设置版本号（模拟 CI 从 tag 提取）
export VERSION=1.0.0

# 步骤 2: 构建 fat JAR
./gradlew clean shadowJar

# 步骤 3: 验证产物
ls -la build/libs/ADBMCPServer-1.0.0-all.jar
# 预期: 文件存在，大小约 17MB

# 步骤 4: 验证 JAR 可运行
timeout 5 java -jar build/libs/ADBMCPServer-1.0.0-all.jar || true
# 预期: 服务器尝试启动（可能因端口/adb 原因退出，但 JVM 能正常加载）

# 步骤 5: 计算 SHA256
shasum -a 256 build/libs/ADBMCPServer-1.0.0-all.jar
# 预期: 输出正确的 SHA256 值
```

**验证标准:** fat JAR 正确生成，文件名含正确版本号，JVM 可加载。

#### Journey 2: Homebrew 安装流程

**自动化可行: 部分（需 Homebrew 环境）**

```bash
# 步骤 1: 验证 Formula 语法
ruby -c Formula/adbmcp-server.rb
# 预期: Syntax OK

# 步骤 2: 使用 brew audit 检查 Formula 规范性（如果 tap 已配置）
# brew audit --new-formula Formula/adbmcp-server.rb

# 步骤 3: 本地测试安装（需要先发布 Release）
# brew tap <owner>/adbmcp-server
# brew install adbmcp-server
# adbmcp-server  # 验证启动
```

**验证标准:** Formula 语法正确，brew audit 无严重警告。

---

## L6: Regression Suite

**Status:** PENDING
**Blocking:** YES

### Command

```bash
# 确保现有功能未受影响
./gradlew clean test

# 验证 fat JAR 仍然可以正常构建
./gradlew shadowJar

# 验证 JAR 入口类正确
java -jar build/libs/ADBMCPServer-*-all.jar --help 2>&1 || true
```

**验证标准:** 现有测试全部通过，fat JAR 构建成功。

---

## Next Steps

验证将在实现阶段（`/opsx:apply`）逐步执行。
