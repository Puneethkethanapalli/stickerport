package app.stickerport

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

/**
 * Swaps the real [StickerportApplication] for [HiltTestApplication] under instrumentation.
 *
 * ## Why a runner is needed at all
 * `@HiltAndroidTest` refuses to run when the Application is `@HiltAndroidApp`, and Hilt's
 * suggested fix — declaring `HiltTestApplication` in `src/androidTest/AndroidManifest.xml` — does
 * **not** work under AGP 9's default (non-orchestrated, non-self-instrumenting) setup, because the
 * generated instrumentation has
 *
 *     android:targetPackage="app.stickerport"
 *
 * so the *app under test's* manifest decides which Application is constructed. The test APK's own
 * `<application android:name=…>` is ignored. Every `@HiltAndroidTest` then fails at rule
 * construction with:
 *
 *     cannot use a @HiltAndroidApp application but found app.stickerport.StickerportApplication
 *
 * Overriding `newApplication` is the one hook that runs in the right process at the right moment.
 *
 * `@CustomTestApplication(HiltTestApplication::class)` is *not* an alternative: `HiltTestApplication`
 * is `final`, so the subclass Hilt generates does not compile.
 */
class HiltTestRunner : AndroidJUnitRunner() {

    override fun newApplication(
        classLoader: ClassLoader?,
        className: String?,
        context: Context?,
    ): Application = super.newApplication(
        classLoader,
        HiltTestApplication::class.java.name,
        context,
    )
}
