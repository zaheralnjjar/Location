plugins {
    id("com.android.application") version "8.6.1" apply false
    // Kotlin 2.0+ is required for the org.jetbrains.kotlin.plugin.compose Gradle plugin
    // (the Compose Compiler moved out of the monolithic Kotlin plugin starting with Kotlin 2.0).
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
}
