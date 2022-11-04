package com.hover.stax.presentation.welcome.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
fun WelcomeHeader(title: String, desc: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.h1,
            modifier = Modifier.testTag("welcome_header_title")
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = desc,
            style = MaterialTheme.typography.body1,
            modifier = Modifier.testTag("welcome_header_description")
        )
    }
}