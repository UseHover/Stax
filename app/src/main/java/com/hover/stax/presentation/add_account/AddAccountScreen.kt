package com.hover.stax.presentation.add_account

import android.annotation.SuppressLint
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.google.accompanist.pager.*
import com.hover.stax.addChannels.ChannelsViewModel
import com.hover.stax.ui.theme.StaxTheme
import com.hover.stax.R
import com.hover.stax.channels.Channel
import com.hover.stax.presentation.components.CountryDropdown
import com.hover.stax.presentation.components.StaxTextField
import com.hover.stax.presentation.components.StaxTextFieldDefaults
import com.hover.stax.ui.theme.OffWhite
import com.hover.stax.ui.theme.mainBackground
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@OptIn(ExperimentalPagerApi::class)
@Composable
fun AddAccountScreen(channelsViewModel: ChannelsViewModel = getViewModel()) {

	val simList by channelsViewModel.sims.observeAsState(initial = emptyList())
	val simCountryList by channelsViewModel.simCountryList.observeAsState(initial = emptyList())
	val countryList by channelsViewModel.channelCountryList.observeAsState(initial = emptyList())
	val channels by channelsViewModel.filteredChannels.observeAsState(initial = emptyList())

	val pagerState = rememberPagerState()
	val tabs = listOf(TabItem.MobileMoney(channels), TabItem.Bank(channels), TabItem.Crypto)

	val searchValue = remember { mutableStateOf(TextFieldValue()) }

	StaxTheme {
		Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
			Scaffold(
				topBar = { TopBar() },
			) {
				Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.Center) {

					Row(Modifier.padding(horizontal = 8.dp)) {
						Column(Modifier.padding(8.dp)) {
							CountryDropdown(channelsViewModel.countryChoice.value ?: "00", countryList) {
								channelsViewModel.countryChoice.postValue(it)
							}
						}

						Column() {
							StaxTextField(searchValue.value, R.string.search, R.drawable.ic_search) {
								searchValue.value = it
							}
						}
					}

					Spacer(modifier = Modifier.height(13.dp))

					Tabs(tabs = tabs, pagerState = pagerState)
					TabsContent(tabs = tabs, pagerState = pagerState)
				}
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar() {
	Column(modifier = Modifier.fillMaxWidth()) {
		CenterAlignedTopAppBar(
	        title = { Text(text = stringResource(R.string.add_account), fontSize = 18.sp) },
			colors = StaxTopBarDefaults()
	    )
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaxTopBarDefaults() = TopAppBarDefaults.centerAlignedTopAppBarColors(
	titleContentColor = OffWhite,
	containerColor = mainBackground)

@Composable
fun MobileMoneyScreen(channels: List<Channel>?) {
	ChannelList(channels, "mmo")
}

@Composable
fun BankScreen(channels: List<Channel>?) {
	ChannelList(channels, "bank")
}

@Composable
fun ChannelList(channels: List<Channel>?, type: String) {
	if (channels?.filter { it.institutionType == type }.isNullOrEmpty()) {
		Text(
			text = stringResource(id = R.string.loading_human),
			style = MaterialTheme.typography.h2,
			color = colorResource(id = R.color.stax_state_blue),
			modifier = Modifier.padding(horizontal = 16.dp).padding(top = 5.dp),
		)
	} else {
		LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 5.dp),
			verticalArrangement = Arrangement.spacedBy(5.dp)) {
			items(channels!!.filter { it.institutionType == type }) { channel ->
				ChannelItem(channel)
			}
		}
	}
}

@Composable
fun CryptoScreen() {
	Row(modifier = Modifier.fillMaxWidth().padding(top = 5.dp)) {
		Icon(painterResource(R.drawable.ic_crypto), contentDescription = "USDC logo", modifier = Modifier.align(Alignment.CenterVertically))
		Text(
			text = "USDC",
			color = OffWhite,
			modifier = Modifier.align(Alignment.CenterVertically).padding(horizontal = 16.dp).padding(top = 5.dp),
			fontSize = 18.sp
		)
	}
}

@Composable
fun ChannelItem(channel: Channel) {
	Row(Modifier.padding(vertical = 8.dp)) {
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
			modifier = Modifier.align(Alignment.CenterVertically).padding(horizontal = 16.dp),
			fontSize = 18.sp,
		)
	}
}

@Preview
@Composable
fun AddAccountScreenPreview() {
	AddAccountScreen()
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun Tabs(tabs: List<TabItem>, pagerState: PagerState) {
	val scope = rememberCoroutineScope()
	TabRow(
		selectedTabIndex = pagerState.currentPage,
		backgroundColor = mainBackground,
		contentColor = OffWhite,
		indicator = { tabPositions ->
			TabRowDefaults.Indicator(
				Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
			)
		}) {
		tabs.forEachIndexed { index, tab ->
			LeadingIconTab(
				icon = { Icon(painter = painterResource(id = tab.icon), contentDescription = "") },
				text = { Text(tab.title) },
				selected = pagerState.currentPage == index,
				modifier = Modifier.padding(vertical = 8.dp),
				onClick = {
					scope.launch {
						pagerState.animateScrollToPage(index)
					}
				},
			)
		}
	}
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun TabsContent(tabs: List<TabItem>, pagerState: PagerState) {
	HorizontalPager(state = pagerState, count = tabs.size, modifier = Modifier.padding(horizontal = 16.dp)) { page ->
		tabs[page].screen()
	}
}