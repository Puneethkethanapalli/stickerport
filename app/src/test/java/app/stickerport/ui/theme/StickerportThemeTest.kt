package app.stickerport.ui.theme

import android.os.Build
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * The **Robolectric** exercise in the catalog, and the last unproven test library.
 *
 * ## Why this needs Robolectric
 * The decision under test is "`SDK_INT >= S`, so use the wallpaper palette" — a branch on
 * `Build.VERSION.SDK_INT`. On the JVM that constant is whatever the host runs, so the *fallback*
 * path is unreachable without asking. Robolectric runs a real Android runtime per
 * `@Config(sdk = …)`, which makes both branches reachable in milliseconds.
 *
 * `StickerportTheme` also calls `dynamicLightColorScheme(LocalContext.current)`, which needs a real
 * `Context` and real resources — neither of which exists in a plain JVM test.
 *
 * ## Why the checkerboard assertions matter
 * spec §12.7 requires a subtle checkerboard behind transparent sticker art. Its tones must stay
 * *independent of the dynamic surface colour*, or artwork sits on a backdrop that shifts with the
 * user's wallpaper. That independence is a design decision worth pinning: it would be easy to
 * "tidy up" later by deriving the checker from `colorScheme.surfaceVariant`, and nobody would
 * notice until converted stickers looked wrong.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class StickerportThemeTest {

    @get:Rule
    val composeRule = createComposeRule()

    // ---- checkerboard tones (spec §12.7) ----

    @Test
    fun `light theme reports the light checker tones`() {
        composeRule.setContent {
            StickerportTheme(darkTheme = false, dynamicColor = false) {
                AssertTones(CheckerLight, CheckerSquareLight)
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun `dark theme reports the dark checker tones`() {
        composeRule.setContent {
            StickerportTheme(darkTheme = true, dynamicColor = false) {
                AssertTones(CheckerDark, CheckerSquareDark)
            }
        }
        composeRule.waitForIdle()
    }

    /**
     * The tones must be distinguishable or the checkerboard is a flat block, and neither may be
     * pure black or white or it reads as a hard grid over the artwork.
     */
    @Test
    fun `checker tones are subtle but distinct`() {
        listOf(
            "light" to (CheckerLight to CheckerSquareLight),
            "dark" to (CheckerDark to CheckerSquareDark),
        ).forEach { (name, tones) ->
            val (background, square) = tones
            assertNotEquals("$name: checker tones are identical", background, square)
            assertNotEquals("$name: square is pure black", Color.Black, square)
            assertNotEquals("$name: square is pure white", Color.White, square)
            assertNotEquals("$name: background is pure black", Color.Black, background)
            assertNotEquals("$name: background is pure white", Color.White, background)
        }
    }

    /**
     * The tonal *gap* between the two tones is what makes the pattern visible without dominating
     * the artwork. A gap so large that the two look like black and white has already failed the
     * "subtle" half of the requirement.
     */
    @Test
    fun `checker tones stay low contrast`() {
        listOf(
            "light" to (CheckerLight to CheckerSquareLight),
            "dark" to (CheckerDark to CheckerSquareDark),
        ).forEach { (name, tones) ->
            val (background, square) = tones
            val gap = background.luminance() - square.luminance()
            assertTrue(
                "$name: checker contrast $gap is too high to read as subtle (expected < 0.08)",
                kotlin.math.abs(gap) < 0.08f,
            )
        }
    }

    // ---- dynamic colour availability (the Robolectric-only half) ----

    /**
     * On API 30 there is no dynamic palette, so the brand fallback must be reported in use.
     * Without Robolectric this path is simply untestable — which is the whole reason this class
     * exists rather than being folded into the existing JVM tests.
     */
    @Test
    @Config(sdk = [30])
    fun `API 30 falls back to the brand palette`() {
        assertTrue(
            "this test is meaningless on a release that has dynamic colour",
            Build.VERSION.SDK_INT < Build.VERSION_CODES.S,
        )

        composeRule.setContent {
            StickerportTheme(darkTheme = false, dynamicColor = true) {
                assertTone("usesDynamicColor", expected = false, actual = StickerportTheme.usesDynamicColor)
                AssertTones(CheckerLight, CheckerSquareLight)
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    @Config(sdk = [34])
    fun `API 34 uses dynamic colour when requested`() {
        composeRule.setContent {
            StickerportTheme(darkTheme = false, dynamicColor = true) {
                assertTone("usesDynamicColor", expected = true, actual = StickerportTheme.usesDynamicColor)
            }
        }
        composeRule.waitForIdle()
    }

    /** Opting out must be honoured even where dynamic colour *is* available. */
    @Test
    @Config(sdk = [34])
    fun `dynamicColor false opts out on API 34`() {
        composeRule.setContent {
            StickerportTheme(darkTheme = false, dynamicColor = false) {
                assertTone("usesDynamicColor", expected = false, actual = StickerportTheme.usesDynamicColor)
                // Even opting out, the checker must still be the brand tone: it is a fixed
                // neutral backdrop by design, not a value derived from the colour scheme.
                AssertTones(CheckerLight, CheckerSquareLight)
            }
        }
        composeRule.waitForIdle()
    }

    // ---- helpers ----

    /** `@Composable` because `StickerportTheme.checker` reads a `CompositionLocal`. */
    @androidx.compose.runtime.Composable
    private fun AssertTones(expectedBackground: Color, expectedSquare: Color) {
        val checker = StickerportTheme.checker
        assertTone("checker background", expectedBackground, checker.background)
        assertTone("checker square", expectedSquare, checker.square)
    }

    private fun assertTone(what: String, expected: Any, actual: Any) {
        assertTrue("$what: expected $expected but was $actual", expected == actual)
    }

    private fun Color.luminance(): Float =
        (0.2126f * red + 0.7152f * green + 0.0722f * blue)
}
