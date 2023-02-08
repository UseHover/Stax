package com.hover.stax.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.hover.stax.presentation.add_accounts.components.TabItem
import com.hover.stax.ui.theme.OffWhite
import com.hover.stax.ui.theme.mainBackground
import kotlinx.coroutines.launch

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