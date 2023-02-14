package com.hover.stax.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import com.hover.stax.R
import com.hover.stax.ui.theme.Background

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun StaxDropdown(selectedOption: String, options: List<String>, content: @Composable (String) -> Unit, onSelect: (String) -> Unit) {
	var expand by remember { mutableStateOf(false) }

	ExposedDropdownMenuBox(
		expanded = expand,
		onExpandedChange = { expand = it },
		modifier = Modifier.fillMaxWidth(0.4f)
	) {
		OutlinedTextField(
			value = TextFieldValue(selectedOption),
			onValueChange = { },
			singleLine = true,
			readOnly = true,
			trailingIcon = {
				ExposedDropdownMenuDefaults.TrailingIcon(expanded = expand)
			},
			colors = staxDropdownDefaults(),
			modifier = Modifier.fillMaxWidth()
		)
		DropdownMenu(expanded = expand,
			modifier = Modifier.exposedDropdownSize(),
			onDismissRequest = { expand = false }) {
			options.forEach { selectionOption ->
				DropdownMenuItem(
					onClick = {
						expand = false
						onSelect(selectionOption)
					}
				) {
					content(selectionOption)
				}
			}
		}
	}
}

@Preview
@Composable
fun StaxDropdwonPreview() {
	val list = listOf("ke", "et", "tz", "ng")
	StaxDropdown(list[0], list, { item -> Text(item) }) { }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun staxDropdownDefaults() = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
	textColor = colorResource(id = R.color.offWhite),
	focusedLabelColor = colorResource(id = R.color.offWhite),
	focusedTrailingIconColor = colorResource(id = R.color.offWhite),
	backgroundColor = Background,
	cursorColor = colorResource(id = R.color.offWhite),
	focusedBorderColor = colorResource(id = R.color.offWhite),
	unfocusedBorderColor = colorResource(id = R.color.buttonColor),)