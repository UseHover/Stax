package com.hover.stax.presentation.add_accounts

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.accompanist.pager.*
import com.hover.stax.addAccounts.AddAccountViewModel
import com.hover.stax.R
import com.hover.stax.channels.Channel
import com.hover.stax.presentation.add_accounts.components.SampleChannelProvider
import com.hover.stax.presentation.add_accounts.components.TabItem
import com.hover.stax.presentation.components.*
import com.hover.stax.ui.theme.Background
import com.hover.stax.ui.theme.BrightBlue
import com.hover.stax.ui.theme.OffWhite
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalMaterialApi::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun ChooseChannelScreen(addAccountViewModel: AddAccountViewModel = getViewModel(), navController: NavController) {
	val countries by addAccountViewModel.channelCountryList.observeAsState(initial = emptyList())
	val channels by addAccountViewModel.filteredChannels.observeAsState(initial = emptyList())
	val countryChoice by addAccountViewModel.countryChoice.observeAsState(initial = "00")

	val channelChoice by addAccountViewModel.chosenChannel.collectAsState(initial = null)

	val showingHelp = remember { mutableStateOf(false) }

	val coroutineScope = rememberCoroutineScope()
	val bottomSheetState = rememberModalBottomSheetState(
		initialValue = ModalBottomSheetValue.Hidden,
		skipHalfExpanded = true)

	Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
		ModalBottomSheetLayout(
			sheetState = bottomSheetState,
			sheetShape = bottomSheetShape(),
			sheetBackgroundColor = Background,
			sheetContent = { AddChannelBottomSheet(channelChoice, addAccountViewModel) }) {
			Scaffold(
				topBar = { TopBar(showingHelp) }
			) {
				FindAccountScreen(channels, countries, countryChoice,
					navigateToAdd = {
						onChoice(it, addAccountViewModel, bottomSheetState, coroutineScope)
					},
					navigateToUSDC = { navController.navigate("createUSDC") },
					onSelectCountry = { addAccountViewModel.countryChoice.postValue(it) },
					onSearch = { addAccountViewModel.filterQuery.postValue(it) })
				showHelp(showingHelp)
				BackHandler(bottomSheetState.isVisible) {
					coroutineScope.launch { bottomSheetState.hide() }
				}
			}
		}
	}
}

fun bottomSheetShape() = RoundedCornerShape(bottomStart = 0.dp, bottomEnd = 0.dp, topStart = 12.dp, topEnd = 12.dp)

@OptIn(ExperimentalMaterialApi::class)
fun onChoice(
	channel: Channel,
	addAccountViewModel: AddAccountViewModel,
	bottomSheetState: ModalBottomSheetState,
	coroutineScope: CoroutineScope
) {
	addAccountViewModel.chooseChannel(channel)
	coroutineScope.launch {
		bottomSheetState.show()
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(showingHelp: MutableState<Boolean>) {
	Column(modifier = Modifier.fillMaxWidth()) {
		CenterAlignedTopAppBar(
			title = { Text(text = stringResource(R.string.add_account), style = MaterialTheme.typography.h1) },
			colors = StaxTopBarDefaults(),
			actions = { IconButton(onClick = { showingHelp.value = true }) {
				Icon(painterResource(id = R.drawable.ic_question),
					stringResource(R.string.learn_more), tint = BrightBlue)
			} }
		)
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaxTopBarDefaults() = TopAppBarDefaults.centerAlignedTopAppBarColors(
	titleContentColor = OffWhite,
	containerColor = Color.Transparent)

@Composable
fun showHelp(showingHelp: MutableState<Boolean>) {
	if (showingHelp.value) {
		AlertDialog(title = { Text(stringResource(R.string.add_accounts_help_title)) },
			text = { Text(stringResource(R.string.add_accounts_help_info)) },
			buttons = {
				Row(horizontalArrangement = Arrangement.End,
					modifier = Modifier
						.fillMaxWidth()
						.padding(13.dp))
				{ SecondaryButton(stringResource(R.string.btn_ok), onClick = { showingHelp.value = false }) }},
			onDismissRequest = { showingHelp.value = false })
	}
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun FindAccountScreen(channels: List<Channel>, countries: List<String>, countryChoice: String,
                      navigateToAdd: (Channel) -> Unit, navigateToUSDC: () -> Unit,
                      onSelectCountry: (String) -> Unit, onSearch: (String) -> Unit) {

	var searchValue by remember { mutableStateOf(TextFieldValue("")) }

	val pagerState = rememberPagerState()
	val tabs = listOf(TabItem.MobileMoney(channels, navigateToAdd), TabItem.Bank(channels, navigateToAdd), TabItem.Crypto(navigateToUSDC))

	Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.Center) {
		Row(Modifier.padding(horizontal = 8.dp)) {
			Column(Modifier.padding(8.dp)) {
				CountryDropdown(countryChoice, countries) {
					onSelectCountry(it)
				}
			}

			Column() {
				StaxTextField(searchValue, R.string.search, R.drawable.ic_search,
					onChange = {
						searchValue = it
						onSearch(it.text) })
			}
		}

		Spacer(modifier = Modifier.height(13.dp))

		Tabs(tabs = tabs, pagerState = pagerState)
		TabsContent(tabs = tabs, pagerState = pagerState)
	}
}

@Composable
fun ChannelList(channels: List<Channel>?, type: String, navigateToAdd: (Channel) -> Unit) {
	if (channels?.filter { it.institutionType == type }.isNullOrEmpty()) {
		Text(
			text = stringResource(id = R.string.loading_human),
			style = MaterialTheme.typography.h2,
			color = colorResource(id = R.color.stax_state_blue),
			modifier = Modifier
				.padding(horizontal = 16.dp)
				.padding(top = 13.dp),
		)
	} else {
		LazyColumn(modifier = Modifier
			.fillMaxSize()
			.padding(top = 13.dp),
			verticalArrangement = Arrangement.spacedBy(5.dp)) {
			items(channels!!.filter { it.institutionType == type }) { channel ->
				ChannelItem(channel, navigateToAdd)
			}
		}
	}
}

@Composable
fun ChannelItem(channel: Channel, navigateToAdd: (Channel) -> Unit) {
	ListItem(title = channel.name, modifier = Modifier.clickable { navigateToAdd(channel) }) {
		Logo(channel.logoUrl, channel.name + " logo")
	}
}

@Composable
fun ListItem(title: String, modifier: Modifier, logo: @Composable () -> Unit) {
	Row(
		modifier
			.padding(vertical = 8.dp)
			.fillMaxWidth()) {
		logo()
		Text(
			text = title,
			color = OffWhite,
			modifier = Modifier
				.align(Alignment.CenterVertically)
				.padding(horizontal = 16.dp),
			fontSize = 18.sp,
		)
	}
}

@Composable
fun CryptoScreen(navigateToUSDC: () -> Unit) {
	Column(modifier = Modifier.padding(top = 13.dp)) {
		ListItem("Stellar USDC", Modifier.clickable { navigateToUSDC() }) {
			Icon(
				painterResource(R.drawable.stellar_logo),
				contentDescription = "Stellar USDC logo",
				modifier = Modifier.height(34.dp).padding(5.dp)
			)
		}
	}
}

@Preview
@Composable
fun AddAccountScreenPreview(@PreviewParameter(SampleChannelProvider::class) channels: List<Channel>) {
	val countries = listOf("00", "ke", "ng", "mz", "zm")
	FindAccountScreen(channels, countries, "ke", {}, {}, {}, {})
}