package com.hover.stax.views.composables

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun StaxContentTitle(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.onBackground,
    fontWeight: FontWeight = FontWeight.Normal,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    Text(
        style = MaterialTheme.typography.subtitle2.copy(color = color, fontWeight = fontWeight),
        text = text,
        modifier = modifier,
        maxLines = maxLines,
        overflow = overflow,
    )
}