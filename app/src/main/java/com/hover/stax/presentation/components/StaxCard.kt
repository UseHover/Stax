package com.hover.stax.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hover.stax.ui.theme.*

@Composable
fun StaxCard(content: @Composable ColumnScope.() -> Unit) {

	Card(
		modifier = Modifier
			.fillMaxWidth()
			.padding(top = 6.dp, end = 6.dp, bottom = 13.dp, start = 6.dp),
		elevation = 3.dp,
		backgroundColor = mainBackground,
		border = BorderStroke(1.dp, Border)
		) {
			Column(
				content = content,
				modifier = Modifier
					.fillMaxWidth()
					.padding(top = 6.dp, end = 13.dp, bottom = 13.dp, start = 13.dp)
			)
		}
}