package com.hover.stax.presentation.add_account

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.*
import com.hover.stax.addChannels.ChannelsViewModel
import com.hover.stax.ui.theme.StaxTheme
import com.hover.stax.R
import com.hover.stax.presentation.components.CountryDropdown
import com.hover.stax.presentation.components.StaxTextField
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

	val tabs = listOf(TabItem.MobileMoney, TabItem.Bank, TabItem.Crypto)
	val pagerState = rememberPagerState()

	val searchValue = remember { mutableStateOf(TextFieldValue()) }

	StaxTheme {
		Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
			Scaffold(
				topBar = { TopBar() },
			) {
				Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.Center) {

					Row {
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

@Composable
fun TopBar() {
	Column(modifier = Modifier.fillMaxWidth()) {
		TopAppBar(
	        title = { Text(text = stringResource(R.string.add_account), fontSize = 18.sp) },
	        backgroundColor = mainBackground,
	        contentColor = OffWhite
	    )
	}
}

@Composable
fun MobileMoneyScreen(channelsViewModel: ChannelsViewModel) {
	Column(modifier = Modifier
		.fillMaxSize()
		.wrapContentSize(Alignment.Center)
	) {
		channelsViewModel.filteredChannels.value?.forEach { channel ->
			Icon()
			Text(text = channel.name,
				color = OffWhite,
				modifier = Modifier.align(Alignment.CenterHorizontally),
				fontSize = 25.sp
			)
		}

	}
}

@Composable
fun BankScreen() {
	Column(modifier = Modifier
		.fillMaxSize()
		.wrapContentSize(Alignment.Center)
	) {
		Text(
			text = "Bank View",
			fontWeight = FontWeight.Bold,
			color = Color.White,
			modifier = Modifier.align(Alignment.CenterHorizontally),
			textAlign = TextAlign.Center,
			fontSize = 25.sp
		)
	}
}

@Composable
fun CryptoScreen() {
	Column(
		modifier = Modifier
			.fillMaxSize()
			.wrapContentSize(Alignment.Center)
	) {
		Text(
			text = "Crypto View",
			fontWeight = FontWeight.Bold,
			color = Color.White,
			modifier = Modifier.align(Alignment.CenterHorizontally),
			textAlign = TextAlign.Center,
			fontSize = 25.sp
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
	HorizontalPager(state = pagerState, count = tabs.size) { page ->
		tabs[page].screen()
	}
}