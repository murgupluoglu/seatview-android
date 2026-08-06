plugins {
    alias(libs.plugins.android.application)
}

kotlin {
    jvmToolchain(17)
}

android {
    compileSdk = 37
    defaultConfig {
        applicationId = "com.murgupluoglu.seatviewsample"
        minSdk = 21
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        buildConfig = true // App.kt guards Timber planting with BuildConfig.DEBUG
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
    namespace = "com.murgupluoglu.seatviewsample"
}

dependencies {
    //Test
    androidTestImplementation(libs.bundles.androidx.test)
    androidTestImplementation(libs.truth)
    //

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.timber)

    implementation(project(":seatview"))
}
