plugins {
    kotlin("jvm") version "1.3.72"
    kotlin("plugin.serialization") version "1.3.72"
}

group = "pro.schmid"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    val ktorVersion = "1.3.2"

    implementation(kotlin("stdlib-jdk8"))

    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-json-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-serialization-jvm:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.20.0")

    implementation("org.jetbrains.exposed", "exposed-core", "0.24.1")
    implementation("org.jetbrains.exposed", "exposed-dao", "0.24.1")
    implementation("org.jetbrains.exposed", "exposed-jdbc", "0.24.1")
    implementation("org.postgresql:postgresql:42.2.2")

    implementation(fileTree("lib") {
        include("*.jar")
    })
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}