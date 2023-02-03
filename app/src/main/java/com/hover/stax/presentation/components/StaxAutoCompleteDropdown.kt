package com.hover.stax.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import com.hover.stax.R

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun StaxAutoCompleteDropdown(label: Int? = null, value: String = "", options: List<String>, startIcon: Int? = null, content: @Composable (String) -> Unit, onChange: (String) -> Unit, onSelect: (String) -> Unit) {
	var expand by remember { mutableStateOf(false) }

	ExposedDropdownMenuBox(
		expanded = expand,
		onExpandedChange = { expand = value.isNotEmpty() || it }
	) {
		OutlinedTextField(
			value = value,
			label = { label?.let { Text(stringResource(id = label)) } },
			onValueChange = {
				expand = it.isNotEmpty()
				onChange(it)
			},
			singleLine = true,
			leadingIcon = {
				startIcon?.let { Icon(painterResource(startIcon), "", tint = colorResource(R.color.white)) }
			},
			colors = staxDropdownDefaults(),
			modifier = Modifier.fillMaxWidth()
		)
		ExposedDropdownMenu(expanded = expand,
			modifier = Modifier.exposedDropdownSize(),
			onDismissRequest = { expand = false }) {
			val filterOpts = options.filter { it.contains(value, ignoreCase = true) }
			filterOpts.forEach { channelName ->
				DropdownMenuItem(
					onClick = {
						expand = false
						onSelect(channelName)
					}
				) {
					content(channelName)
				}
			}
		}
	}
}

//@Preview
//@Composable
//fun StaxAutoCompleteDropdownPreview() {
//	val list = listOf("ke", "et", "tz", "ng")
//	StaxAutoCompleteDropdown(R.string.search, list[0], list, null, { }, { item -> Text(item) }) { }
//}