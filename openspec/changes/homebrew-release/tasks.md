## 1. Spec Reading & L0 Validation

- [x] 1.1 读取 `specs/release-workflow/spec.md` 和 `specs/homebrew-formula/spec.md`
- [x] 1.2 验证每个 spec 包含 `### Requirement:` 和 `#### Scenario:` 块
- [x] 1.3 验证所有 scenario 使用 WHEN/THEN 格式
- [x] 1.4 验证 proposal 中的 `release-workflow` 和 `homebrew-formula` 与 spec 文件一一对应
- [x] 1.5 标记不完整或模糊的需求以便澄清

> **验证来源:** `test-verification.md` → L0: Spec Completeness

## 2. Core Implementation

### 2.1 版本号动态化

- [x] 2.1.1 修改 `build.gradle.kts`，从环境变量 `VERSION` 读取版本号，默认 `1.0-SNAPSHOT`
- [x] 2.1.2 验证 `./gradlew shadowJar` 默认生成 `ADBMCPServer-1.0-SNAPSHOT-all.jar`
- [x] 2.1.3 验证 `VERSION=1.0.0 ./gradlew shadowJar` 生成 `ADBMCPServer-1.0.0-all.jar`

> **验证来源:** `test-verification.md` → L2 → Requirement: 版本号与 tag 一致

### 2.2 GitHub Actions Release 工作流

- [x] 2.2.1 创建 `.github/workflows/release.yml`
- [x] 2.2.2 配置 tag `v*` 模式触发
- [x] 2.2.3 从 tag 名提取版本号（去掉 `v` 前缀），注入 `VERSION` 环境变量
- [x] 2.2.4 执行 `./gradlew shadowJar` 构建 fat JAR
- [x] 2.2.5 计算 fat JAR 的 SHA256 校验和
- [x] 2.2.6 使用 `softprops/action-gh-release` 创建 GitHub Release，上传 JAR 和 SHA256

> **验证来源:** `test-verification.md` → L5 → Journey 1: 发版构建流程

### 2.3 Homebrew Formula

- [x] 2.3.1 创建 `Formula/adbmcp-server.rb`（在本项目内先编写，后续迁移到 tap 仓库）
- [x] 2.3.2 声明 `depends_on "openjdk@21"`
- [x] 2.3.3 实现 `install` 方法：安装 fat JAR 到 `libexec`，生成 wrapper 脚本到 `bin`
- [x] 2.3.4 wrapper 脚本中设置 `JAVA_HOME` 指向 `openjdk@21`
- [x] 2.3.5 wrapper 脚本中添加 `adb` 可用性检测，不可用时输出安装提示并退出
- [x] 2.3.6 实现 `caveats` 方法，提示用户安装 `android-platform-tools`
- [x] 2.3.7 实现 `test` 方法（冒烟测试）

> **验证来源:** `test-verification.md` → L2 → Requirement: Wrapper 脚本设置 JAVA_HOME / Caveat 提示 ADB 安装

## 3. L1 静态验证 (Gate: MUST PASS)

> **验证来源:** `test-verification.md` → L1: Static Validation

- [x] 3.1 执行编译验证: `./gradlew compileKotlin`
- [x] 3.2 验证 GitHub Actions YAML 语法: `python3 -c "import yaml; yaml.safe_load(open('.github/workflows/release.yml'))"`
- [x] 3.3 验证 Homebrew Formula Ruby 语法: `ruby -c Formula/adbmcp-server.rb`
- [x] 3.4 修复所有语法错误后重新验证

## 4. L2 单元测试 (Gate: MUST PASS)

> **验证来源:** `test-verification.md` → L2: Unit Tests

- [x] 4.1 验证版本号环境变量注入: `VERSION=1.0.0 ./gradlew shadowJar && ls build/libs/ | grep "1.0.0"`
- [x] 4.2 验证默认版本号: `./gradlew shadowJar && ls build/libs/ | grep "SNAPSHOT"`
- [x] 4.3 验证 Formula 包含 JAVA_HOME 设置: `grep "JAVA_HOME" Formula/adbmcp-server.rb`
- [x] 4.4 验证 Formula 包含 adb 检测: `grep "command -v adb" Formula/adbmcp-server.rb`
- [x] 4.5 验证 Formula 包含 caveats: `grep -A 5 "def caveats" Formula/adbmcp-server.rb`

## 5. L5 端到端测试 (Gate: MUST PASS)

> **验证来源:** `test-verification.md` → L5: E2E Tests

- [x] 5.1 模拟发版构建：`VERSION=1.0.0 ./gradlew clean shadowJar`，验证产物正确
- [x] 5.2 验证 fat JAR 可启动：`timeout 5 java -jar build/libs/ADBMCPServer-1.0.0-all.jar || true`
- [x] 5.3 计算并验证 SHA256：`shasum -a 256 build/libs/ADBMCPServer-1.0.0-all.jar`
- [x] 5.4 验证 Formula Ruby 语法：`ruby -c Formula/adbmcp-server.rb`
- [x] 5.5 **[用户确认]** 展示构建产物和 Formula 内容，确认符合预期

## 6. L6 回归测试 (Gate: MUST PASS)

> **验证来源:** `test-verification.md` → L6: Regression Suite

- [x] 6.1 执行完整测试套件: `./gradlew clean test`
- [x] 6.2 验证 fat JAR 构建成功: `./gradlew shadowJar`
- [x] 6.3 验证无现有测试被破坏

## 7. 结果汇总与收尾

> **验证来源:** `test-verification.md` → Failure Analysis & Next Steps

- [x] 7.1 将所有测试结果记录到 `test-verification.md`
- [x] 7.2 对失败项进行分类 (bug / flaky / expected / infra)
- [x] 7.3 如有阻塞性失败: 修复并重新运行受影响的测试级别
- [x] 7.4 最终验证: 所有 Gate (L1, L2, L5, L6) 显示 PASS
