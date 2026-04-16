## Context

ADBMCPServer 是一个 Kotlin/Gradle 项目，通过 Shadow 插件生成 fat JAR（约 17MB），JDK 已降至 21。目前无 CI/CD 和分发机制。需要建立从源码到用户安装的完整发布链路：GitHub Actions 自动构建 → GitHub Release 托管产物 → Homebrew Tap 分发。

## Goals / Non-Goals

**Goals:**
- 用户可通过 `brew tap` + `brew install` 一键安装 ADBMCPServer
- JDK 21 作为 Formula 依赖自动安装
- ADB 缺失时给出明确指引
- 打 tag 时自动构建并发布到 GitHub Release

**Non-Goals:**
- 不提交到 homebrew-core（项目初期，走自建 Tap）
- 不做 GraalVM Native Image（当前 fat JAR 方案足够）
- 不做 Linux/Windows 包管理器分发
- 不做自动更新 Formula 的 CI（初期手动更新版本即可）

## Decisions

### Decision 1: 使用自建 Tap 而非 homebrew-core

**选择**: 创建独立仓库 `homebrew-adbmcp-server`

**理由**: homebrew-core 审核严格，要求项目有足够知名度和用户量。自建 Tap 完全自主控制发版节奏，适合项目初期。

**替代方案**: 提交到 homebrew-core — 审核通过难度大，且 JDK 25 依赖（已降至 21 缓解了这个问题，但项目知名度仍不足）。

### Decision 2: 分发 fat JAR 而非 distZip

**选择**: 直接下载并安装 Shadow fat JAR

**理由**: 单文件分发最简单，Homebrew Formula 中通过 wrapper 脚本启动即可。distZip 包含额外的启动脚本和目录结构，对 Homebrew 来说是不必要的复杂度。

**替代方案**: `./gradlew distZip` — 多了解压和目录管理步骤，wrapper 脚本与 Homebrew 自己生成的脚本功能重复。

### Decision 3: Gradle 构建时从 tag 提取版本号

**选择**: `build.gradle.kts` 中读取环境变量 `VERSION`，CI 从 Git tag 解析后注入。本地构建保留 `1.0-SNAPSHOT` 默认值。

**理由**: 避免每次发版手动改 `build.gradle.kts` 中的版本号。tag 是版本的单一来源。

### Decision 4: ADB 依赖通过 caveat + 运行时检测处理

**选择**: Formula 的 `caveats` 方法提示用户安装 ADB，wrapper 脚本在启动时检测 `adb` 是否可用。

**理由**: `android-platform-tools` 在 Homebrew 中是 Cask，Formula 无法声明对 Cask 的依赖。双重提示（安装时 caveat + 运行时检查）确保用户不会困惑。

## Risks / Trade-offs

| 风险 | 缓解措施 |
|------|----------|
| 用户机器上 Homebrew 没有 openjdk@21 | Formula 的 `depends_on` 会自动处理安装 |
| 用户忽略 caveat 未安装 ADB | wrapper 脚本运行时检测并给出安装命令 |
| fat JAR 17MB 偏大 | 对于服务端工具可接受；未来可考虑 Native Image |
| 手动更新 Formula 中的 SHA256 容易出错 | 初期可接受，后续可添加 CI 自动化更新 Tap |
| GitHub 仓库名必须以 `homebrew-` 前缀 | 这是 Homebrew Tap 的命名约定，非风险 |
