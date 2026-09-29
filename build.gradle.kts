// Stickerport — root build script.
//
// AGP 9 has *built-in Kotlin*: the `org.jetbrains.kotlin.android` plugin must NOT be applied
// (see https://developer.android.com/build/migrate-to-built-in-kotlin). AGP bundles KGP 2.2.10,
// so the only reason to declare a `buildscript` classpath here is to move KGP to a newer version.
buildscript {
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.20")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}
