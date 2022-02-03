package com.hover.stax.ui.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.hover.stax.R

val EffraFont = FontFamily(
    Font(R.font.effra_regular, weight = FontWeight.Normal),
    Font(R.font.effra_medium, weight = FontWeight.Medium),
    Font(R.font.effra_heavy, weight = FontWeight.Bold)
)

val Typography = Typography(
    body1 = TextStyle(
        fontFamily = EffraFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    body2 = TextStyle(
        fontFamily = EffraFont,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp
    ),
    h1 = TextStyle(
        fontFamily = EffraFont,
        fontWeight = FontWeight.Medium,
        fontSize = 28.sp
    ),
    h2 = TextStyle(
        fontFamily = EffraFont,
        fontWeight = FontWeight.Medium,
        fontSize = 21.sp
    ),
    button = TextStyle(
        fontFamily = EffraFont,
        fontWeight = FontWeight.Medium,
        fontSize = 17.sp
    )
)