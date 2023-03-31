package com.hover.stax.presentation.components

import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import com.hover.stax.R

@Composable
fun StaxTextField(textField: TextFieldValue, placeholder: Int, startIcon: Int? = null, onChange: (TextFieldValue) -> Unit) {
    if (startIcon != null) {
        OutlinedTextField(
            value = textField,
            onValueChange = { onChange(it) },
            label = {
                Text(text = stringResource(placeholder), style = MaterialTheme.typography.body1)
            },
            singleLine = true,
            colors = StaxTextFieldDefaults(),
            leadingIcon = {
                Icon(painterResource(startIcon), "", tint = colorResource(R.color.white))
            }
        )
    } else {
        OutlinedTextField(
            value = textField,
            onValueChange = { onChange(it) },
            label = {
                Text(text = stringResource(placeholder), style = MaterialTheme.typography.body1)
            },
            singleLine = true,
            colors = StaxTextFieldDefaults()
        )
    }
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

@Preview
@Composable
fun EmptyNoIconStaxTextFieldPreview() {
    StaxTextField(TextFieldValue(""), R.string.search) { }
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
    cursorColor = colorResource(id = R.color.offWhite)
)