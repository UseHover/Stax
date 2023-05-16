/*
 * Copyright 2022 Stax
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hover.stax.views.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import com.hover.stax.views.theme.CardViewColor
import com.hover.stax.views.theme.ColorPrimary
import com.hover.stax.views.theme.ColorPrimaryDark
import com.hover.stax.views.theme.OffWhite
import com.hover.stax.views.theme.StaxStateRed
import com.hover.stax.views.theme.mainBackground

private val DarkColorPalette = darkColors(
    primary = ColorPrimary,
    onPrimary = OffWhite,
    primaryVariant = ColorPrimaryDark,
    secondary = BrightBlue,
    onSecondary = CardViewColor,
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
    onSecondary = CardViewColor,
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