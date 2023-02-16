package com.hover.stax.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.hover.stax.R
import com.hover.stax.ui.theme.OffWhite

@Composable
fun TallTopBar(title: String, onBack: (() -> Unit)?) {
	TopAppBar(
		modifier = Modifier.fillMaxWidth().height(100.dp),
		elevation = 0.dp,
		title = { Text(text = title, style = MaterialTheme.typography.h1) },
		navigationIcon = {
			onBack?.let {
				IconButton(content = {
					Icon(
						painterResource(R.drawable.ic_close),
						contentDescription = "back",
						tint = OffWhite
					)
				}, onClick = { onBack() })
			}
		},
		backgroundColor = Color.Transparent
	)
}