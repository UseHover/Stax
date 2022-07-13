package com.hover.stax.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

private val DarkColorPalette = darkColors(
    primary = ColorPrimary,
    onPrimary = OffWhite,
    primaryVariant = ColorPrimaryDark,
    secondary = BrightBlue,
    onSecondary= CardViewColor,
    surface = CardViewColor,
    onSurface = OffWhite,
    background = mainBackground,
    onBackground = OffWhite,
    error = StaxStateRed,
    onError = OffWhite
)

private val LightColorPalette = lightColors(
    primary = ColorPrimary,
    onPrimary = OffWhite,
    primaryVariant = ColorPrimaryDark,
    secondary = BrightBlue,
    onSecondary= CardViewColor,
    surface = CardViewColor,
    onSurface = OffWhite,
    background = mainBackground,
    onBackground = OffWhite,
    error = StaxStateRed,
    onError = OffWhite
)

@Composable
fun StaxTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}