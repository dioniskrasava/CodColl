plugins {
    kotlin("jvm") version "1.9.0"
    id("org.jetbrains.compose") version "1.5.0"
    kotlin("plugin.serialization") version "1.9.0"
}

group = "app.codcoll"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    implementation(compose.materialIconsExtended)
}

compose.desktop {
    application {
        mainClass = "app.codcoll.MainKt"

        nativeDistributions {
            packageName = "Code collector"
            packageVersion = "1.0."
            description = "(v 1.0.) Сборщик кода для AI чатов"

            linux {
                appCategory = "Utility"
            }

            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.AppImage,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb
            )
        }
    }
}

kotlin {
    jvmToolchain(17)
}