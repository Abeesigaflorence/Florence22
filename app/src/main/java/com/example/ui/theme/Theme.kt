package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val RoyoColorScheme = lightColorScheme(
    primary = GoldenYellow,
    onPrimary = DeepEspresso,
    primaryContainer = SurfaceButter,
    onPrimaryContainer = DeepEspresso,
    secondary = WarmAmber,
    onSecondary = SurfaceWhite,
    secondaryContainer = SurfaceButter,
    onSecondaryContainer = DeepEspresso,
    tertiary = SuccessGreen,
    onTertiary = SurfaceWhite,
    background = WarmCream,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceButter,
    onSurfaceVariant = MutedText,
    outline = CardBorder,
    outlineVariant = CardBorder,
    error = BrickRed,
    onError = SurfaceWhite
)

@Composable
fun RoyoTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = RoyoColorScheme,
        typography = Typography,
        content = content
    )
}
