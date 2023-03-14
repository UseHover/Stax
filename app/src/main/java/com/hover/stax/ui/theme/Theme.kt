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
package com.hover.stax.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

private val DarkColorPalette = darkColors(
    primary = BrightBlue,
    onPrimary = OffWhite,
    primaryVariant = SecondaryBlue,
    secondary = OffWhite,
    onSecondary = SecondaryBlue,
    surface = Background,
    onSurface = OffWhite,
    background = Background,
    onBackground = OffWhite,
    error = StaxStateRed,
    onError = OffWhite
)

private val LightColorPalette = lightColors(
    primary = BrightBlue,
    onPrimary = OffWhite,
    primaryVariant = SecondaryBlue,
    secondary = BrightBlue,
    onSecondary = OffWhite,
    surface = OffWhite,
    onSurface = SecondaryBlue,
    background = OffWhite,
    onBackground = SecondaryBlue,
    error = StaxStateRed,
    onError = OffWhite
)

@Composable
fun StaxTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
//    val colors = if (darkTheme) {
//        DarkColorPalette
//    } else {
//        LightColorPalette
//    }

    MaterialTheme(
        colors = DarkColorPalette,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}