package com.hover.stax.presentation.components

import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import com.hover.stax.R

@Composable
fun StaxTextField(text: TextFieldValue, placeholder: Int, startIcon: Int?, onChange: (TextFieldValue) -> Unit) {
	OutlinedTextField(
		value = text,
		onValueChange = onChange,
		label = { Text(text = stringResource(placeholder)) },
		singleLine = true,
		leadingIcon = {
			startIcon?.let { Icon(painterResource(startIcon), "", tint = colorResource(R.color.white)) }
		},
		colors = StaxTextFieldDefaults()
	)
}

@Preview
@Composable
fun EmptyStaxTextFieldPreview() {
	StaxTextField(TextFieldValue(""), R.string.search, R.drawable.ic_search) { }
}

@Preview
@Composable
fun FilledStaxTextFieldPreview() {
	StaxTextField(TextFieldValue("Test value"), R.string.search, R.drawable.ic_search) { }
}

@Composable
fun StaxTextDefaults() = TextStyle(color = colorResource(id = R.color.offWhite))

@Composable
fun StaxTextFieldDefaults() = TextFieldDefaults.outlinedTextFieldColors(
	textColor = colorResource(id = R.color.offWhite),
	focusedBorderColor = colorResource(id = R.color.offWhite),
	unfocusedBorderColor = colorResource(id = R.color.buttonColor),
	focusedLabelColor = colorResource(id = R.color.offWhite),
	unfocusedLabelColor = colorResource(id = R.color.offWhite),
	cursorColor = colorResource(id = R.color.offWhite))