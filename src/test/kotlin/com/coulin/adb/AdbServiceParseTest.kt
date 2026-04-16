package com.coulin.adb

import kotlin.test.Test
import kotlin.test.assertEquals

class AdbServiceParseTest {

    // === parseMemTotal ===

    @Test
    fun parseMemTotalNormal() {
        val output = "MemTotal:        7804920 kB\nMemFree:          123456 kB"
        assertEquals("7621", AdbService.parseMemTotal(output))
    }

    @Test
    fun parseMemTotalLarge() {
        val output = "MemTotal:        16384000 kB"
        assertEquals("16000", AdbService.parseMemTotal(output))
    }

    @Test
    fun parseMemTotalEmpty() {
        assertEquals("未知", AdbService.parseMemTotal(""))
    }

    @Test
    fun parseMemTotalNoMemTotalLine() {
        val output = "MemFree:          123456 kB\nBuffers:           12345 kB"
        assertEquals("未知", AdbService.parseMemTotal(output))
    }

    // === parseStorage ===

    @Test
    fun parseStorageNormal() {
        val output = "Filesystem      Size  Used Avail Use% Mounted on\n/dev/fuse      268435456 95420416 173015040  36% /storage/emulated"
        val result = AdbService.parseStorage(output)
        assertEquals("256 GB (已用 91 GB, 可用 165 GB)", result)
    }

    @Test
    fun parseStorageHeaderOnly() {
        val output = "Filesystem      Size  Used Avail Use% Mounted on"
        assertEquals("未知", AdbService.parseStorage(output))
    }

    @Test
    fun parseStorageEmpty() {
        assertEquals("未知", AdbService.parseStorage(""))
    }

    // === parseBattery ===

    @Test
    fun parseBatteryNormal() {
        val output = "  level: 85\n  status: 2\n  temperature: 280"
        val result = AdbService.parseBattery(output)
        assert(result.contains("电量: 85%"))
        assert(result.contains("状态: 充电中"))
        assert(result.contains("温度: 28.0°C"))
    }

    @Test
    fun parseBatteryDischarging() {
        val output = "  level: 42\n  status: 3\n  temperature: 315"
        val result = AdbService.parseBattery(output)
        assert(result.contains("电量: 42%"))
        assert(result.contains("状态: 放电中"))
        assert(result.contains("温度: 31.5°C"))
    }

    @Test
    fun parseBatteryFull() {
        val output = "  level: 100\n  status: 5\n  temperature: 250"
        val result = AdbService.parseBattery(output)
        assert(result.contains("电量: 100%"))
        assert(result.contains("状态: 已充满"))
    }

    @Test
    fun parseBatteryMissingFields() {
        val output = "  USB powered: true"
        val result = AdbService.parseBattery(output)
        assert(result.contains("电量: 未知%"))
        assert(result.contains("状态: 未知"))
        assert(result.contains("温度: 未知"))
    }

    @Test
    fun parseBatteryEmpty() {
        val result = AdbService.parseBattery("")
        assert(result.contains("电量: 未知%"))
        assert(result.contains("温度: 未知"))
    }
}
