import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.util.capitalizeDecapitalize.capitalizeAsciiOnly
import java.util.SimpleTimeZone

plugins {
    id("java")
    kotlin("jvm") version "1.9.22"
    `maven-publish`
    id("io.github.goooler.shadow") version "8.1.7"
}

group = "dev.marten_mrfcyt"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://javadoc.jitpack.io")
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
    maven("https://repo.opencollab.dev/maven-snapshots/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://libraries.minecraft.net")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    maven(url = "https://mvn.lumine.io/repository/maven-public/")
    maven("https://maven.enginehub.org/repo/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    compileOnly("net.kyori:adventure-text-minimessage:4.13.1")
    compileOnly("com.mojang:brigadier:1.0.18")
    compileOnly("io.lumine:Mythic-Dist:5.6.1")
    implementation("com.google.code.gson:gson:2.8.9")
    implementation(kotlin("reflect"))
    implementation("org.reflections:reflections:0.10.2")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.10")
    implementation("mlib.api:MLib:0.0.1")
}

val targetJavaVersion = 21
kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks.build {
    dependsOn("shadowJar")
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("paper-plugin.yml") {
        expand(props)
    }
}

task<ShadowJar>("buildAndMove") {
    dependsOn("shadowJar")

    group = "build"
    description = "Builds the jar and moves it to the server folder"

    doLast {
        val jar = file("build/libs/${project.name}-${version}-all.jar")
        val server = file("server/plugins/${project.name.capitalizeAsciiOnly()}-${version}.jar")

        if (server.exists()) {
            server.delete()
        }

        jar.copyTo(server, overwrite = true)
    }
}