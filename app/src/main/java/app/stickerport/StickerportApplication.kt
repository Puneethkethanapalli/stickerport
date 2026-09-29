package app.stickerport

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * The Hilt application class (T0.4).
 *
 * `@HiltAndroidApp` is what generates the top-level `SingletonComponent` and, critically, the
 * `Hilt_StickerportApplication` superclass. Without it Hilt cannot inject into any `Activity`, and
 * the failure only shows up at runtime when the first injection point is reached.
 *
 * The manifest must name this class via `android:name=".StickerportApplication"`. That is the one
 * wiring step with no compiler check behind it, which is exactly why T0.4's acceptance test is
 * "app launches with Hilt without crashing" rather than "it compiles".
 */
@HiltAndroidApp
class StickerportApplication : Application()
