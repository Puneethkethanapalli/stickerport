package app.stickerport.nativewebp

/**
 * Entry point for the JNI layer that wraps libwebp (spec §9.5).
 *
 * Sprint 0 only creates the module so the project shape matches the plan; there is no CMake, no
 * vendored libwebp and no `externalBuild` block yet. Those arrive in T2.2 (vendored libwebp +
 * CMake + 16 KB page-size compatibility) and T2.3 / T9.1 (the JNI encoder entry points).
 */
object WebPNative {
    /** Name passed to [System.loadLibrary] once the native build exists (T2.2). */
    const val LIBRARY_NAME: String = "stickerport_webp"

    /** True once `System.loadLibrary(LIBRARY_NAME)` has succeeded in this process. */
    val isAvailable: Boolean get() = false
}
