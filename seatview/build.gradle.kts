plugins {
    alias(libs.plugins.android.library)
    `maven-publish`
}

// JitPack requires the maven-publish plugin to produce an installable artifact; it builds
// the tag with `./gradlew build publishToMavenLocal`. Consumers resolve this module as
// com.github.murgupluoglu:seatview-android:<tag> (the repo-level aggregate JitPack generates).
group = "com.github.murgupluoglu"
version = "4.0.0"

kotlin {
    jvmToolchain(17)
    compilerOptions {
        // Emit real JVM default methods for interface members with bodies, so Java consumers
        // implementing Seat / SeatViewListener inherit the defaults instead of being forced
        // to override every method. ABI-affecting, hence set for the 4.0.0 major.
        freeCompilerArgs.add("-Xjvm-default=all")
    }
}

android {
    compileSdk = 37

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    namespace = "com.murgupluoglu.seatview"
    publishing {
        // AGP requires the variant to be declared before maven-publish can consume it.
        singleVariant("release") {
            withSourcesJar()
        }
    }
    lint {
        targetSdk = 37
    }
    testOptions {
        targetSdk = 37
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
}

publishing {
    publications {
        register<MavenPublication>("release") {
            // `components["release"]` only exists once AGP has created the variant.
            afterEvaluate { from(components["release"]) }
            artifactId = "seatview"
        }
    }
}

// Robolectric requires Java 21 to emulate SDK 36+. Keep the library's own bytecode at
// Java 17 (jvmToolchain above) and run only the unit tests on a Java 21 launcher, so the
// published artifact does not force consumers onto a newer JDK.
tasks.withType<Test>().configureEach {
    javaLauncher.set(
        javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    )
    // Emulating SDK 36+ makes Robolectric's FileDescriptorInterceptor reach into
    // jdk.internal.access.SharedSecrets (via ApplicationSharedMemory, new in API 36).
    // The module system blocks that on modern JDKs, so it must be exported explicitly —
    // note this needs --add-exports (the package is not merely encapsulated, it is unexported).
    jvmArgs(
        "--add-exports=java.base/jdk.internal.access=ALL-UNNAMED",
        "--add-opens=java.base/java.lang=ALL-UNNAMED",
        "--add-opens=java.base/java.io=ALL-UNNAMED",
        "--add-opens=java.base/java.util=ALL-UNNAMED",
    )
}

dependencies {
    //Test
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.truth)
    androidTestImplementation(libs.bundles.androidx.test)
    androidTestImplementation(libs.truth)
    //

    // Deliberately narrow: the library needs ViewCompat/AccessibilityNodeInfoCompat (core)
    // and ExploreByTouchHelper (customview) — not all of appcompat, which consumers should
    // not be forced to take on.
    implementation(libs.androidx.core)
    implementation(libs.androidx.customview)
}
