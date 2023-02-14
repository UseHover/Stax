package com.hover.stax.presentation.add_accounts

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.google.accompanist.pager.*
import com.hover.stax.addChannels.ChannelsViewModel
import com.hover.stax.R
import com.hover.stax.channels.Channel
import com.hover.stax.presentation.add_accounts.components.SampleChannelProvider
import com.hover.stax.presentation.add_accounts.components.TabItem
import com.hover.stax.presentation.components.*
import com.hover.stax.ui.theme.Background
import com.hover.stax.ui.theme.BrightBlue
import com.hover.stax.ui.theme.OffWhite
import org.koin.androidx.compose.getViewModel

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun ChooseChannelScreen(channelsViewModel: ChannelsViewModel = getViewModel(), navController: NavController) {

	val simList by channelsViewModel.sims.observeAsState(initial = emptyList())
	val countryChannels by channelsViewModel.simCountryList.observeAsState(initial = emptyList())

	val countries by channelsViewModel._channelCountryList.observeAsState(initial = emptyList())
	val channels by channelsViewModel.filteredChannels.observeAsState(initial = emptyList())
	val countryChoice by channelsViewModel.countryChoice.observeAsState(initial = "00")

	val showingHelp = remember { mutableStateOf(false) }

	Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
		Scaffold(
			topBar = { TopBar(showingHelp) },
		) {
			FindAccountScreen(channels, countries, countryChoice,
				{ onChoice(it, channelsViewModel) }, { navController.navigate("createUSDC") },
				{ channelsViewModel.countryChoice.postValue(it) },
				{ channelsViewModel.filterQuery.postValue(it) })
			showHelp(showingHelp)
		}
	}
}

fun onChoice(channel: Channel, channelsViewModel: ChannelsViewModel) {
	channelsViewModel.createAccount(channel)
//	navController.navigate("addChannel")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(showingHelp: MutableState<Boolean>) {
	Column(modifier = Modifier.fillMaxWidth()) {
		CenterAlignedTopAppBar(
			title = { Text(text = stringResource(R.string.add_account), fontSize = 18.sp) },
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
	containerColor = Background)

@Composable
fun showHelp(showingHelp: MutableState<Boolean>) {
	if (showingHelp.value) {
		AlertDialog(title = { Text(stringResource(R.string.add_accounts_help_title)) },
			text = { Text(stringResource(R.string.add_accounts_help_info)) },
			buttons = {
				Row(horizontalArrangement = Arrangement.End,
					modifier = Modifier.fillMaxWidth().padding(13.dp))
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
			.padding(top = 5.dp),
			verticalArrangement = Arrangement.spacedBy(5.dp)) {
			items(channels!!.filter { it.institutionType == type }) { channel ->
				ChannelItem(channel, navigateToAdd)
			}
		}
	}
}

@Composable
fun ChannelItem(channel: Channel, navigateToAdd: (Channel) -> Unit) {
	Row(Modifier.padding(vertical = 8.dp).clickable { navigateToAdd(channel) }) {
		AsyncImage(
			model = ImageRequest.Builder(LocalContext.current).data(channel.logoUrl)
				.crossfade(true)
				.diskCachePolicy(CachePolicy.ENABLED).build(),
			contentDescription = channel.name + " logo",
			placeholder = painterResource(id = R.drawable.img_placeholder),
			error = painterResource(id = R.drawable.img_placeholder),
			modifier = Modifier
				.size(dimensionResource(id = R.dimen.margin_34))
				.clip(CircleShape),
			contentScale = ContentScale.Crop
		)
		Text(
			text = channel.name,
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
	Row(modifier = Modifier.fillMaxWidth().padding(top = 5.dp)
		.clickable { navigateToUSDC() }) {
		Icon(painterResource(R.drawable.ic_crypto), contentDescription = "USDC logo", modifier = Modifier.align(Alignment.CenterVertically))
		Text(
			text = "USDC",
			color = OffWhite,
			modifier = Modifier
				.align(Alignment.CenterVertically)
				.padding(horizontal = 16.dp)
				.padding(top = 5.dp),
			fontSize = 18.sp
		)
	}
}

@Preview
@Composable
fun AddAccountScreenPreview(@PreviewParameter(SampleChannelProvider::class) channels: List<Channel>) {
	val countries = listOf("00", "ke", "ng", "mz", "zm")
	FindAccountScreen(channels, countries, "ke", {}, {}, {}, {})
}