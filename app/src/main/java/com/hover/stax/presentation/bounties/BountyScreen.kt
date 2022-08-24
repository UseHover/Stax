package com.hover.stax.presentation.bounties

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.hover.stax.R
import com.hover.stax.domain.model.Bounty
import com.hover.stax.presentation.bounties.components.ChannelBountyCard
import com.hover.stax.presentation.bounties.components.CountryDropdown
import com.hover.stax.presentation.bounties.components.SpannableImageTextView
import com.hover.stax.presentation.bounties.components.getCountryString
import com.hover.stax.ui.theme.StaxTheme

const val CODE_ALL_COUNTRIES = "00"

@Composable
fun BountyList(bountyViewModel: BountyViewModel) {
    val bountiesState by bountyViewModel.bountiesState.collectAsState()
    val countries by bountyViewModel.countryList.collectAsState(initial = listOf(CODE_ALL_COUNTRIES))
    val country by bountyViewModel.country.collectAsState()

    StaxTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize(), color = MaterialTheme.colors.background
        ) {
            LazyColumn {
                item {
                    CountryDropdown(countries, country, bountiesState.loading, bountyViewModel)
                }

                items(bountiesState.bounties) {
                    ChannelBountyCard(channelBounty = it, bountyViewModel)
                }

                if (bountiesState.bounties.isEmpty() && !bountiesState.loading) {
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

        SpannableImageTextView(
            drawable = R.drawable.ic_error,
            stringRes = R.string.bounty_transaction_failed,
            modifier = Modifier.padding(start = margin8, end = margin13, top = 5.dp, bottom = dimensionResource(id = R.dimen.margin_10)),
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

data class BountyItemState(
    @ColorRes val color: Int = 0,
    @StringRes val msg: Int = 0,
    @DrawableRes val icon: Int = 0,
    val isOpen: Boolean = true,
    val bountySelectEvent: BountySelectEvent? = null
)

sealed class BountySelectEvent {
    data class ViewTransactionDetail(val uuid: String) : BountySelectEvent()
    data class ViewBountyDetail(val bounty: Bounty) : BountySelectEvent()
}