package com.coulin.mcp

import com.coulin.adb.AdbService
import io.modelcontextprotocol.kotlin.sdk.types.*
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.cio.CIO
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.mcpStreamableHttp
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.McpJson
import io.modelcontextprotocol.kotlin.sdk.types.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.types.Tool
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import kotlin.collections.emptyMap

object MCPServer {

    private var server: Server? = null

    private const val LOCALHOST = "127.0.0.1"

    private const val LOCAL_PORT = 3001

    private const val SERVER_NAME = "adb_mcp_server"

    private const val SERVER_VERSION = "1.0.0"

    private fun tryInitServer() {
        if (server == null) {
            synchronized(MCPServer::class.java) {
                if (server == null) {
                    server = Server(
                        serverInfo = Implementation(
                            name = SERVER_NAME,
                            version = SERVER_VERSION,
                        ),
                        options = ServerOptions(
                            capabilities = ServerCapabilities(
                                tools = ServerCapabilities.Tools(listChanged = true),
                            )
                        )
                    )
                    initTools()
                }
            }
        }
    }

    private fun initTools() {
        server ?: return
        val listDevicesTool = Tool(
            name = "adb_list_devices",
            description = """当需要知道当前连接的设备和状态时使用该工具。
✅ 正确: 用户说"帮我看看手机连上了没" → 调用 adb_list_devices 查看连接状态
✅ 正确: 操作前先调用此工具确认有设备在线，避免后续工具调用失败
❌ 错误: 用户问"我的手机是什么型号" → 不应使用此工具，应使用 adb_get_device_info 获取详细设备信息""",
            inputSchema = ToolSchema(
                properties = JsonObject(emptyMap())
            )
        )
        val screenshotTool = Tool(
            name = "adb_screenshot",
            description = """获取当前屏幕截图。当你想看屏幕上显示什么时使用此工具。
✅ 正确: 执行点击操作后，调用截图确认页面是否跳转成功
✅ 正确: 用户说"看看屏幕上显示什么" → 调用截图查看当前画面
❌ 错误: 需要获取某个按钮的精确坐标来点击 → 截图无法提供坐标信息，应使用 adb_get_ui_hierarchy 获取 UI 元素的 bounds 属性""",
            inputSchema = ToolSchema(
                properties = JsonObject(emptyMap())
            )
        )
        val dumpLayoutTool = Tool(
            name = "adb_get_ui_hierarchy",
            description = """获取当前屏幕的 UI 布局 XML 结构。当你需要查找按钮位置、读取屏幕文字内容，或者截图不够清晰时，请务必使用此工具。
✅ 正确: 需要点击"登录"按钮 → 先调用此工具获取 XML，从 bounds="[236,1056][544,1124]" 中计算中心坐标 (390, 1090)，再调用 adb_tap
✅ 正确: 需要读取屏幕上的文字内容（如验证码、提示信息）→ 调用此工具从 XML 的 text 属性中提取
❌ 错误: 只是想看看屏幕大概长什么样 → 此工具返回的是 XML 结构数据，应先使用 adb_screenshot 查看画面概览""",
            inputSchema = ToolSchema(
                properties = JsonObject(emptyMap())
            )
        )
        val tapTool = Tool(
            name = "adb_tap",
            description = """需要点击屏幕上的指定坐标 (x, y)时使用该工具。建议先使用 screenshot 或 get_ui_hierarchy 确认坐标。
✅ 正确: 通过 adb_get_ui_hierarchy 得知"确认"按钮 bounds="[100,500][300,600]"，计算中心点后调用 adb_tap(x=200, y=550)
❌ 错误: 没有先获取 UI 布局或截图，凭猜测调用 adb_tap(x=500, y=500) → 可能点击到错误的位置，应先用 adb_screenshot 或 adb_get_ui_hierarchy 确认坐标
❌ 错误: 想向下滚动页面，调用 adb_tap → 点击无法实现滚动，应使用 adb_swipe""",
            inputSchema = ToolSchema(
                properties = buildJsonObject {
                    putJsonObject("x") { put("type", "integer"); put("description", "X 坐标") }
                    putJsonObject("y") { put("type", "integer"); put("description", "Y 坐标") }
                },
                required = listOf("x", "y")
            )
        )
        val swipeTool = Tool(
            name = "adb_swipe",
            description = """当需要在屏幕上执行滑动操作 (从点1滑动到点2)时使用该工具。用于滚动列表或解锁屏幕。
✅ 正确: 向下滚动列表查看更多内容 → adb_swipe(startX=540, startY=1800, endX=540, endY=600, durationMs=500)
✅ 正确: 需要精确滑动（如滑块验证、进度条调节）→ 先调用 adb_get_ui_hierarchy 获取滑块元素的 bounds 属性确定起止坐标，再执行 adb_swipe
❌ 错误: 想点击某个按钮 → adb_swipe(startX=200, startY=550, endX=200, endY=550, durationMs=100)，起点终点相同的滑动不等于点击，应使用 adb_tap
❌ 错误: 快速滑动 durationMs=0 → 时间过短可能不被设备识别，建议至少 100ms""",
            inputSchema = ToolSchema(
                properties = buildJsonObject {
                    putJsonObject("startX") { put("type", "integer"); put("description", "起始 X 坐标") }
                    putJsonObject("startY") { put("type", "integer"); put("description", "起始 Y 坐标") }
                    putJsonObject("endX") { put("type", "integer"); put("description", "结束 X 坐标") }
                    putJsonObject("endY") { put("type", "integer"); put("description", "结束 Y 坐标") }
                    putJsonObject("durationMs") {
                        put("type", "integer")
                        put("description", "滑动持续时间(毫秒)，默认 500。时间越短速度越快。")
                    }
                },
                required = listOf("startX", "startY", "endX", "endY", "durationMs")
            )
        )
        val inputTextTool = Tool(
            name = "adb_input_text",
            description = """需要在输入框中输入文本时使用该工具。注意：1.不支持输入非 ASCII 字符（如中文）。2.如果文本框未聚焦，先使用点击工具聚焦相应文本框。
✅ 正确: 先用 adb_tap 点击搜索框使其获得焦点，再调用 adb_input_text(text="hello world") 输入文字
❌ 错误: 直接调用 adb_input_text(text="你好世界") → 不支持非 ASCII 字符，中文会输入乱码或失败。如需输入中文，应使用 adb_shell 配合 ADBKeyBoard 等输入法
❌ 错误: 未先点击输入框就调用 adb_input_text → 文本可能输入到错误的位置或无响应，必须先用 adb_tap 聚焦目标输入框""",
            inputSchema = ToolSchema(
                buildJsonObject {
                    putJsonObject("text") {
                        put("type", "string")
                        put("description", "要输入的英文字符串")
                    }
                },
                required = listOf("text")
            )
        )
        val pressKeyTool = Tool(
            name = "adb_press_key",
            description = """模拟物理按键功能时使用该工具，如返回，增减音量，锁屏；屏幕点击与滑动不要使用该工具。常用 KeyCode: 3=HOME, 4=BACK, 24=音量+, 25=音量-, 26=电源, 66=ENTER, 67=DEL
✅ 正确: 用户说"返回上一页" → adb_press_key(keyCode=4)，使用 BACK 键返回
✅ 正确: 用户说"回到桌面" → adb_press_key(keyCode=3)，使用 HOME 键
❌ 错误: 想点击屏幕上的"返回"按钮图标 → 不应使用 adb_press_key，物理返回键和屏幕上的返回按钮不同，应先获取按钮坐标后使用 adb_tap
❌ 错误: 想滑动屏幕 → adb_press_key 只能模拟物理按键，滑动应使用 adb_swipe""",
            inputSchema = ToolSchema(
                properties = buildJsonObject {
                    putJsonObject("keyCode") {
                        put("type", "integer")
                        put("description", "Android KeyCode (整数)")
                    }
                },
                required = listOf("keyCode")
            )
        )
        val startAppTool = Tool(
            name = "adb_start_app",
            description = """需要启动一个特定的应用时使用该工具。
✅ 正确: 用户说"打开设置" → adb_start_app(packageName="com.android.settings")
✅ 正确: 用户说"打开微信" → adb_start_app(packageName="com.tencent.mm")
❌ 错误: adb_start_app(packageName="设置") → 必须传入包名而非应用名称。如不知道包名，先用 adb_shell(command="pm list packages | grep keyword") 查找
❌ 错误: adb_start_app(packageName="settings.apk") → 传入的是文件名而非包名""",
            inputSchema = ToolSchema(
                buildJsonObject {
                    putJsonObject("packageName") {
                        put("type", "string")
                        put("description", "应用的包名 (如 com.android.settings)")
                    }
                },
                required = listOf("packageName")
            )
        )
        val installAppTool = Tool(
            name = "adb_install_app",
            description = """需要安装 APK 文件到设备时使用该工具。支持覆盖安装和降级安装。如果遇到安装失败，根据失败信息选择强制覆盖安装或降级安装。
✅ 正确: adb_install_app(apkPath="/Users/me/Downloads/app-release.apk") → 使用主机上的绝对路径安装
✅ 正确: 首次安装失败提示已存在 → 重试 adb_install_app(apkPath="/Users/me/Downloads/app.apk", installReplace=true) 覆盖安装
❌ 错误: adb_install_app(apkPath="/sdcard/Download/app.apk") → 这是设备上的路径，不是主机路径，应使用主机上 APK 的绝对路径
❌ 错误: adb_install_app(apkPath="app.apk") → 必须使用绝对路径，不能使用相对路径""",
            inputSchema = ToolSchema(
                properties = buildJsonObject {
                    putJsonObject("apkPath") {
                        put("type", "string")
                        put("description", "APK 文件在主机上的绝对路径")
                    }
                    putJsonObject("installReplace") {
                        put("type", "boolean")
                        put("description", "是否强制覆盖安装 (-r)，默认为 false")
                    }
                    putJsonObject("installDowngrade") {
                        put("type", "boolean")
                        put("description", "是否允许降级安装 (-d)，默认为 false")
                    }
                },
                required = listOf("apkPath")
            )
        )
        val uninstallAppTool = Tool(
            name = "adb_uninstall_app",
            description = """需要卸载某个应用时使用该工具。
✅ 正确: adb_uninstall_app(packageName="com.example.myapp") → 通过包名卸载应用
❌ 错误: adb_uninstall_app(packageName="/Users/me/Downloads/app.apk") → 传入了 APK 路径，应传入应用包名
❌ 错误: adb_uninstall_app(packageName="微信") → 传入了应用名称，应传入包名如 "com.tencent.mm"""",
            inputSchema = ToolSchema(
                properties = buildJsonObject {
                    putJsonObject("packageName") {
                        put("type", "string")
                        put("description", "要卸载的应用包名")
                    }
                },
                required = listOf("packageName")
            )
        )
        val shellTool = Tool(
            name = "adb_shell",
            description = """执行原始 ADB Shell 命令。这是一个高级工具，仅在其他工具无法满足需求时使用。
✅ 正确: 查看已安装应用列表 → adb_shell(command="pm list packages")
✅ 正确: 查看当前 Activity → adb_shell(command="dumpsys activity activities | grep mResumedActivity")
❌ 错误: 截图 → adb_shell(command="screencap -p /sdcard/screen.png")，已有专用工具 adb_screenshot
❌ 错误: 点击屏幕 → adb_shell(command="input tap 500 500")，已有专用工具 adb_tap""",
            inputSchema = ToolSchema(
                properties = buildJsonObject {
                    putJsonObject("command") { put("type", "string"); put("description", "Shell 命令") }
                },
                required = listOf("command")
            )
        )
        val foregroundServicesTool = Tool(
            name = "adb_foreground_services",
            description = """查询设备上正在运行的前台服务（Foreground Service）。当你需要确认某个服务是否以前台服务方式运行时使用该工具。
✅ 正确: 检查音乐播放器是否在前台运行 → adb_foreground_services(packageName="com.spotify.music")
✅ 正确: 查看所有前台服务 → adb_foreground_services()，不传 packageName
❌ 错误: 想查看所有正在运行的进程 → 此工具只查前台服务，查进程应使用 adb_shell(command="ps -A")
❌ 错误: 想查看某个应用是否已安装 → 应使用 adb_shell(command="pm list packages | grep 包名")""",
            inputSchema = ToolSchema(
                properties = buildJsonObject {
                    putJsonObject("packageName") {
                        put("type", "string")
                        put("description", "包名，不传则查询所有前台服务")
                    }
                }
            )
        )
        val deviceInfoTool = Tool(
            name = "adb_get_device_info",
            description = """需要获取设备信息时使用该工具。获取设备的基本信息，包括品牌、型号、Android 版本、芯片、内存、屏幕、存储、电池等。多设备时请传入设备序列号。
✅ 正确: 用户问"我的手机是什么型号" → adb_get_device_info()
✅ 正确: 多设备连接时 → adb_get_device_info(serial="emulator-5554") 指定设备
❌ 错误: 想查看设备是否连接 → 此工具获取的是详细硬件信息，查连接状态应使用 adb_list_devices
❌ 错误: 多设备时不传 serial → 可能查询到错误的设备或报错，多设备时必须指定 serial""",
            inputSchema = ToolSchema(
                properties = buildJsonObject {
                    putJsonObject("serial") {
                        put("type", "string")
                        put("description", "设备序列号（来自 adb_list_devices），多设备时必须指定")
                    }
                }
            )
        )

        server?.addTool(listDevicesTool) {
            CallToolResult(content = listOf(TextContent(text = AdbService.execute(listOf("devices", "-l")))))
        }
        server?.addTool(screenshotTool) {
            try {
                val base64 = AdbService.screenshot()
                CallToolResult(content = listOf(ImageContent(data = base64, mimeType = "image/png")))
            } catch (e: Exception) {
                CallToolResult(isError = true, content = listOf(TextContent(text = "截图失败: ${e.message}, 如果连续失败，可以尝试使用获取布局工具")))
            }
        }
        server?.addTool(dumpLayoutTool) {
            try {
                // UI XML 通常很大，但对 LLM 理解界面结构至关重要
                val xml = AdbService.getUiHierarchy()
                CallToolResult(content = listOf(TextContent(text = xml)))
            } catch (e: Exception) {
                CallToolResult(isError = true, content = listOf(TextContent(text = "获取布局失败: ${e.message}， 如果连续失败，尝试使用截图工具")))
            }
        }
        server?.addTool(tapTool) { request ->
            val args = request.arguments
            val x = args?.get("x")?.jsonPrimitive?.int ?: throw IllegalArgumentException("Missing x")
            val y = args?.get("y")?.jsonPrimitive?.int ?: throw IllegalArgumentException("Missing y")
            AdbService.tap(x, y)
            CallToolResult(content = listOf(TextContent(text = "已点击坐标 ($x, $y)")))
        }
        server?.addTool(swipeTool) { request ->
            val args = request.arguments
            val startX = args?.get("startX")?.jsonPrimitive?.int ?: throw IllegalArgumentException("Missing startX")
            val startY = args?.get("startY")?.jsonPrimitive?.int ?: throw IllegalArgumentException("Missing startY")
            val endX = args?.get("endX")?.jsonPrimitive?.int ?: throw IllegalArgumentException("Missing endX")
            val endY = args?.get("endY")?.jsonPrimitive?.int ?: throw IllegalArgumentException("Missing endY")
            val duration = args?.get("durationMs")?.jsonPrimitive?.int ?: 500
            AdbService.swipe(startX, startY, endX, endY, duration)
            CallToolResult(content = listOf(TextContent(text = "已滑动 ($startX,$startY) -> ($endX,$endY)")))
        }
        server?.addTool(inputTextTool) { request ->
            val args = request.arguments
            val text = args?.get("text")?.jsonPrimitive?.content ?: throw IllegalArgumentException("Missing text")
            AdbService.inputText(text)
            CallToolResult(content = listOf(TextContent(text = "已输入文本: $text")))
        }
        server?.addTool(pressKeyTool) { request ->
            val args = request.arguments
            val code = args?.get("keyCode")?.jsonPrimitive?.int ?: throw IllegalArgumentException("Missing keyCode")
            AdbService.pressKey(code)
            CallToolResult(content = listOf(TextContent(text = "已按键 KeyCode: $code")))
        }
        server?.addTool(startAppTool) { request ->
            val args = request.arguments
            val pkg = args?.get("packageName")?.jsonPrimitive?.content ?: throw IllegalArgumentException("Missing packageName")
            AdbService.startApp(pkg)
            CallToolResult(content = listOf(TextContent(text = "尝试启动应用: $pkg")))
        }
        server?.addTool(installAppTool) { request ->
            val args = request.arguments
            val apkPath = args?.get("apkPath")?.jsonPrimitive?.content ?: throw IllegalArgumentException("Missing apkPath")
            val reinstall = args.get("installReplace")?.jsonPrimitive?.boolean ?: false
            val downgrade = args.get("installDowngrade")?.jsonPrimitive?.boolean ?: false
            val output = "安装结果：${AdbService.installApp(apkPath, reinstall, downgrade)}"
            CallToolResult(content = listOf(TextContent(text = output)))
        }
        server?.addTool(uninstallAppTool) { request ->
            val args = request.arguments
            val pkg = args?.get("packageName")?.jsonPrimitive?.content ?: throw IllegalArgumentException("Missing packageName")
            val output = AdbService.uninstallApp(pkg)
            CallToolResult(content = listOf(TextContent(text = output)))
        }
        server?.addTool(shellTool) { request ->
            val args = request.arguments
            val cmd = args?.get("command")?.jsonPrimitive?.content ?: throw IllegalArgumentException("Missing command")
            val output = AdbService.execute(listOf("shell", cmd))
            CallToolResult(content = listOf(TextContent(text = output)))
        }
        server?.addTool(foregroundServicesTool) { request ->
            val pkg = request.arguments?.get("packageName")?.jsonPrimitive?.content
            val output = AdbService.getForegroundServices(pkg)
            CallToolResult(content = listOf(TextContent(text = output)))
        }
        server?.addTool(deviceInfoTool) { request ->
            try {
                val serial = request.arguments?.get("serial")?.jsonPrimitive?.content
                val output = AdbService.getDeviceInfo(serial)
                CallToolResult(content = listOf(TextContent(text = output)))
            } catch (e: Exception) {
                CallToolResult(isError = true, content = listOf(TextContent(text = "获取设备信息失败: ${e.message}")))
            }
        }
    }

    fun launch() {
        tryInitServer()
        server?.let {
            embeddedServer(CIO, host = LOCALHOST, port = LOCAL_PORT) {
                install(ContentNegotiation) {
                    json(McpJson)
                }
                mcpStreamableHttp {
                    it
                }
            }.start(wait = true)
        }
    }
}