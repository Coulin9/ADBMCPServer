plugins {
    kotlin("jvm") version "2.3.0"
    application
    id("com.gradleup.shadow") version "8.3.6"
}

group = "com.coulin"
version = System.getenv("VERSION") ?: "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val mcpVersion = "0.9.0"
val ktorVersion = "3.4.1"

dependencies {
    testImplementation(kotlin("test"))
    implementation("io.ktor:ktor-server-netty:${ktorVersion}")
    implementation("io.modelcontextprotocol:kotlin-sdk-server:${mcpVersion}")
    implementation("io.ktor:ktor-server-core:${ktorVersion}")
    implementation("io.ktor:ktor-server-cio:${ktorVersion}")
    implementation("io.ktor:ktor-server-content-negotiation:${ktorVersion}")
    implementation("io.ktor:ktor-serialization-kotlinx-json:${ktorVersion}")
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("com.coulin.MainKt")
}

tasks.test {
    useJUnitPlatform()
}