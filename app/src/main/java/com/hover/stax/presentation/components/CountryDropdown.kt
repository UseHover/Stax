package com.hover.stax.presentation.components

import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import com.hover.stax.R
import com.hover.stax.countries.CountryAdapter
import com.yariksoffice.lingver.Lingver
import java.util.*


@Composable
fun CountryDropdown(selected: String, options: List<String>, onSelect: (String) -> Unit) {
	StaxDropdown(selectedOption = getCountryString(selected), options = options, content = { country -> CountryItem(country) }, onSelect = onSelect)
}

@Composable
fun CountryItem(countryCode: String = "00") {
	Text(text = getCountryString(countryCode))
}

@Composable
fun getCountryString(code: String?): String = if (code.isNullOrEmpty() || code == CountryAdapter.CODE_ALL_COUNTRIES)
	stringResource(R.string.all_countries_with_emoji)
else
	stringResource(R.string.country_with_emoji, countryCodeToEmoji(code), getFullCountryName(code))

@Composable
private fun getFullCountryName(code: String): String {
	if (code.isNullOrEmpty() || code == CountryAdapter.CODE_ALL_COUNTRIES) {
		return stringResource(R.string.all_countries_with_emoji)
	}
	val locale = Locale(Lingver.getInstance().getLanguage(), code)
	return locale.displayCountry
}

private fun countryCodeToEmoji(code: String): String {
	if (code.isNullOrEmpty() || code == CountryAdapter.CODE_ALL_COUNTRIES) { return "" }
	val firstLetter = Character.codePointAt(code.uppercase(), 0) - 0x41 + 0x1F1E6
	val secondLetter = Character.codePointAt(code.uppercase(), 1) - 0x41 + 0x1F1E6
	return String(Character.toChars(firstLetter)) + String(Character.toChars(secondLetter))
}