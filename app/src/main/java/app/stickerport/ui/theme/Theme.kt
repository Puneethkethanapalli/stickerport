package app.stickerport.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = GreenPrimaryLight,
    onPrimary = GreenOnPrimaryLight,
    primaryContainer = GreenContainerLight,
    onPrimaryContainer = GreenOnContainerLight,
    secondary = TealSecondaryLight,
    onSecondary = TealOnSecondaryLight,
    secondaryContainer = TealContainerLight,
    onSecondaryContainer = TealOnContainerLight,
    tertiary = BlueTertiaryLight,
    onTertiary = BlueOnTertiaryLight,
    tertiaryContainer = BlueContainerLight,
    onTertiaryContainer = BlueOnTertiaryContainerLight,
    error = RedErrorLight,
    onError = RedOnErrorLight,
    errorContainer = RedErrorContainerLight,
    onErrorContainer = RedOnErrorContainerLight,
    // A fixed neutral backdrop rather than the derived `background`, so transparent sticker art
    // sits on a predictable, low-chroma surface in both themes (spec §12.7).
    background = CheckerLight,
    surface = CheckerLight,
)

private val DarkColors = darkColorScheme(
    primary = GreenPrimaryDark,
    onPrimary = GreenOnPrimaryDark,
    primaryContainer = GreenContainerDark,
    onPrimaryContainer = GreenOnContainerDark,
    secondary = TealSecondaryDark,
    onSecondary = TealOnSecondaryDark,
    secondaryContainer = TealContainerDark,
    onSecondaryContainer = TealOnContainerDark,
    tertiary = BlueTertiaryDark,
    onTertiary = BlueOnTertiaryDark,
    tertiaryContainer = BlueContainerDark,
    onTertiaryContainer = BlueOnTertiaryContainerDark,
    error = RedErrorDark,
    onError = RedOnErrorDark,
    errorContainer = RedErrorContainerDark,
    onErrorContainer = RedOnErrorContainerDark,
    background = CheckerDark,
    surface = CheckerDark,
)

/** The two tones of the checkerboard drawn behind transparent sticker art. */
data class CheckerColors(
    val background: Color,
    val square: Color,
)

val LocalCheckerColors = staticCompositionLocalOf {
    CheckerColors(background = CheckerLight, square = CheckerSquareLight)
}

/**
 * True when the active colour scheme came from the system wallpaper rather than [StickerportColors].
 *
 * Screens that show artwork (the preview grid, pack cards) use this to decide whether the checkerboard
 * is still needed: with dynamic colour the system background may already clash with a given
 * wallpaper, so a neutral checker stays on; with the fixed brand scheme it can be softened.
 */
val LocalUsesDynamicColor = staticCompositionLocalOf { false }

/** The two tones of the checkerboard for the currently active theme. */
object StickerportTheme {
    val checker: CheckerColors
        @Composable @ReadOnlyComposable get() = LocalCheckerColors.current

    val usesDynamicColor: Boolean
        @Composable @ReadOnlyComposable get() = LocalUsesDynamicColor.current
}

/**
 * Stickerport's Material 3 theme.
 *
 * @param dynamicColor use the wallpaper-derived palette on Android 12+ (Material You). Falls back
 *   to the brand palette on older releases, and whenever [dynamicColor] is false.
 * @param darkTheme defaults to the system setting; pass an explicit value from previews and tests
 *   so both schemes are renderable without changing device settings.
 */
@Composable
fun StickerportTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val supportsDynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val useDynamic = dynamicColor && supportsDynamic

    val colorScheme = when {
        useDynamic && darkTheme -> dynamicDarkColorScheme(LocalContext.current)
        useDynamic -> dynamicLightColorScheme(LocalContext.current)
        darkTheme -> DarkColors
        else -> LightColors
    }

    val checker = if (darkTheme) {
        CheckerColors(background = CheckerDark, square = CheckerSquareDark)
    } else {
        CheckerColors(background = CheckerLight, square = CheckerSquareLight)
    }

    CompositionLocalProvider(
        LocalCheckerColors provides checker,
        LocalUsesDynamicColor provides useDynamic,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = StickerportTypography,
            shapes = StickerportShapes,
            content = content,
        )
    }
}
