import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
    kotlin("plugin.serialization") version "1.7.10"
    id("com.github.johnrengelman.shadow").version("7.1.2")

    `java-library`
}

group = "com.dallonf"
version = "0.1-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("org.antlr:antlr4-runtime:4.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0-RC")
    testImplementation(kotlin("test"))
    implementation("com.github.hiking93:grapheme-splitter-lite:0.0.1")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "18"
}

sourceSets.main {
    java {
        srcDir("${projectDir}/src/${this@main.name}/gen")
    }
}
