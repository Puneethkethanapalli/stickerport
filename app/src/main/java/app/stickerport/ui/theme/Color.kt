package app.stickerport.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Stickerport's fallback brand palette.
 *
 * These are only used when dynamic colour is unavailable — that is, on Android 11 (API 30) and
 * below, and whenever the user turns dynamic colour off. On Android 12+ the system derives the
 * scheme from the user's wallpaper instead, so this palette mostly exists to keep previews and
 * older devices on-brand.
 *
 * The seed is the teal-green used by the launcher icon (`ic_launcher_background`).
 */

// ---- Light ----

internal val GreenPrimaryLight = Color(0xFF006B58)
internal val GreenOnPrimaryLight = Color(0xFFFFFFFF)
internal val GreenContainerLight = Color(0xFF7FF8DC)
internal val GreenOnContainerLight = Color(0xFF00201A)

internal val TealSecondaryLight = Color(0xFF4A635C)
internal val TealOnSecondaryLight = Color(0xFFFFFFFF)
internal val TealContainerLight = Color(0xFFCCE8DF)
internal val TealOnContainerLight = Color(0xFF06201A)

internal val BlueTertiaryLight = Color(0xFF416277)
internal val BlueOnTertiaryLight = Color(0xFFFFFFFF)
internal val BlueContainerLight = Color(0xFFC5E7FF)
internal val BlueOnTertiaryContainerLight = Color(0xFF001E2C)

internal val RedErrorLight = Color(0xFFBA1A1A)
internal val RedOnErrorLight = Color(0xFFFFFFFF)
internal val RedErrorContainerLight = Color(0xFFFFDAD6)
internal val RedOnErrorContainerLight = Color(0xFF410002)

// ---- Dark ----

internal val GreenPrimaryDark = Color(0xFF5CDBC1)
internal val GreenOnPrimaryDark = Color(0xFF00382E)
internal val GreenContainerDark = Color(0xFF005143)
internal val GreenOnContainerDark = Color(0xFF7FF8DC)

internal val TealSecondaryDark = Color(0xFFB1CCC3)
internal val TealOnSecondaryDark = Color(0xFF1C352E)
internal val TealContainerDark = Color(0xFF334B44)
internal val TealOnContainerDark = Color(0xFFCCE8DF)

internal val BlueTertiaryDark = Color(0xFFA8CBE3)
internal val BlueOnTertiaryDark = Color(0xFF0B3447)
internal val BlueContainerDark = Color(0xFF284B5E)
internal val BlueOnTertiaryContainerDark = Color(0xFFC5E7FF)

internal val RedErrorDark = Color(0xFFFFB4AB)
internal val RedOnErrorDark = Color(0xFF690005)
internal val RedErrorContainerDark = Color(0xFF93000A)
internal val RedOnErrorContainerDark = Color(0xFFFFDAD6)

/**
 * The brand background colour. Deliberately a fixed neutral-ish tint rather than
 * `surface`/`background`: sticker art is transparent and sits on this, so a stable, low-chroma
 * backdrop keeps the artwork readable and colour-accurate across light and dark.
 */
internal val CheckerLight = Color(0xFFF2F4F3)
internal val CheckerDark = Color(0xFF101413)

/**
 * The two tones of the checkerboard drawn behind transparent sticker art (spec §12.7: "a subtle
 * checkerboard behind transparent art"). Kept close in luminance so the pattern reads as a
 * texture rather than as part of the artwork.
 */
internal val CheckerSquareLight = Color(0xFFE2E5E4)
internal val CheckerSquareDark = Color(0xFF191D1C)
