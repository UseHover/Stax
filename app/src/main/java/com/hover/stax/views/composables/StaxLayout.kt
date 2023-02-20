package com.hover.stax.views.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StaxLayout(
    title: @Composable () -> Unit,
    footer: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: LazyListScope.() -> Unit
) {
    StaxLazyColumn(modifier) {
        item {
            Spacer(Modifier.height(24.dp))
            title()
            Spacer(Modifier.height(24.dp))
        }

        content()

        item {
            Spacer(Modifier.height(8.dp))
        }

        item {
            footer()
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun StaxLazyColumn(
    modifier: Modifier = Modifier,
    content: LazyListScope.() -> Unit
) {
    Box(
        modifier = Modifier
    ) {
        LazyColumn(
            modifier = modifier
                .navigationBarsPadding()
                .imePadding(),
            content = content
        )
    }
}