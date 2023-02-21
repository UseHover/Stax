package com.hover.stax.send

import androidx.compose.runtime.Composable
import com.hover.stax.views.composables.StaxHeader
import com.hover.stax.views.composables.StaxLayout

@Composable
fun ReviewScreen(
    onClickBack: () -> Unit,
) {

    StaxLayout(
        title = {
            StaxHeader(
                text = "Review",
                onClickBack = onClickBack
            )
        },
        content = {

        },
        footer = {
            // add button here
        }
    )
}