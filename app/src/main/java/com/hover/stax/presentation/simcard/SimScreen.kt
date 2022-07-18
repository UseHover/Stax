package com.hover.stax.presentation.simcard

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.hover.stax.R
import com.hover.stax.accounts.AccountsViewModel
import com.hover.stax.bonus.BonusViewModel
import com.hover.stax.domain.model.Account
import com.hover.stax.presentation.home.TopBar
import com.hover.stax.ui.theme.*
import com.hover.stax.utils.DateUtils
import com.hover.stax.utils.network.NetworkMonitor
import org.koin.androidx.compose.getViewModel

data class SimScreenClickFunctions(
	val onClickedAddNewAccount: () -> Unit,
	val onClickedSettingsIcon: () -> Unit,
	val onClickedCheckBalance: () -> Unit,
	val onClickedBuyAirtime: () -> Unit
)

@Composable
fun SimScreen(simScreenClickFunctions: SimScreenClickFunctions) {
	val accountsViewModel : AccountsViewModel = getViewModel()
	val bonusViewModel : BonusViewModel =  getViewModel()

	val accounts =  accountsViewModel.telecomAccounts.observeAsState(initial = null)
	val hasNetwork by NetworkMonitor.StateLiveData.get().observeAsState(initial = false)
	val presentSims = accountsViewModel.presentSims.observeAsState(initial = emptyList())
	val bonuses = bonusViewModel.bonusList.collectAsState()

	StaxTheme {
		Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
			Scaffold(topBar = {
				TopBar(title = R.string.nav_home,
					isInternetConnected = hasNetwork,
					simScreenClickFunctions.onClickedSettingsIcon)
			}, content = {
				LazyColumn(modifier = Modifier.padding(horizontal = 13.dp)) {
					item {
						PageTitle()
					}
					item {
						accounts.value?.let {
							if(it.isEmpty()) GrantPermissionText()
						}
					}

					item {
						accounts.value?.let {
							if(it.isEmpty()) {
								LinkSimCard(id = R.string.link_sim_to_stax,
									onClickedLinkSimCard = simScreenClickFunctions.onClickedAddNewAccount)
							}
						}
					}

					accounts.value?.let {
					items(it) { account ->
						if(account.id > 0) {
							val sim =  presentSims.value.find { it.subscriptionId == account.subscriptionId }
							SimItem(simIndex = sim?.slotIdx ?: 1,
								account = account,
								bonus = (bonuses.value.bonuses.first().bonusPercent * 100).toInt(),
								onClickedBuyAirtime = simScreenClickFunctions.onClickedBuyAirtime) {
							}
						}
						else {
							LinkSimCard(id = R.string.link_sim_to_stax,
								onClickedLinkSimCard = simScreenClickFunctions.onClickedAddNewAccount)
						}
					}
					}
				}
			})
		}
	}
}

@Preview
@Composable
fun SimScreenPreview() {
	val accounts = mutableListOf<Account>()
	val account1 = Account("Telecom")
	val account2 = Account("Safaricom")
	account1.id = 1
	account1.subscriptionId = 1
	account1.latestBalance = "NGN 200"
	account1.latestBalanceTimestamp = 123

	account2.id = 2
	account2.subscriptionId = 2
	account2.latestBalance = "NGN 500"
	account2.latestBalanceTimestamp = 2345

	accounts.add(account1)
	accounts.add(account2)

	StaxTheme {
		Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
			Scaffold(topBar = {
				TopBar(title = R.string.nav_home, isInternetConnected = false, {})
			}, content = {
				LazyColumn(modifier = Modifier.padding(horizontal = 13.dp)) {
					item {
						PageTitle()
					}
					item {
						if(accounts.isEmpty()) GrantPermissionText()
					}
					items(accounts) { account ->
							if(account.id > 0) {
								SimItem(simIndex = 1,
									account = account,
									bonus = (0.05 * 100).toInt(),
									onClickedBuyAirtime = { }) {
								}
							}
							else {
								LinkSimCard(id = R.string.link_sim_to_stax,
									onClickedLinkSimCard = {  })
							}
						}
				}
			})
		}
	}
}

@Composable
fun PageTitle() {
	val size13 = dimensionResource(id = R.dimen.margin_13)
	Text(
		text = stringResource(id = R.string.your_linked_sim),
		modifier = Modifier.padding(vertical = size13),
		style = MaterialTheme.typography.button
	)
}

@Composable
fun GrantPermissionText() {
	val size13 = dimensionResource(id = R.dimen.margin_13)
	val size34 = dimensionResource(id = R.dimen.margin_34)
	Text(
		text = stringResource(id = R.string.simpage_permission_alert),
		modifier = Modifier
			.padding(vertical = size13, horizontal = size34)
			.fillMaxWidth(),
		textAlign = TextAlign.Center,
		color = TextGrey,
	)
}

@Composable
fun SimItem(simIndex: Int, 
            account: Account,
            bonus : Int,
            onClickedBuyAirtime: () -> Unit, 
            onClickedCheckBalance: () -> Unit) {
	
	val size13 = dimensionResource(id = R.dimen.margin_13)
	OutlinedButton(
		onClick = {},
		modifier = Modifier
			.fillMaxWidth()
			.padding(vertical = 13.dp)
			.shadow(elevation = 0.dp),
		shape = MaterialTheme.shapes.medium,
		border = BorderStroke(width = 0.5.dp, color = DarkGray),
		colors = ButtonDefaults.buttonColors(
			backgroundColor = ColorSurface,
			contentColor = OffWhite
		)
	) {
		Column(modifier = Modifier
			.fillMaxWidth()) {
			SimItemTopRow(simIndex = simIndex, account = account, onClickedCheckBalance = onClickedCheckBalance)
			Column {
				Text(text = stringResource(id = R.string.airtime_balance_holder, account.latestBalance ?: "-"),
				color = TextGrey, modifier = Modifier.padding(top = size13), style = MaterialTheme.typography.body1)

				Text(text = stringResource(id = R.string.as_of, DateUtils.humanFriendlyDate(account.latestBalanceTimestamp)),
					color = TextGrey, modifier = Modifier.padding(bottom = 26.dp), style = MaterialTheme.typography.body1)

				OutlinedButton(
					onClick = onClickedBuyAirtime,
					modifier = Modifier
						.padding(bottom = 6.dp)
						.shadow(elevation = 0.dp)
						.wrapContentWidth(),
					shape = MaterialTheme.shapes.medium,
					border = BorderStroke(width = 0.5.dp, color = DarkGray),
					colors = ButtonDefaults.buttonColors(
						backgroundColor = ColorSurface,
						contentColor = OffWhite
					)
				) {
					Row(modifier = Modifier
						.wrapContentWidth()
						.padding(all = 5.dp)) {
						var buyAirtimeLabel = stringResource(id = R.string.nav_airtime)
						if(bonus > 0) {
							val bonusPercent = bonus.toString().plus("%")
							buyAirtimeLabel = stringResource(id = R.string.buy_airitme_with_discount, bonusPercent)
						}
						Text(
							text = buyAirtimeLabel,
							style = MaterialTheme.typography.button,
							modifier = Modifier
								.padding(end = 5.dp),
							textAlign = TextAlign.Start,
							fontSize = 14.sp
						)
						Image(painter = painterResource(id = R.drawable.ic_bonus),
							contentDescription = null, modifier = Modifier.size(18.dp) )
					}
				}

			}
			
		}
	}
}

@Composable
fun SimItemTopRow(simIndex: Int,
                  account: Account, 
                  onClickedCheckBalance: () -> Unit) {
	val size34 = dimensionResource(id = R.dimen.margin_34)
	Row {
		AsyncImage(
			model = ImageRequest.Builder(LocalContext.current)
				.data(account.logoUrl)
				.crossfade(true)
				.diskCachePolicy(CachePolicy.ENABLED)
				.build(),
			contentDescription = "",
			placeholder = painterResource(id = R.drawable.image_placeholder),
			error = painterResource(id = R.drawable.ic_stax),
			modifier = Modifier
				.size(size34)
				.clip(CircleShape)
				.align(Alignment.CenterVertically),
			contentScale = ContentScale.Crop
		)

		Column(modifier = Modifier
			.weight(1f)
			.padding(horizontal = 13.dp)) {
			Text(text = account.name, style = MaterialTheme.typography.body1)
			Text(text = stringResource(id = R.string.sim_index, simIndex), 
				color = TextGrey, style = MaterialTheme.typography.body2)
		}

		Button(onClick = onClickedCheckBalance,
			modifier = Modifier
				.width(120.dp)
				.shadow(elevation = 2.dp),
			shape = MaterialTheme.shapes.medium,
			colors = ButtonDefaults.buttonColors(backgroundColor = BrightBlue, contentColor = ColorPrimary)) {
			Text(
				text = stringResource(id = R.string.check_balance_capitalized),
				style = MaterialTheme.typography.button,
				modifier = Modifier
					.fillMaxWidth(),
				textAlign = TextAlign.Center,
				fontSize = 12.sp
			)
		}
	}
}

@Composable
fun LinkSimCard(@StringRes id: Int, onClickedLinkSimCard: () -> Unit) {
	OutlinedButton(
		onClick = onClickedLinkSimCard,
		modifier = Modifier
			.fillMaxWidth()
			.shadow(elevation = 0.dp),
		shape = MaterialTheme.shapes.medium,
		border = BorderStroke(width = 0.5.dp, color = DarkGray),
		colors = ButtonDefaults.buttonColors(
			backgroundColor = ColorSurface,
			contentColor = OffWhite
		)
	) {
		Text(
			text = stringResource(id = id),
			style = MaterialTheme.typography.button,
			modifier = Modifier
				.fillMaxWidth()
				.padding(top = 5.dp, bottom = 5.dp),
			textAlign = TextAlign.Center
		)
	}

}
