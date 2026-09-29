plugins {
    alias(libs.plugins.android.library)
}

android {
    // `android.uniquePackageNames` is true in AGP 9, so every module needs a distinct namespace.
    namespace = "app.stickerport.nativewebp"
    compileSdk = 37
    compileSdkMinor = 2

    defaultConfig {
        // A library module has no applicationId and no minSdk of its own; the min SDK comes from
        // the consuming app module (26) so that the JNI layer is built and tested against the
        // real floor. Set explicitly anyway to keep the module self-describing.
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
}
