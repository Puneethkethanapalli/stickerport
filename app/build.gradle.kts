plugins {
    alias(libs.plugins.android.application)
    // Required for @Composable. Must be applied even though AGP 9 has built-in Kotlin —
    // built-in Kotlin compiles the code; the Compose plugin supplies the Compose compiler.
    alias(libs.plugins.kotlin.compose)
    // Type-safe navigation routes (T0.3) are @Serializable classes, so the routes *are* the
    // arguments and no string route templates exist anywhere in the app.
    alias(libs.plugins.kotlin.serialization)
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

        // AGP 9's built-in default, stated explicitly so a future AGP bump cannot silently switch
        // runners and skip every instrumented test.
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    //
    // The BOM must be added to *every* configuration that resolves a Compose artifact, not just
    // `implementation` — androidTest and debug have their own classpaths and will fail with
    // "Could not find androidx.compose.ui:ui-test-junit4:." (an empty version) otherwise.
    val composeBom = platform(libs.compose.bom)
    add("implementation", composeBom)
    add("androidTestImplementation", composeBom)
    add("debugImplementation", composeBom)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    // material3 does not bring the Icons set with it; the top bar needs it.
    implementation(libs.compose.material.icons.core)

    // Navigation (T0.3). The type-safe route API in this version needs kotlinx-serialization on
    // the classpath, not just the compiler plugin.
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    // ui-tooling gives the live-preview / inspector; debug only, and it must not ship in release.
    debugImplementation(libs.compose.ui.tooling)

    // Compose UI tests (T0.3 "How to test"). `ui-test-manifest` is what lets `createComposeRule`
    // find an Activity; debug-only, and it is deprecated in favour of using the real
    // ComponentActivity, so it is scoped to test builds only.
    debugImplementation(libs.compose.ui.test.manifest)

    // ---- Instrumented tests (T0.3 onwards) ----
    // The device/emulator side. Uses JUnit4 because that is what the Android instrumentation
    // runner speaks; the JUnit5 setup in T0.5 is for the *unit* (JVM) side only.
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.junit4)
}
