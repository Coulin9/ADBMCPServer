## 模块范围 (Module Scope)

<!-- 根据 proposal，列出本次变更涉及或相关的模块/组件 -->
<!-- List all modules/components affected by or relevant to this change -->

| 模块 | 关联原因 | 优先级 |
|------|----------|--------|
| <!-- module name --> | <!-- why it's relevant --> | <!-- high/medium/low --> |

## 数据源调查计划 (Data Source Investigation Plan)

### Source 1: 项目知识库 (knowledge/ 文件夹)

<!-- 先阅读 knowledge/AGENTS.md 了解知识库结构，再规划需要查询的内容 -->
<!-- Read knowledge/AGENTS.md first, then plan what to look up -->

**知识库结构概要:**
<!-- Summary of knowledge base structure after reading AGENTS.md -->

**计划查询的内容:**
- [ ] <!-- file path or topic --> — <!-- rationale for querying this -->

### Source 2: 远端 RAG 服务器

<!-- 列出需要向 RAG 服务发送的查询 -->
<!-- List queries to send to the RAG service -->

**计划查询:**
- [ ] <!-- search query or document ID --> — <!-- what you expect to find -->

**状态:** <!-- 可用 / 不可用（已询问用户，跳过原因：...） -->

### Source 3: 飞书文档 (Feishu Docs via MCP)

<!-- 必须通过 Feishu MCP 工具实际搜索并定位文档，列出有效的文档链接 -->
<!-- MUST use Feishu MCP tools to search and locate real documents; list valid URLs, NOT keywords -->

**已确认的相关文档:**
- [ ] [文档标题](feishu-url) — <!-- why this document is relevant -->

**状态:** <!-- 可用 / 不可用（已询问用户，跳过原因：...） -->

### Source 4: 本地代码事实

<!-- 列出需要检查的代码文件、模块、接口、模式 -->
<!-- List code files, modules, interfaces, and patterns to examine -->

**计划查询:**
- [ ] <!-- file path, class name, grep pattern --> — <!-- what you expect to find -->

## 跳过的数据源 (Skipped Data Sources)

<!-- 记录不可用的数据源及跳过原因 -->
<!-- Document any unavailable data sources and why they were skipped -->

| 数据源 | 跳过原因 | 用户确认 |
|--------|----------|----------|
| <!-- source name --> | <!-- reason --> | <!-- yes/no --> |

## 用户确认 (User Confirmation)

<!-- 以下问题需要用户确认后才能进入 design-research-report 阶段 -->

- [ ] 模块范围是否正确？是否有遗漏或多余的模块？
- [ ] 计划查询的资料是否完整？
- [ ] 是否有需要额外补充的资料来源或具体文档？
