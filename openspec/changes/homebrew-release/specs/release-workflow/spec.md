## ADDED Requirements

### Requirement: Tag 触发自动构建和发布
当推送符合 `v*` 模式的 Git tag 时，GitHub Actions 工作流 SHALL 自动触发构建流程，编译 fat JAR 并创建 GitHub Release。

#### Scenario: 推送版本 tag 触发 Release
- **WHEN** 开发者推送格式为 `v1.0.0` 的 tag
- **THEN** GitHub Actions 自动执行 `shadowJar` 构建任务，生成 fat JAR 文件，并创建对应的 GitHub Release，上传 JAR 作为 release asset

#### Scenario: 非版本 tag 不触发
- **WHEN** 推送的 tag 不符合 `v*` 模式（如 `test-123`）
- **THEN** 发布工作流不会被触发

### Requirement: Release 包含 SHA256 校验和
每次 Release MUST 包含 fat JAR 文件的 SHA256 校验和，供 Homebrew Formula 引用。

#### Scenario: Release 中提供 SHA256
- **WHEN** GitHub Release 创建完成
- **THEN** Release notes 中包含 fat JAR 的 SHA256 校验和值

### Requirement: 版本号与 tag 一致
构建产物的版本号 MUST 与 Git tag 保持一致（去掉 `v` 前缀）。

#### Scenario: tag v1.0.0 生成正确版本的 JAR
- **WHEN** 推送 tag `v1.0.0`
- **THEN** 生成的 JAR 文件名为 `ADBMCPServer-1.0.0-all.jar`
