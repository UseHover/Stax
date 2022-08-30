package com.hover.stax.presentation.bounties

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.hover.stax.R
import com.hover.stax.domain.model.Bounty
import com.hover.stax.presentation.bounties.components.ChannelBountiesCardPreview
import com.hover.stax.presentation.bounties.components.ChannelBountyCard
import com.hover.stax.presentation.bounties.components.CountryDropdown
import com.hover.stax.presentation.bounties.components.CountryDropdownPreview
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