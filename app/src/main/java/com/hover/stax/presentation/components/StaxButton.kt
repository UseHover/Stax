package com.hover.stax.presentation.components

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hover.stax.ui.theme.ColorSurface
import com.hover.stax.ui.theme.DarkGray
import com.hover.stax.ui.theme.OffWhite

@Composable
fun StaxButton(text: String, icon: Int?, onClick: () -> Unit) {
	OutlinedButton(
		onClick = { onClick() },
		modifier = Modifier
			.padding(bottom = 6.dp, top = 13.dp)
			.shadow(elevation = 0.dp)
			.wrapContentWidth(),
		shape = MaterialTheme.shapes.medium,
		border = BorderStroke(width = 0.5.dp, color = DarkGray),
		colors = ButtonDefaults.buttonColors(backgroundColor = ColorSurface, contentColor = OffWhite)
	) {
		Row(
			modifier = Modifier.wrapContentWidth().padding(all = 5.dp)
		) {
			if (icon != null) {
				Image(
					painter = painterResource(id = icon),
					contentDescription = null,
					modifier = Modifier.size(18.dp)
				)
				Spacer(modifier = Modifier.width(3.dp))
			}

			Text(
				text = text,
				style = MaterialTheme.typography.button,
				modifier = Modifier.padding(end = 5.dp),
				textAlign = TextAlign.Start,
				fontSize = 14.sp
			)
		}
	}
}