// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
}

// Repositories are declared centrally in settings.gradle.kts (dependencyResolutionManagement).

tasks.register<Delete>("clean") {
    description = "clean task"
    delete(rootProject.layout.buildDirectory)
}
