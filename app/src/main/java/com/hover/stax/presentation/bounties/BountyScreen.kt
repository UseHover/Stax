package com.hover.stax.presentation.bounties

import android.content.Context
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import com.hover.stax.R
import com.hover.stax.countries.CountryAdapter
import com.hover.stax.domain.model.Bounty
import com.hover.stax.domain.model.ChannelBounties
import com.hover.stax.presentation.home.HorizontalImageTextView
import com.hover.stax.ui.theme.Brutalista
import com.hover.stax.ui.theme.StaxTheme
import com.yariksoffice.lingver.Lingver
import java.util.*

const val CODE_ALL_COUNTRIES = "00"

@Composable
fun BountyList(bountyViewModel: BountyViewModel, selectListener: SelectListener) {
    val bountiesState by bountyViewModel.bountiesState.collectAsState()

    StaxTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize(), color = MaterialTheme.colors.background
        ) {
            LazyColumn {
                item {
                    CountryDropdown(bountyViewModel, bountiesState.loading)
                }

                items(bountiesState.bounties) {
                    ChannelBountyCard(channelBounty = it, selectListener)
                }

                if (bountiesState.bounties.isEmpty()) {
                    item {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(dimensionResource(id = R.dimen.margin_13)),
                            text = stringResource(id = R.string.bounty_error_none),
                            style = MaterialTheme.typography.body2,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChannelBountyCard(channelBounty: ChannelBounties, selectListener: SelectListener) {
    if (channelBounty.bounties.isNotEmpty())
        Column {
            Text(
                text = channelBounty.channel.ussdName.uppercase(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(id = R.dimen.margin_13)),
                style = MaterialTheme.typography.body1,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End
            )

            channelBounty.bounties.forEach {
                BountyCard(bounty = it, selectListener)
            }
        }
}

@Composable
fun BountyCard(bounty: Bounty, selectListener: SelectListener) {
    val context = LocalContext.current
    val margin8 = dimensionResource(id = R.dimen.margin_8)
    val margin13 = dimensionResource(id = R.dimen.margin_13)

    val bountyState = getBountyState(bounty, selectListener)

    val strikeThrough = TextStyle(
        fontFamily = Brutalista,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        textDecoration = TextDecoration.LineThrough
    )

    Column(
        modifier = Modifier
            .background(color = colorResource(id = bountyState.color))
            .padding(vertical = margin8)
            .clickable { bountyState.clickListener }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = margin13),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = bounty.generateDescription(context),
                modifier = Modifier
                    .padding(top = margin8, bottom = margin8, end = margin13)
                    .weight(1f),
                style = if (bountyState.isOpen) MaterialTheme.typography.body1 else strikeThrough
            )

            Text(
                text = stringResource(R.string.bounty_amount_with_currency, bounty.action.bounty_amount),
                modifier = Modifier
                    .padding(top = margin8, bottom = margin8),
                style = if (bountyState.isOpen) MaterialTheme.typography.body1 else strikeThrough,
                fontWeight = FontWeight.Medium
            )
        }

        if (bountyState.msg != 0)
            HorizontalImageTextView(
                drawable = bountyState.icon,
                stringRes = bountyState.msg,
                modifier = Modifier
                    .padding(start = margin13, end = margin13, top = 5.dp, bottom = dimensionResource(id = R.dimen.margin_10)),
                MaterialTheme.typography.caption
            )
    }
}

@Composable
fun CountryDropdown(bountyViewModel: BountyViewModel, isLoading: Boolean) {
    val countries by bountyViewModel.countryList.collectAsState(initial = listOf(CODE_ALL_COUNTRIES))
    val country by bountyViewModel.country.collectAsState()

    var expanded by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(country) }
    var textFieldSize by remember { mutableStateOf(Size.Zero) }

    val icon = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown
    val borderColor = if (isLoading) colorResource(id = R.color.stax_state_blue) else Color.White

    val context = LocalContext.current

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
                    "contentDescription",
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
            readOnly = true
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
                        style = MaterialTheme.typography.body1
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

@Preview
@Composable
fun ChannelBountiesCardPreview() {
    Column {
        Text(
            text = "ACS Microfinance - *614*435# - NG".uppercase(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.margin_13)),
            style = MaterialTheme.typography.body1,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End
        )

        repeat(3) {
            BountyCardPreview()
        }
    }
}

@Preview
@Composable
fun BountyCardPreview() {
    val margin13 = dimensionResource(id = R.dimen.margin_13)
    val margin8 = dimensionResource(id = R.dimen.margin_8)

    Column(
        modifier = Modifier
            .background(color = colorResource(id = R.color.colorSurface))
            .padding(vertical = margin8)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = margin13),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Check Balance",
                modifier = Modifier
                    .padding(top = margin8, bottom = margin8, end = margin13),
                style = MaterialTheme.typography.body1
            )

            Text(
                text = "USD $1",
                modifier = Modifier
                    .padding(top = margin8, bottom = margin8),
                style = MaterialTheme.typography.body1,
                fontWeight = FontWeight.Medium
            )
        }

        HorizontalImageTextView(
            drawable = R.drawable.ic_error,
            stringRes = R.string.bounty_transaction_failed,
            modifier = Modifier.padding(start = margin13, end = margin13, top = 5.dp, bottom = dimensionResource(id = R.dimen.margin_10)),
            MaterialTheme.typography.caption
        )
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

@Preview
@Composable
fun BountiesPreview() {
    StaxTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
            LazyColumn {
                item {
                    CountryDropdownPreview()
                }
                items(5) {
                    ChannelBountiesCardPreview()
                }
            }
        }
    }
}

private fun getBountyState(bounty: Bounty, selectListener: SelectListener): BountyItemState {
    return when {
        bounty.hasASuccessfulTransactions() -> BountyItemState(R.color.muted_green, R.string.done, R.drawable.ic_check, false) {}
        bounty.isLastTransactionFailed() && !bounty.action.bounty_is_open -> BountyItemState(R.color.stax_bounty_red_bg, R.string.bounty_transaction_failed, R.drawable.ic_error, false) {
            selectListener.viewTransactionDetail(bounty.transactions.last().uuid)
        }
        bounty.isLastTransactionFailed() && bounty.action.bounty_is_open -> BountyItemState(R.color.stax_bounty_red_bg, R.string.bounty_transaction_failed_try_again, R.drawable.ic_error, true) {
            selectListener.viewBountyDetail(bounty)
        }
        !bounty.action.bounty_is_open -> BountyItemState(color = R.color.lighter_grey, msg = 0, icon = 0, isOpen = false) {}
        bounty.transactionCount > 0 -> BountyItemState(R.color.pending_brown, R.string.bounty_pending_short_desc, R.drawable.ic_warning, true) {
            selectListener.viewTransactionDetail(bounty.transactions.last().uuid)
        }
        else -> BountyItemState(color = R.color.colorSurface, msg = 0, icon = 0, isOpen = true) {
            selectListener.viewBountyDetail(bounty)
        }
    }
}

fun getCountryString(code: String, context: Context): String = if (code.isEmpty() || code == CountryAdapter.CODE_ALL_COUNTRIES)
    context.getString(R.string.all_countries_with_emoji)
else
    context.getString(R.string.country_with_emoji, code.countryCodeToUnicodeFlag(), getFullCountryName(code))

private fun getFullCountryName(code: String): String {
    val locale = Locale(Lingver.getInstance().getLanguage(), code)
    return locale.displayCountry
}

fun String.countryCodeToUnicodeFlag(): String {
    try {
        return this
            .filter { it in 'A'..'Z' }
            .map { it.code.toByte() }
            .flatMap { char ->
                listOf(
                    0xD8.toByte(),
                    0x3C.toByte(),
                    0xDD.toByte(),
                    (0xE6.toByte() + (char - 'A'.code.toByte())).toByte()
                )
            }
            .toByteArray()
            .let { bytes ->
                String(bytes, Charsets.UTF_16)
            }
    } catch (e: Exception) {
        return ""
    }
}

data class BountyItemState(
    @ColorRes val color: Int,
    @StringRes val msg: Int,
    @DrawableRes val icon: Int,
    val isOpen: Boolean,
    val clickListener: () -> Unit
)

interface SelectListener {
    fun viewTransactionDetail(uuid: String?)
    fun viewBountyDetail(b: Bounty)
}