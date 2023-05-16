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

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.hover.stax.ui.R

val Brutalista = FontFamily(
    Font(R.font.brutalista_regular, weight = FontWeight.Normal),
    Font(R.font.brutalista_medium, weight = FontWeight.Medium),
    Font(R.font.brutalista_bold, weight = FontWeight.Bold)
)

val Typography = Typography(
    body1 = TextStyle(
        fontFamily = Brutalista,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    body2 = TextStyle(
        fontFamily = Brutalista,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp
    ),
    subtitle2 = TextStyle(
        fontFamily = Brutalista,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),
    h1 = TextStyle(
        fontFamily = Brutalista,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp
    ),
    h2 = TextStyle(
        fontFamily = Brutalista,
        fontWeight = FontWeight.Medium,
        fontSize = 21.sp
    ),
    h3 = TextStyle(
        fontFamily = Brutalista,
        fontWeight = FontWeight.Medium,
        fontSize = 19.sp
    ),
    h4 = TextStyle(
        fontFamily = Brutalista,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp
    ),
    button = TextStyle(
        fontFamily = Brutalista,
        fontWeight = FontWeight.Medium,
        fontSize = 17.sp
    ),
    caption = TextStyle(
        fontFamily = Brutalista,
        fontWeight = FontWeight.Normal
    )
)