package com.hover.stax.presentation.add_accounts.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hover.stax.R
import com.hover.stax.presentation.components.PRIMARY
import com.hover.stax.presentation.components.SECONDARY
import com.hover.stax.presentation.components.StaxButtonColors
import com.hover.stax.ui.theme.BrightBlue
import com.hover.stax.ui.theme.OffWhite

@Composable
fun InputPinScreen(pin: MutableState<String>, doneText: Int, doneAction: () -> Unit) {
	Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
		Column(modifier = Modifier
			.fillMaxWidth().weight(1f, true), verticalArrangement = Arrangement.Bottom) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.Center
			) {
				pin.value.forEach { _ ->
					Box(
						modifier = Modifier
							.size(32.dp)
							.clip(CircleShape)
							.background(BrightBlue)
					)
					Spacer(modifier = Modifier.width(8.dp))
				}
			}
			Divider(thickness = 1.dp, color = OffWhite, modifier = Modifier
				.padding(horizontal = 89.dp)
				.padding(vertical = 13.dp))
		}
		Row(modifier = Modifier
			.fillMaxWidth()
			.weight(2f, false)) {
			PinPad(pin = pin, doneText, doneAction)
		}
	}
}

@Composable
fun PinPad(pin: MutableState<String>, doneText: Int, doneAction: () -> Unit) {
	Column() {
		Row() {
			PinButton("1", pin, Modifier.weight(1F))
			PinButton("2", pin,  Modifier.weight(1F))
			PinButton("3", pin,  Modifier.weight(1F))
		}
		Row() {
			PinButton("4", pin,  Modifier.weight(1F))
			PinButton("5", pin,  Modifier.weight(1F))
			PinButton("6", pin,  Modifier.weight(1F))
		}
		Row() {
			PinButton("7", pin,  Modifier.weight(1F))
			PinButton("8", pin,  Modifier.weight(1F))
			PinButton("9", pin,  Modifier.weight(1F))
		}
		Row() {
			Spacer(modifier = Modifier.weight(1F))
			PinButton("0", pin,  Modifier.weight(1F))
			BackspaceButton(pin, modifier = Modifier.weight(1F))
		}
		Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
			Button(
				onClick = { doneAction() },
				modifier = Modifier
					.wrapContentWidth()
					.padding(vertical = 6.dp),
				contentPadding = PaddingValues(vertical = 13.dp, horizontal = 55.dp),
				colors = StaxButtonColors(PRIMARY)
			) {
				Text(
					text = stringResource(id = doneText),
					style = MaterialTheme.typography.button,
					textAlign = TextAlign.Center,
					fontSize = 18.sp
				)
			}

//			PrimaryButton("Continue", modifier = Modifier.height(55.dp), onClick = {})
		}
	}
}

@Composable
fun PinButton(text: String, editingPin: MutableState<String>, modifier: Modifier) {
	PinPadButton(
		onClick = { addPinDigit(text, editingPin) },
		modifier = modifier.padding(8.dp)
	) {
		Text(
			text = text,
			style = MaterialTheme.typography.button,
			textAlign = TextAlign.Center,
			fontSize = 14.sp
		)
	}
}

@Composable
fun BackspaceButton(editingPin: MutableState<String>, modifier: Modifier) {
	PinPadButton(
		onClick = { removeLastDigit(editingPin) },
		modifier = modifier.padding(8.dp)
	) {
		Icon(painterResource(id = R.drawable.ic_backspace), contentDescription = "Backspace", )
	}
}

@Composable
fun PinPadButton(onClick: () -> Unit, modifier: Modifier, content: @Composable () -> Unit) {
	Button(
		onClick = onClick,
		modifier = modifier
			.padding(vertical = 6.dp)
			.height(55.dp),
		contentPadding = PaddingValues(13.dp),
		colors = StaxButtonColors(SECONDARY)
	) {
		content()
	}
}

fun addPinDigit(num: String, editingPin: MutableState<String>) {
	editingPin.value += num
}

fun removeLastDigit(editingPin: MutableState<String>) {
	editingPin.value = editingPin.value.dropLast(1)
}

@Preview
@Composable
fun InputPinScreenPreview() {
	val editingPin = remember { mutableStateOf("1234") }
	InputPinScreen(pin = editingPin, R.string.btn_continue, {})
}