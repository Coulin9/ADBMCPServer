# AGENTS.md — ADB MCP Server

## Project Overview

ADB MCP Server is a Kotlin/JVM Model Context Protocol (MCP) server that wraps
Android Debug Bridge (ADB) commands as tools invokable by AI agents over HTTP.
It is **not** an Android app — it is a server-side JVM application that controls
Android devices via the `adb` CLI.

## Architecture

- **Language**: Kotlin (JVM), JDK 24 toolchain
- **Framework**: Ktor (HTTP server, CIO engine)
- **Protocol**: MCP via `io.modelcontextprotocol:kotlin-sdk-server`
- **Core pattern**: `AdbService` (singleton object) wraps all `adb` CLI calls;
  `MCPServer` registers tools and handles MCP protocol transport.

### Key Files

| File | Role |
|------|------|
| `src/main/kotlin/Main.kt` | Entry point (`com.coulin` package) |
| `src/main/kotlin/mcp/MCPServer.kt` | MCP server setup, Ktor config, tool registration |
| `src/main/kotlin/adb/AdbService.kt` | All ADB command execution and output parsing |
| `build.gradle.kts` | Build config and dependencies |

## Build / Run / Test Commands

**Prerequisites**: JDK 24, `adb` on PATH.

```bash
# Build
./gradlew build

# Run (listens on 127.0.0.1:3000)
./gradlew run

# Run all tests
./gradlew test

# Run a single test class
./gradlew test --tests "com.coulin.adb.AdbServiceParseTest"

# Run a single test method
./gradlew test --tests "com.coulin.adb.AdbServiceParseTest.parseMemTotalNormal"
```

**No lint/checkstyle/detekt/ktlint configured.** The only style enforcement is
`kotlin.code.style=official` in `gradle.properties` and IntelliJ settings.

## Testing

- **Framework**: kotlin.test on JUnit Platform (JUnit 5)
- **No mocking library** — tests use inline string literals as test data
- **Test types**:
  - `AdbServiceParseTest.kt` — Unit tests for parsing functions (no device needed)
  - `AdbServiceDeviceInfoTest.kt` — Integration tests requiring a real ADB device
- **Test methods**: Self-contained, no shared fixtures or setup/teardown

## Code Style Guidelines

### Naming

| Element | Convention | Example |
|---------|-----------|---------|
| Packages | lowercase dot-separated | `com.coulin.adb` |
| Classes | PascalCase, acronyms uppercase | `AdbService`, `MCPServer` |
| Functions | camelCase | `getDeviceInfo`, `parseMemTotal` |
| Constants | SCREAMING_SNAKE_CASE | `ADB_CMD`, `DEFAULT_TIMEOUT_SEC` |
| Variables | camelCase | `mcpVersion`, `reinstall` |
| Test methods | camelCase descriptive (no underscores) | `parseMemTotalNormal` |

### Formatting

- Kotlin Official code style (4-space indentation)
- Opening braces on same line
- Use `StringBuilder` with `appendLine()` for multi-line output
- String templates: `"已点击坐标 ($x, $y)"`

### Imports

- Wildcard imports are used for MCP SDK types (`io.modelcontextprotocol.kotlin.sdk.types.*`)
- Standard library uses explicit single-class imports
- No enforced import ordering tool

### Visibility & Structure

- Heavy use of Kotlin `object` singletons (no DI framework)
- `internal` visibility for functions needing test access but not public API
- Double-checked locking for lazy server initialization

### Error Handling

- **MCP layer** (`MCPServer`): try/catch returning `CallToolResult(isError = true, ...)`
- **ADB layer** (`AdbService`): throws `RuntimeException` for timeouts
- Missing tool arguments: `?: throw IllegalArgumentException(...)`
- Parse failures: return fallback string `"未知"` (unknown) rather than throwing
- Some error conditions are detected but soft-handled (commented-out throws)

### Localization

- **User-facing strings** (tool descriptions, error messages, output labels): Chinese (Simplified)
- **Code identifiers and comments**: English

## Dependencies

| Dependency | Version | Purpose |
|------------|---------|---------|
| `kotlin("jvm")` | 2.3.0 | Kotlin compiler |
| `io.modelcontextprotocol:kotlin-sdk-server` | 0.9.0 | MCP server SDK |
| `io.ktor:ktor-server-core` | 3.4.1 | Ktor HTTP core |
| `io.ktor:ktor-server-cio` | 3.4.1 | Ktor CIO engine (runtime) |
| `io.ktor:ktor-server-content-negotiation` | 3.4.1 | Content negotiation |
| `io.ktor:ktor-serialization-kotlinx-json` | 3.4.1 | JSON serialization |
| `kotlin("test")` | — | Test library (JUnit Platform) |

## Development Conventions

1. **Adding ADB features**: Add the command wrapper in `AdbService` first,
   then register the MCP tool in `MCPServer.initTools()`
2. **Tool registration**: All tools defined in `MCPServer.kt` inside `initTools`
3. **Process execution**: Use `AdbService.execute()` which handles process
   creation, timeout (`DEFAULT_TIMEOUT_SEC = 30`), and output capture
4. **New parse functions**: Mark `internal` so tests can access them;
   return `"未知"` on parse failure instead of throwing
5. **Test new parsing**: Add unit tests in `AdbServiceParseTest.kt` using
   inline string literals — no mocking needed
6. **Integration tests**: Tests that need a real device go in separate test
   classes; they will fail in CI without a connected device

## OpenSpec Workflow

This project uses a custom OpenSpec schema at `openspec/schemas/test-driven-spec/`
with a test-driven workflow that includes an optional design research phase.

**Full flow (with design research):**
proposal → design-research-plan → design-research-report → specs → design → test-verification → tasks

**Skippable flow (without design research):**
proposal → specs → design → test-verification → tasks

After proposal creation, the user is asked whether to execute or skip the design
research phase. Design research investigates 4 data sources before design:
1. Project knowledge base (`knowledge/` folder)
2. Remote RAG server
3. Feishu docs (via MCP)
4. Local code facts

Key apply rules:
- E2E user journeys require explicit user confirmation
- Tests must never be auto-skipped without user approval
- Architecture deviations during test fixes require document updates and full restart
