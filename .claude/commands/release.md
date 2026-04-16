---
name: Release
description: 发布新版本到 GitHub Release 并更新 Homebrew Tap
category: Release
tags: [release, homebrew, deploy]
---

发布新版本到 GitHub Release 并更新 Homebrew Formula。

**Input**: `/release <version>` — 版本号，如 `1.1.0`（不带 `v` 前缀）。如未提供则询问。

**Steps**

1. **确认版本号**

   如果未提供版本号，使用 AskUserQuestion 询问。
   校验版本号格式（应为 semver，如 `1.1.0`）。
   检查 tag `v<version>` 是否已存在，已存在则报错。

2. **确认发版内容**

   运行 `git log` 查看自上一个 tag 以来的提交，展示给用户确认：
   ```
   即将发布 v<version>，包含以下变更：
   - <commit summary 1>
   - <commit summary 2>
   ...
   ```
   使用 AskUserQuestion 确认是否继续。

3. **创建 tag 并推送**

   ```bash
   git tag v<version>
   git push origin v<version>
   ```

4. **等待 GitHub Actions 完成**

   使用 `gh run list` 找到本次 workflow run，然后 `gh run watch` 等待完成。
   如果失败，展示日志并停止。

5. **获取 SHA256**

   从 Release 中提取 SHA256：
   ```bash
   gh release view v<version> --repo Coulin9/ADBMCPServer
   ```

6. **更新本项目 Formula**

   更新 `Formula/adbmcp-server.rb` 中的 `url` 和 `sha256` 字段。
   提交并推送到主仓库。

7. **更新 Homebrew Tap 仓库**

   克隆 `Coulin9/homebrew-adbmcp-server` 到临时目录。
   复制更新后的 Formula 过去。
   提交并推送。
   清理临时目录。

8. **展示结果**

   ```
   ✓ 发布完成！

   Release: https://github.com/Coulin9/ADBMCPServer/releases/tag/v<version>
   Tap 已更新: https://github.com/Coulin9/homebrew-adbmcp-server

   用户可通过以下命令更新：
     brew update && brew upgrade adbmcp-server
   ```
