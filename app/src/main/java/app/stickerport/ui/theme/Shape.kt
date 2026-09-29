package app.stickerport.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * App shapes (Material 3).
 *
 * Slightly rounder than the M3 defaults, because the app shows a lot of cards and sticker tiles
 * and the softer corners suit the subject matter. Kept small and explicit so later tasks have one
 * place to tune.
 */
internal val StickerportShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp),
)
