package com.hover.stax.views.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun StaxHeader(
    text: String,
    onClickBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        Box(
            modifier = Modifier
                .padding(start = 16.dp)
                .weight(0.2F)
        ) {
            StaxBackButton(
                onClick = onClickBack
            )
        }

        StaxTitle(
            text = text
        )

        Spacer(
            Modifier
                .size(0.dp)
                .weight(0.2F)
        )
    }
}

@Composable
fun StaxBackButton(
    onClick: () -> Unit,
    imageVector: ImageVector = Icons.Rounded.ChevronLeft
) {
    StaxIconButton(
        onClick = onClick,
        modifier = Modifier.size(28.dp)
    ) {
        StaxIcon(
            imageVector = imageVector,
        )
    }
}

@Composable
fun StaxIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled
    ) {
        content()
    }
}