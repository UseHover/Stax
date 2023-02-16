package com.hover.stax.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.hover.stax.R
import com.hover.stax.presentation.add_accounts.StaxTopBarDefaults
import com.hover.stax.ui.theme.OffWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CenteredTopBar(title: String, onBack: (() -> Unit)?) {
	Column(modifier = Modifier.fillMaxWidth()) {
		CenterAlignedTopAppBar(
			title = { Text(text = title, style = MaterialTheme.typography.h1) },
			navigationIcon = {
				onBack?.let {
					IconButton(content = {
						Icon(
							painterResource(R.drawable.ic_close),
							contentDescription = "back",
							tint = OffWhite
						)
					},
						onClick = { onBack() })
				}
			},
			colors = StaxTopBarDefaults()
		)
	}
}