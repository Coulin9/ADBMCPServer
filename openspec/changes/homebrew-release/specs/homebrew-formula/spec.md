## ADDED Requirements

### Requirement: Formula 声明 JDK 依赖
Homebrew Formula MUST 声明 `openjdk@21` 为依赖项，安装时自动安装 JDK。

#### Scenario: 安装时自动安装 JDK
- **WHEN** 用户执行 `brew install adbmcp-server`
- **THEN** Homebrew 自动安装 `openjdk@21` 作为依赖（如果尚未安装）

### Requirement: Wrapper 脚本设置 JAVA_HOME
Formula 生成的 wrapper 启动脚本 MUST 正确设置 `JAVA_HOME` 指向 Homebrew 安装的 `openjdk@21`。

#### Scenario: 启动时使用正确的 JDK
- **WHEN** 用户执行 `adbmcp-server`
- **THEN** 进程使用 Homebrew 安装的 openjdk@21 运行，而非系统其他 JDK

### Requirement: 运行时检测 ADB 可用性
Wrapper 脚本 MUST 在启动前检测 `adb` 是否在 PATH 中可用，不可用时给出明确安装提示。

#### Scenario: ADB 未安装时给出提示
- **WHEN** 用户执行 `adbmcp-server` 且 PATH 中没有 `adb`
- **THEN** 输出错误信息提示用户通过 `brew install --cask android-platform-tools` 安装 ADB，并以非零状态码退出

#### Scenario: ADB 已安装时正常启动
- **WHEN** 用户执行 `adbmcp-server` 且 PATH 中有 `adb`
- **THEN** 服务器正常启动

### Requirement: Caveat 提示 ADB 安装
Formula MUST 包含 caveat 区块，在安装完成后提示用户需要手动安装 ADB。

#### Scenario: 安装完成后显示 ADB 提示
- **WHEN** `brew install adbmcp-server` 完成
- **THEN** 终端显示 caveat 信息，告知用户运行 `brew install --cask android-platform-tools` 安装 ADB

### Requirement: 通过自建 Tap 分发
Formula MUST 托管在独立的 GitHub 仓库 `homebrew-adbmcp-server` 中，用户通过 `brew tap` 后安装。

#### Scenario: 用户通过 tap 安装
- **WHEN** 用户执行 `brew tap <owner>/adbmcp-server && brew install adbmcp-server`
- **THEN** 成功安装 adbmcp-server 及其 JDK 依赖
