package com.hover.stax.presentation.welcome.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hover.stax.ui.theme.BrightBlue
import com.hover.stax.ui.theme.ColorPrimaryDark

@Composable
fun ContinueButton(text: String, modifier: Modifier = Modifier, onClick: (() -> Unit)) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = BrightBlue,
            contentColor = ColorPrimaryDark
        )
    ) {
        Text(
            modifier = modifier.padding(top = 5.dp, bottom = 5.dp),
            text = text,
            style = MaterialTheme.typography.button
        )
    }
}