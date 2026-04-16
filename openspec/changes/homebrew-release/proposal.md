## Why

ADBMCPServer 目前唯一的分发方式是手动下载 fat JAR 运行，用户需要自行配置 JDK 和 ADB 环境。通过 Homebrew 发布可以让 macOS 用户一条命令安装并自动配齐 JDK 依赖，大幅降低使用门槛。

## What Changes

- 将项目版本从 `1.0-SNAPSHOT` 改为正式版本号 `1.0.0`
- 创建 GitHub Actions 工作流，在打 tag 时自动构建 fat JAR 并发布 GitHub Release
- 创建 Homebrew Tap 仓库（`homebrew-adbmcp-server`），包含 Formula 定义
- Formula 声明 `openjdk@21` 为依赖（自动安装），通过 caveat + 运行时检测处理 ADB 依赖
- 生成 wrapper 启动脚本，设置 JAVA_HOME 并检测 adb 可用性

## Capabilities

### New Capabilities
- `release-workflow`: GitHub Actions 自动化发版流程——tag 触发构建、上传 fat JAR 到 GitHub Release、计算 SHA256
- `homebrew-formula`: Homebrew Formula 定义及 Tap 仓库结构——包含 JDK 依赖声明、wrapper 脚本生成、ADB 检测、caveat 提示

### Modified Capabilities

（无现有 capability 需要修改）

## Impact

- **构建配置**: `build.gradle.kts` 版本号变更
- **新增文件**: `.github/workflows/release.yml`、Homebrew Formula 文件
- **外部依赖**: 需要创建新的 GitHub 仓库 `homebrew-adbmcp-server` 作为 Tap
- **用户体验**: 用户可通过 `brew tap && brew install` 安装，自动获得 JDK 21
