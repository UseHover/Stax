package com.hover.stax.ui.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.hover.stax.R

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