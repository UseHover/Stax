package com.hover.stax.views.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.hover.stax.ui.theme.StaxTheme

@Composable
fun StaxContentText(
    text: String?,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
    fontWeight: FontWeight = FontWeight.Normal,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    AnimatedVisibility(visible = !text.isNullOrEmpty()) {
        Text(
            fontSize = fontSize,
            style = MaterialTheme.typography.subtitle2.copy(fontWeight = fontWeight),
            text = text.toString(), // TODO - this is a hack to get around showing/hiding views using AnimatedVisibility
            modifier = modifier,
            maxLines = maxLines,
            overflow = overflow,
        )
    }
}

@Composable
fun StaxTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.subtitle1.copy(textAlign = TextAlign.Center),
        )
    }
}

@Composable
fun StaxPrimaryButtonText(text: String, textAllCaps: Boolean = false) {
    Text(
        text = if (textAllCaps) text.uppercase() else text,
        style = MaterialTheme.typography.button.copy(color = MaterialTheme.colors.onPrimary),
        textAlign = TextAlign.Center
    )
}

@Preview
@Composable
fun StaxContentTextPreview() {
    StaxTheme {
        StaxContentText(text = "This is a test", fontSize = 16.sp)
    }
}

@Preview
@Composable
fun StaxContentEmptyTextPreview() {
    StaxTheme {
        StaxContentText(text = null, fontSize = 16.sp)
    }
}