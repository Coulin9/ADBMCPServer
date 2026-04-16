package com.coulin.adb

import java.io.File
import java.util.Base64
import java.util.concurrent.TimeUnit

/**
 * 简单的 ADB 命令执行器
 */
object AdbService {
    private const val ADB_CMD = "adb"
    private const val DEFAULT_TIMEOUT_SEC = 10L

    // 统一执行命令的方法
    fun execute(args: List<String>, timeoutSec: Long = DEFAULT_TIMEOUT_SEC, serial: String? = null): String {
        val command = if (serial != null) {
            listOf(ADB_CMD, "-s", serial) + args
        } else {
            listOf(ADB_CMD) + args
        }
        val process = ProcessBuilder(command)
            .redirectErrorStream(true) // 将错误流合并到输出流，方便 AI 看到错误信息
            .start()
        val output = process.inputStream.bufferedReader().use { it.readText() }
        val completed = process.waitFor(timeoutSec, TimeUnit.SECONDS)
        if (!completed) {
            process.destroy()
            throw RuntimeException("ADB command timed out: ${command.joinToString(" ")}")
        }
        // 简单的错误检查 (ADB 有时报错也会返回 0，所以检查输出)
        if (output.contains("error:") || output.contains("Aborted")) {
            // 这里可以根据需要决定是否抛出异常，或者直接返回错误信息给 AI
            // throw RuntimeException("ADB Error: $output")
        }
        return output.trim()
    }

    // === 功能实现 ===
    fun tap(x: Int, y: Int) = execute(listOf("shell", "input", "tap", x.toString(), y.toString()))

    fun swipe(x1: Int, y1: Int, x2: Int, y2: Int, durationMs: Int) =
        execute(listOf("shell", "input", "swipe", x1.toString(), y1.toString(), x2.toString(), y2.toString(), durationMs.toString()))

    fun inputText(text: String) {
        // 处理空格，ADB input text 不支持直接空格，通常替换为 %s
        val safeText = text.replace(" ", "%s")
        execute(listOf("shell", "input", "text", safeText))
    }

    fun pressKey(keyCode: Int) = execute(listOf("shell", "input", "keyevent", keyCode.toString()))

    fun startApp(packageName: String, activityName: String? = null) {
        val args = if (activityName != null) {
            listOf("shell", "am", "start", "-n", "$packageName/$activityName")
        } else {
            // 如果只知道包名，尝试通过 monkey 启动 (一种常见的 trick)
            listOf("shell", "monkey", "-p", packageName, "-c", "android.intent.category.LAUNCHER", "1")
        }
        execute(args)
    }

    fun stopApp(packageName: String) = execute(listOf("shell", "am", "force-stop", packageName))

    /**
     * 安装应用
     * @param apkPath APK 文件路径 (主机上的路径)
     * @param reinstall 是否强制覆盖安装 (-r)
     * @param downgrade 是否允许降级安装 (-d)
     */
    fun installApp(apkPath: String, reinstall: Boolean = false, downgrade: Boolean = false): String {
        val args = mutableListOf("install")
        if (reinstall) args.add("-r")
        if (downgrade) args.add("-d")
        args.add(apkPath)
        return execute(args, timeoutSec = 60) // 安装可能需要较长时间
    }

    /**
     * 卸载应用
     * @param packageName 应用包名
     */
    fun uninstallApp(packageName: String): String {
        return execute(listOf("uninstall", packageName))
    }

    /**
     * 获取 UI 布局 XML。
     * 这对 AI 非常重要，它可以通过 XML 分析出 "登录" 按钮的坐标。
     */
    fun getUiHierarchy(): String {
        // 1. Dump 到手机临时文件
        execute(listOf("shell", "uiautomator", "dump", "/sdcard/window_dump.xml"), timeoutSec = 15)
        // 2. 读取文件内容
        return execute(listOf("shell", "cat", "/sdcard/window_dump.xml"))
    }

    fun screenshot(): String {
        execute(listOf("shell", "screencap", "-p", "/sdcard/mcp_screen.png"))
        val tempFile = File.createTempFile("screenshot", ".png")
        execute(listOf("pull", "/sdcard/mcp_screen.png", tempFile.absolutePath))
        val bytes = tempFile.readBytes()
        tempFile.delete()
        return Base64.getEncoder().encodeToString(bytes)
    }

    /**
     * 获取设备基本信息。
     * @param serial 可选设备序列号，多设备时必须指定
     * @return 格式化的设备信息文本
     */
    fun getDeviceInfo(serial: String? = null): String {
        val sb = StringBuilder()

        // 1. 设备属性 (批量 getprop)
        sb.appendLine("=== 设备信息 ===")
        val props = listOf(
            "ro.product.manufacturer",
            "ro.product.model",
            "ro.product.name",
            "ro.build.version.release",
            "ro.build.version.sdk",
            "ro.board.platform",
            "ro.hardware"
        )
        val getpropCmd = props.joinToString("; ") { "getprop $it" }
        val getpropOutput = execute(listOf("shell", getpropCmd), serial = serial)
        val propValues = getpropOutput.lines().map { it.trim() }
        fun propAt(index: Int): String = propValues.getOrNull(index)?.takeIf { it.isNotBlank() } ?: "未知"
        sb.appendLine("品牌: ${propAt(0)}")
        sb.appendLine("型号: ${propAt(1)}")
        sb.appendLine("产品名: ${propAt(2)}")
        sb.appendLine("Android 版本: ${propAt(3)} (API ${propAt(4)})")
        sb.appendLine("芯片平台: ${propAt(5)}")
        sb.appendLine("硬件: ${propAt(6)}")
        sb.appendLine()

        // 2. 内存
        sb.appendLine("=== 内存 ===")
        try {
            val memOutput = execute(listOf("shell", "cat", "/proc/meminfo"), serial = serial)
            val memTotal = parseMemTotal(memOutput)
            sb.appendLine("总内存: $memTotal MB")
        } catch (e: Exception) {
            sb.appendLine("总内存: 获取失败 (${e.message})")
        }
        sb.appendLine()

        // 3. 屏幕
        sb.appendLine("=== 屏幕 ===")
        try {
            val screenOutput = execute(listOf("shell", "wm size; wm density"), serial = serial)
            val resolution = screenOutput.lines().firstOrNull { it.contains("Physical size:") || it.contains("Override size:") }
                ?.substringAfter(":")?.trim() ?: "未知"
            val dpi = screenOutput.lines().firstOrNull { it.contains("Physical density:") || it.contains("Override density:") }
                ?.substringAfter(":")?.trim() ?: "未知"
            sb.appendLine("分辨率: $resolution")
            sb.appendLine("DPI: $dpi")
        } catch (e: Exception) {
            sb.appendLine("分辨率: 获取失败 (${e.message})")
        }
        sb.appendLine()

        // 4. 存储
        sb.appendLine("=== 存储 ===")
        try {
            val dfOutput = execute(listOf("shell", "df", "/data"), serial = serial)
            val storageInfo = parseStorage(dfOutput)
            sb.appendLine("内部存储: $storageInfo")
        } catch (e: Exception) {
            sb.appendLine("内部存储: 获取失败 (${e.message})")
        }
        sb.appendLine()

        // 5. 电池
        sb.appendLine("=== 电池 ===")
        try {
            val batteryOutput = execute(listOf("shell", "dumpsys", "battery"), serial = serial)
            val batteryInfo = parseBattery(batteryOutput)
            sb.append(batteryInfo)
        } catch (e: Exception) {
            sb.appendLine("电池信息: 获取失败 (${e.message})")
        }

        return sb.toString().trimEnd()
    }

    internal fun parseMemTotal(output: String): String {
        val line = output.lines().firstOrNull { it.startsWith("MemTotal:") } ?: return "未知"
        val kb = line.replace(Regex("[^0-9]"), "").toLongOrNull() ?: return "未知"
        return (kb / 1024).toString()
    }

    internal fun parseStorage(output: String): String {
        // df 输出格式: Filesystem Size Used Avail Use% Mounted on
        val line = output.lines().lastOrNull { it.isNotBlank() && !it.startsWith("Filesystem") } ?: return "未知"
        val parts = line.trim().split(Regex("\\s+"))
        if (parts.size < 5) return "未知"
        val totalKb = parts[1].toLongOrNull()
        val usedKb = parts[2].toLongOrNull()
        val availKb = parts[3].toLongOrNull()
        if (totalKb == null) return "未知"
        val totalGb = totalKb / 1024 / 1024
        val usedGb = (usedKb ?: 0) / 1024 / 1024
        val availGb = (availKb ?: 0) / 1024 / 1024
        return "$totalGb GB (已用 $usedGb GB, 可用 $availGb GB)"
    }

    internal fun parseBattery(output: String): String {
        val sb = StringBuilder()
        val level = output.lines().firstOrNull { it.trim().startsWith("level:") }?.trim()?.substringAfter(":")?.trim()
        val statusCode = output.lines().firstOrNull { it.trim().startsWith("status:") }?.trim()?.substringAfter(":")?.trim()?.toIntOrNull()
        val temp = output.lines().firstOrNull { it.trim().startsWith("temperature:") }?.trim()?.substringAfter(":")?.trim()?.toIntOrNull()

        val statusText = when (statusCode) {
            1 -> "未知"
            2 -> "充电中"
            3 -> "放电中"
            4 -> "未充电"
            5 -> "已充满"
            else -> statusCode?.toString() ?: "未知"
        }

        sb.appendLine("电量: ${level ?: "未知"}%")
        sb.appendLine("状态: $statusText")
        if (temp != null) {
            sb.appendLine("温度: ${temp / 10}.${temp % 10}°C")
        } else {
            sb.appendLine("温度: 未知")
        }
        return sb.toString()
    }

    /**
     * 查询设备上的前台服务（Foreground Service）。
     * @param packageName 可选包名，不传则查询所有前台服务
     * @return 过滤后的前台服务记录，无前台服务时返回提示文本
     */
    fun getForegroundServices(packageName: String? = null): String {
        val args = mutableListOf("shell", "dumpsys", "activity", "services")
        if (packageName != null) {
            args.add(packageName)
        }
        val raw = execute(args)
        return filterForegroundRecords(raw)
    }

    /**
     * 按 "* ServiceRecord" 边界切分 dumpsys 输出，
     * 保留含 "isForeground=true" 的完整记录块。
     */
    private fun filterForegroundRecords(output: String): String {
        if (output.isBlank()) return "没有正在运行的前台服务"

        val lines = output.lines()
        val records = mutableListOf<MutableList<String>>()
        var currentRecord: MutableList<String>? = null

        for (line in lines) {
            if (line.trimStart().startsWith("* ServiceRecord")) {
                currentRecord = mutableListOf(line)
                records.add(currentRecord)
            } else if (currentRecord != null) {
                currentRecord.add(line)
            }
        }

        val foregroundRecords = records.filter { block ->
            block.any { it.contains("isForeground=true") }
        }

        if (foregroundRecords.isEmpty()) return "没有正在运行的前台服务"

        return foregroundRecords.joinToString("\n") { it.joinToString("\n") }
    }

}