import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
    kotlin("plugin.serialization") version "1.7.10"
}

group = "com.dallonf"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.antlr:antlr4-runtime:4.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0-RC")
    testImplementation(kotlin("test"))
    testImplementation("com.tylerthrailkill.helpers:pretty-print:v2.0.8")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

sourceSets.main {
    java {
        srcDir("${projectDir}/src/${this@main.name}/gen")
    }
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
