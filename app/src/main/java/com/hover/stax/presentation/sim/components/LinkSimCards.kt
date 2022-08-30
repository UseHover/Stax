package com.hover.stax.presentation.sim.components

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hover.stax.ui.theme.ColorSurface
import com.hover.stax.ui.theme.DarkGray
import com.hover.stax.ui.theme.OffWhite

@Composable
internal fun LinkSimCard(@StringRes id: Int, onClickedLinkSimCard: () -> Unit, stringArg: String = "") {
	OutlinedButton(
		onClick = onClickedLinkSimCard,
		modifier = Modifier
			.fillMaxWidth()
			.padding(vertical = 13.dp)
			.shadow(elevation = 0.dp),
		shape = MaterialTheme.shapes.medium,
		border = BorderStroke(width = 0.5.dp, color = DarkGray),
		colors = ButtonDefaults.buttonColors(
			backgroundColor = ColorSurface,
			contentColor = OffWhite
		)
	) {
		Text(
			text = stringResource(id = id, stringArg),
			style = MaterialTheme.typography.button,
			modifier = Modifier
				.fillMaxWidth()
				.padding(top = 5.dp, bottom = 5.dp),
			textAlign = TextAlign.Center
		)
	}
}