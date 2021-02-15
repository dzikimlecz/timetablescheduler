import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.30"
    kotlin("plugin.serialization") version "1.4.10"
    application
    id("org.openjfx.javafxplugin") version "0.0.9"

}

group = "me.dzikimlecz"
version = "1.0-SNAPSHOT"

repositories {
    jcenter()
}

dependencies {
    testImplementation(kotlin("test-junit"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")
}

javafx {
    version = "15.0.1"
    modules("javafx.controls", "javafx.fxml")
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("me.dzikimlecz.AppKt")
}
