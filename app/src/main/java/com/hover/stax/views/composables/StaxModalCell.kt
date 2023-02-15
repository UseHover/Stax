package com.hover.stax.views.composables

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hover.stax.ui.theme.AlphaDisabled
import com.hover.stax.ui.theme.AlphaHigh
import com.hover.stax.ui.theme.StaxTheme

@Composable
fun StaxModalCell(
    onClick: () -> Unit,
    text: String,
    color: Color = MaterialTheme.colors.surface,
    textColor: Color = MaterialTheme.colors.onSurface,
    enabled: Boolean = true,
    leftIcon: @Composable (() -> Unit)? = null,
    rightIcon: @Composable (() -> Unit)? = null
) {
    val colorAlpha = if (enabled) {
        AlphaHigh
    } else {
        AlphaDisabled
    }
    val onClickState = if (enabled) {
        onClick
    } else {
        {}
    }
    val indication = if (enabled) {
        LocalIndication.current
    } else {
        null
    }

    val shape = MaterialTheme.shapes.medium
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(56.dp)
            .clip(shape)
            .clickable(
                onClick = onClickState,
                indication = indication,
                interactionSource = remember { MutableInteractionSource() }
            ),
        shape = shape,
        color = color.copy(alpha = colorAlpha),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leftIcon != null) {
                Spacer(Modifier.width(8.dp))
                leftIcon()
                Spacer(Modifier.width(16.dp))
            } else {
                Spacer(Modifier.width(20.dp))
            }

            StaxContentTitle(
                text = text,
                color = textColor.copy(alpha = colorAlpha)
            )

            if (rightIcon != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    rightIcon()
                    Spacer(Modifier.size(20.dp))
                }
            }
        }
    }
}

@Preview
@Composable
fun StaxModalCell() {
    StaxTheme {
        StaxModalCell(onClick = {}, text = "Cell")
    }
}