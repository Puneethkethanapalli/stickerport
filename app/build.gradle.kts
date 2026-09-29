plugins {
    alias(libs.plugins.android.application)
    // Required for @Composable. Must be applied even though AGP 9 has built-in Kotlin —
    // built-in Kotlin compiles the code; the Compose plugin supplies the Compose compiler.
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "app.stickerport"

    // Compile against the newest stable platform (Android 17 / API 37.2) but *target* 36.
    //
    // `compileSdk` only decides which APIs are visible; `targetSdk` opts the app in to that
    // platform's runtime behaviour changes. Google Play requires new apps and updates to target
    // API 36 as of 2026-08-31, so 36 is what we target; compiling against 37.2 lets us use new
    // APIs while staying off the Android 17 behaviour-change list until it has been tested.
    //
    // API 37 ships minor versions (37.0 / 37.1 / 37.2) and the SDK package names match, e.g.
    // `platforms;android-37.2` — there is no plain `platforms;android-37`.
    compileSdk = 37
    compileSdkMinor = 2

    defaultConfig {
        applicationId = "app.stickerport"
        // NFR-1. minSdk 26 is what the spec pins; do not lower it casually.
        minSdk = 26
        // NFR-1. Google Play requires new apps and updates to target API 36 as of 2026-08-31.
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        debug {
            // Keep debug fast: no minification, no resource shrinking.
            isMinifyEnabled = false
            isShrinkResources = false
        }
        release {
            // R8 on for release (spec §6 "Build").
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            // No signingConfig yet: `assembleRelease` produces an *unsigned* release APK.
            // Real signing is a Play-readiness task (T14.2), not a skeleton task.
        }
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // The Compose BOM pins every androidx.compose.* artifact to one compatible set. Individual
    // Compose libraries must NOT carry their own version.
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)

    // ui-tooling gives the live-preview / inspector; debug only, and it must not ship in release.
    debugImplementation(libs.compose.ui.tooling)
}
