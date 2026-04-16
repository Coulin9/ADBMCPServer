# ADB MCP Server

## Project Overview

**ADB MCP Server** is a Model Context Protocol (MCP) server that exposes Android Debug Bridge (ADB) capabilities to AI agents. It allows an LLM to interact with and control Android devices connected via ADB, enabling tasks such as UI automation, screenshot capture, and shell command execution.

## Architecture

*   **Language:** Kotlin (JVM)
*   **Framework:** Ktor (for the HTTP server)
*   **Protocol:** Model Context Protocol (MCP) using `io.modelcontextprotocol:kotlin-sdk-server`
*   **Core Logic:** `AdbService` acts as a wrapper around the local `adb` command-line tool.

## Key Files

*   `src/main/kotlin/mcp/MCPServer.kt`: Defines the MCP server, initializes the Ktor server, and registers the available tools.
*   `src/main/kotlin/adb/AdbService.kt`: Contains the logic for executing ADB commands (tap, swipe, screenshot, dump layout, etc.).
*   `src/main/kotlin/Main.kt`: The application entry point.
*   `build.gradle.kts`: Project build configuration and dependencies.

## Building and Running

### Prerequisites

*   JDK 24 (configured in `build.gradle.kts`)
*   Android SDK Platform-Tools (specifically `adb`) installed and available in the system PATH.

### Commands

*   **Build:**
    ```bash
    ./gradlew build
    ```

*   **Run:**
    ```bash
    ./gradlew run
    ```
    The server listens on `127.0.0.1:3000` by default.

## Exposed Tools

The server exposes the following MCP tools:

| Tool Name | Description |
| :--- | :--- |
| `adb_list_devices` | Lists currently connected devices and their status. |
| `adb_screenshot` | Captures a screenshot of the current screen. |
| `adb_get_ui_hierarchy` | Dumps the current UI layout hierarchy (XML). Essential for locating UI elements. |
| `adb_tap` | Taps at specific (x, y) coordinates. |
| `adb_swipe` | Swipes from one coordinate to another. |
| `adb_input_text` | Inputs text into the currently focused field (ASCII only). |
| `adb_press_key` | Simulates a physical key press (Home, Back, Enter, etc.). |
| `adb_start_app` | Starts an application by its package name. |
| `adb_install_app` | Installs an APK file to the device. Supports force reinstall and downgrade. |
| `adb_uninstall_app` | Uninstalls an app by its package name. |
| `adb_shell` | Executes a raw ADB shell command. |

## Development Conventions

*   **ADB Wrapper:** All interactions with the `adb` process are encapsulated in `AdbService`. New ADB features should be added here first.
*   **Tool Registration:** New MCP tools are defined and registered in `MCPServer.kt` inside the `initTools` method.
*   **Error Handling:** `AdbService` generally throws exceptions or returns error strings which are caught in `MCPServer` and returned as `CallToolResult` with `isError = true`.
