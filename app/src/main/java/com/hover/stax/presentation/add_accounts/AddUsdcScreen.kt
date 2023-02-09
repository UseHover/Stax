package com.hover.stax.presentation.add_accounts

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.navigation.NavController
import com.hover.stax.R
import com.hover.stax.addChannels.UsdcViewModel
import com.hover.stax.presentation.components.PRIMARY
import com.hover.stax.presentation.components.PrimaryButton
import com.hover.stax.presentation.components.SECONDARY
import com.hover.stax.presentation.components.StaxButtonColors
import com.hover.stax.ui.theme.BrightBlue
import com.hover.stax.ui.theme.OffWhite
import org.koin.androidx.compose.getViewModel

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun AddUsdcScreen(viewModel: UsdcViewModel = getViewModel(), navController: NavController) {

	val editingPin = remember { mutableStateOf("") }

//	viewModel.createAccount()

	Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
		Scaffold(
			topBar = { TopBar(navController) },
		) {
			InputPinScreen(editingPin)
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(navController: NavController) {
	Column(modifier = Modifier.fillMaxWidth()) {
		CenterAlignedTopAppBar(
			navigationIcon = {
				IconButton(content = { Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = "back", tint = OffWhite) },
					onClick = { navController.popBackStack() })
			},
			title = { Text(text = stringResource(R.string.create_pin), fontSize = 18.sp) },
			colors = StaxTopBarDefaults()
		)
	}
}

@Composable
fun InputPinScreen(editingPin: MutableState<String>) {
	Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
		Column(modifier = Modifier
			.fillMaxWidth()
			.weight(1f, true), verticalArrangement = Arrangement.Center) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.Center
			) {
				editingPin.value.forEach { _ ->
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
				.padding(horizontal = 55.dp)
				.padding(vertical = 13.dp))
		}
		Row(modifier = Modifier
			.fillMaxWidth()
			.weight(1f, false)) {
			PinPad(editingPin = editingPin)
		}
	}
}

@Composable
fun PinPad(editingPin: MutableState<String>) {
	Column() {
		Row() {
			PinButton("1", editingPin, Modifier.weight(1F))
			PinButton("2", editingPin,  Modifier.weight(1F))
			PinButton("3", editingPin,  Modifier.weight(1F))
		}
		Row() {
			PinButton("4", editingPin,  Modifier.weight(1F))
			PinButton("5", editingPin,  Modifier.weight(1F))
			PinButton("6", editingPin,  Modifier.weight(1F))
		}
		Row() {
			PinButton("7", editingPin,  Modifier.weight(1F))
			PinButton("8", editingPin,  Modifier.weight(1F))
			PinButton("9", editingPin,  Modifier.weight(1F))
		}
		Row() {
			Spacer(modifier = Modifier.weight(1F))
			PinButton("0", editingPin,  Modifier.weight(1F))
			BackspaceButton(editingPin, modifier = Modifier.weight(1F))
		}
		Row(modifier = Modifier.fillMaxWidth().padding(top = 55.dp), horizontalArrangement = Arrangement.Center) {
			Button(
				onClick = { },
				modifier = Modifier.wrapContentWidth().padding(vertical = 6.dp),
				contentPadding = PaddingValues(vertical = 13.dp, horizontal = 55.dp),
				colors = StaxButtonColors(PRIMARY)
			) {
				Text(
					text = "Continue",
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
		modifier = modifier
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
		modifier = modifier
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
	InputPinScreen(editingPin = editingPin)
}