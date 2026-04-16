package com.coulin.adb

import kotlin.test.Test
import kotlin.test.assertTrue

class AdbServiceDeviceInfoTest {

    @Test
    fun getDeviceInfoReturnsAllSections() {
        val output = AdbService.getDeviceInfo()
        println("=== Device Info Output ===")
        println(output)
        println("=== End ===")

        assertTrue(output.contains("=== 设备信息 ==="), "Missing 设备信息 section")
        assertTrue(output.contains("=== 内存 ==="), "Missing 内存 section")
        assertTrue(output.contains("=== 屏幕 ==="), "Missing 屏幕 section")
        assertTrue(output.contains("=== 存储 ==="), "Missing 存储 section")
        assertTrue(output.contains("=== 电池 ==="), "Missing 电池 section")
        assertTrue(output.contains("品牌:"), "Missing 品牌 field")
        assertTrue(output.contains("型号:"), "Missing 型号 field")
        assertTrue(output.contains("Android 版本:"), "Missing Android 版本 field")
    }

    @Test
    fun getDeviceInfoWithSerial() {
        val devices = AdbService.execute(listOf("devices"))
        val serialLine = devices.lines().firstOrNull { it.contains("device") && !it.startsWith("List") }
        if (serialLine != null) {
            val serial = serialLine.trim().split(Regex("\\s+")).first()
            val output = AdbService.getDeviceInfo(serial)
            println("=== Device Info (serial=$serial) ===")
            println(output)

            assertTrue(output.contains("=== 设备信息 ==="), "Missing 设备信息 section with serial")
            assertTrue(output.contains("品牌:"), "Missing 品牌 with serial")
        }
    }
}
