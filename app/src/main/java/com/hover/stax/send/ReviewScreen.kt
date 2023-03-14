package com.hover.stax.send

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hover.stax.ui.theme.StaxTheme
import com.hover.stax.views.composables.StaxHeader
import com.hover.stax.views.composables.StaxLayout
import com.hover.stax.views.composables.StaxPrimaryButton

@Composable
fun ReviewScreen(
    onClickBack: () -> Unit,
) {

    StaxLayout(
        modifier = Modifier.padding(16.dp),
        title = {
            StaxHeader(
                text = "Review",
                onClickBack = onClickBack
            )
        },
        content = {
        },
        footer = {
            PaymentScreenFooter()
        }
    )
}

@Composable
fun PaymentScreenFooter() {
    Column {
        Text(text = "Make sure you have the correct details before sending ")
        StaxPrimaryButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {},
            isEnabled = true,
            title = "Send money"
        )
    }
}

@Preview
@Composable
fun ReviewScreenPreview() {
    StaxTheme(darkTheme = true) {
        ReviewScreen(
            onClickBack = { },
        )
    }
}