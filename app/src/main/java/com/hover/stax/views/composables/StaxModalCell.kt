package com.hover.stax.views.composables

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hover.stax.R
import com.hover.stax.ui.theme.StaxTheme

@Composable
fun StaxModalCell(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    header: String,
    subHeader: String,
    footer: String?,
    enabled: Boolean = true,
    leftIcon: @Composable (() -> Unit)? = null,
    rightIcon: @Composable (() -> Unit)? = null
) {
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
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(shape)
            .clickable(
                onClick = onClickState,
                indication = indication,
                interactionSource = remember { MutableInteractionSource() }
            ),
        shape = shape,
    ) {
        Column {
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

                Column(modifier = Modifier.fillMaxWidth()) {
                    StaxContentText(
                        text = header,
                        fontSize = 14.sp
                    )
                    StaxContentText(
                        text = subHeader,
                        fontSize = 12.sp
                    )
                    StaxContentText(
                        text = footer,
                        fontSize = 12.sp
                    )
                }

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
            Divider(
                color = colorResource(id = R.color.nav_grey),
                modifier = Modifier.padding(horizontal = 13.dp)
            )
        }
    }
}

@Preview
@Composable
fun StaxModalCellPreview() {
    StaxTheme {
        StaxModalCell(
            modifier = Modifier.heightIn(min = 70.dp),
            onClick = { },
            header = "This is a test",
            subHeader = "This is a test",
            footer = "This is a test",
            rightIcon = { StaxImage(imageUrl = "https://example.com/image.png") }
        )
    }
}