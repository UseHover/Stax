package com.hover.stax.presentation.bounties.components

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.hover.stax.R
import com.hover.stax.countries.CountryAdapter
import com.hover.stax.presentation.bounties.BountyViewModel
import com.hover.stax.presentation.bounties.CODE_ALL_COUNTRIES
import com.yariksoffice.lingver.Lingver
import java.util.*

@Composable
fun CountryDropdown(countries: List<String>, country: String, isLoading: Boolean, bountyViewModel: BountyViewModel) {
    var expanded by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(country) }
    var textFieldSize by remember { mutableStateOf(Size.Zero) }
    val interactionSource = remember { MutableInteractionSource() }

    val icon = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown
    val borderColor = if (isLoading) colorResource(id = R.color.stax_state_blue) else Color.White

    val context = LocalContext.current

    if (interactionSource.collectIsPressedAsState().value)
        expanded = !expanded

    Column(
        Modifier
            .padding(10.dp)
    ) {
        OutlinedTextField(
            value = getCountryString(selected, context),
            onValueChange = { selected = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = dimensionResource(id = R.dimen.margin_10))
                .onGloballyPositioned { coordinates ->
                    textFieldSize = coordinates.size.toSize()
                },
            label = {
                Text(stringResource(id = R.string.select_country), style = MaterialTheme.typography.body1)
            },
            trailingIcon = {
                Icon(
                    icon,
                    "Dropdown",
                    Modifier.clickable { expanded = !expanded },
                    tint = if (isLoading) colorResource(id = R.color.stax_state_blue) else Color.White
                )
            },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedLabelColor = borderColor,
                unfocusedBorderColor = borderColor,
                focusedBorderColor = borderColor,
                unfocusedLabelColor = borderColor
            ),
            readOnly = true,
            interactionSource = interactionSource
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(with(LocalDensity.current) { textFieldSize.width.toDp() })
        ) {
            countries.forEach { countryCode ->
                DropdownMenuItem(onClick = {
                    selected = countryCode
                    expanded = false
                    bountyViewModel.loadBounties(countryCode)
                }) {
                    Text(
                        modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.margin_10)),
                        text = getCountryString(countryCode, context),
                        style = MaterialTheme.typography.button
                    )
                }
            }
        }

        if (isLoading)
            Text(
                modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.margin_10)),
                text = stringResource(id = R.string.filtering_in_progress),
                style = MaterialTheme.typography.body2,
                color = colorResource(id = R.color.stax_state_blue)
            )
    }
}

fun getCountryString(code: String, context: Context): String = if (code.isEmpty() || code == CountryAdapter.CODE_ALL_COUNTRIES)
    context.getString(R.string.all_countries_with_emoji)
else
    context.getString(R.string.country_with_emoji, countryCodeToEmoji(code), getFullCountryName(code))

private fun getFullCountryName(code: String): String {
    val locale = Locale(Lingver.getInstance().getLanguage(), code)
    return locale.displayCountry
}

private fun countryCodeToEmoji(countryCode: String): String {
    return try {
        val firstLetter = Character.codePointAt(countryCode.uppercase(), 0) - 0x41 + 0x1F1E6
        val secondLetter = Character.codePointAt(countryCode.uppercase(), 1) - 0x41 + 0x1F1E6
        String(Character.toChars(firstLetter)) + String(Character.toChars(secondLetter))
    } catch (e: Exception) {
        ""
    }
}

@Preview
@Composable
fun CountryDropdownPreview() {
    val countryCodes = listOf("KE, UG, TZ, ET, ZA")
    var expanded by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(CODE_ALL_COUNTRIES) }
    var textFieldSize by remember { mutableStateOf(Size.Zero) }

    val icon = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown

    Column(Modifier.padding(10.dp)) {
        OutlinedTextField(
            value = selected,
            onValueChange = { selected = it },
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    textFieldSize = coordinates.size.toSize()
                },
            label = {
                Text(
                    text = stringResource(id = R.string.select_country),
                    style = MaterialTheme.typography.body2,
                )
            },
            trailingIcon = {
                Icon(
                    icon,
                    "contentDescription",
                    Modifier.clickable { expanded = !expanded },
                    tint = Color.White
                )
            },
            readOnly = true,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedLabelColor = colorResource(id = R.color.stax_state_blue),
                unfocusedBorderColor = Color.White,
                focusedBorderColor = colorResource(id = R.color.stax_state_blue),
                unfocusedLabelColor = Color.White
            )
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(with(LocalDensity.current) { textFieldSize.width.toDp() })
        ) {
            countryCodes.forEach { countryCode ->
                DropdownMenuItem(onClick = {
                    selected = countryCode
                    expanded = false
                }) {
                    Text(text = getCountryString(countryCode, LocalContext.current))
                }
            }
        }

        Text(
            modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.margin_10)),
            text = stringResource(id = R.string.filtering_in_progress),
            style = MaterialTheme.typography.body2,
            color = colorResource(id = R.color.stax_state_blue)
        )
    }
}