package com.hover.stax.balances

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danchoo.glideimage.GlideImage
import com.hover.stax.R
import com.hover.stax.domain.model.Account
import com.hover.stax.presentation.home.HomeViewModel
import com.hover.stax.ui.theme.StaxTheme
import com.hover.stax.utils.DateUtils

@Composable
private fun ShowBalances(accountList: List<Account>,
                         balanceTapListener: BalanceTapListener?,
                         onClickedAddAccount: () -> Unit,
                         context: Context) {

	if (accountList.isEmpty()) {
		EmptyBalance(onClickedAddAccount)
	}
	else {
		LazyColumn(modifier = Modifier.fillMaxWidth()) {
			items(accountList) { account ->
				BalanceItem(staxAccount = account,
					context = context,
					balanceTapListener = balanceTapListener)
			}
		}
	}
}

@Composable
private fun BalanceHeader(onClickedAddAccount: () -> Unit, accountExists: Boolean) {
	val size13 = dimensionResource(id = R.dimen.margin_13)
	Row(modifier = Modifier
		.padding(all = size13)
		.fillMaxWidth()) {
		Text(text = stringResource(id = R.string.your_accounts),
			modifier = Modifier.weight(1f),
			style = MaterialTheme.typography.h3)

		if (accountExists) {
			Text(text = stringResource(id = R.string.add_an_account),
				modifier = Modifier
					.clickable(onClick = onClickedAddAccount)
					.padding(end = 5.dp))
			Icon(painter = painterResource(id = R.drawable.ic_add_white_16),
				contentDescription = null,
				modifier = Modifier
					.clip(CircleShape)
					.background(color = colorResource(id = R.color.brightBlue)))
		}
	}
}

@Composable
private fun EmptyBalance(onClickedAddAccount: () -> Unit) {
	val size34 = dimensionResource(id = R.dimen.margin_34)
	val size16 = dimensionResource(id = R.dimen.margin_16)
	Column(modifier = Modifier.padding(horizontal = size34, vertical = size16)) {
		Text(text = stringResource(id = R.string.empty_balance_desc),
			fontSize = 14.sp,
			color = colorResource(id = R.color.offWhite),
			textAlign = TextAlign.Center)

		Surface(shape = RoundedCornerShape(corner = CornerSize(size = 5.dp)),
			border = BorderStroke(width = 0.5.dp, color = colorResource(id = R.color.darkGrey)),
			modifier = Modifier
				.fillMaxWidth()
				.padding(top = 16.dp)
				.clickable(onClick = onClickedAddAccount)) {
			Text(text = stringResource(id = R.string.add_account),
				style = MaterialTheme.typography.h3,
				modifier = Modifier
					.fillMaxWidth()
					.padding(all = 13.dp),
				textAlign = TextAlign.Center)
		}
	}
}

@Composable
private fun BalanceItem(staxAccount: Account,
                        balanceTapListener: BalanceTapListener?,
                        context: Context) {
	val size24 = dimensionResource(id = R.dimen.margin_24)
	val size18 = dimensionResource(id = R.dimen.margin_18)
	val size13 = dimensionResource(id = R.dimen.margin_13)
	Column {
		Row(modifier = Modifier
			.background(color = MaterialTheme.colors.background)
			.fillMaxWidth()
			.padding(vertical = size18)
			.clickable { balanceTapListener?.onTapDetail(accountId = staxAccount.id) }) {
			GlideImage(data = staxAccount.logoUrl,
				contentScale = ContentScale.Crop,
				placeHolder = R.drawable.image_placeholder,
				width = size24,
				height = size24,
				modifier = Modifier
					.clip(CircleShape)
					.align(Alignment.CenterVertically)
					.padding(horizontal = size13))

			Text(text = staxAccount.name,
				style = MaterialTheme.typography.body1,
				modifier = Modifier
					.weight(1f)
					.align(Alignment.CenterVertically),
				color = colorResource(id = R.color.white))
			Column {
				Text(text = staxAccount.latestBalance ?: " -",
					modifier = Modifier.align(Alignment.End),
					color = colorResource(id = R.color.offWhite))
				Text(text = DateUtils.timeAgo(context, staxAccount.latestBalanceTimestamp),
					modifier = Modifier.align(Alignment.End),
					color = colorResource(id = R.color.offWhite))
			}

			Image(painter = painterResource(id = R.drawable.ic_refresh_white_24dp),
				contentDescription = null,
				modifier = Modifier
					.align(Alignment.CenterVertically)
					.padding(horizontal = size13)
					.clickable { balanceTapListener?.onTapRefresh(staxAccount) })

		}
		Divider(color = colorResource(id = R.color.nav_grey))
	}
}

interface BalanceTapListener {
	fun onTapRefresh(account: Account?)
	fun onTapDetail(accountId: Int)
}


@Composable
fun BalanceScreen(homeViewModel: HomeViewModel,
                  balanceTapListener: BalanceTapListener,
                  onClickedAddAccount: () -> Unit) {
	StaxTheme {
		Surface {
			val homeState = homeViewModel.homeState.collectAsState()
			val context = LocalContext.current

			Column(modifier = Modifier.background(color = MaterialTheme.colors.background)) {
				BalanceHeader(onClickedAddAccount = onClickedAddAccount,
					homeState.value.accounts.isNotEmpty())
				ShowBalances(accountList = homeState.value.accounts,
					context = context,
					balanceTapListener = balanceTapListener,
					onClickedAddAccount = onClickedAddAccount)
			}
		}
	}
}


@Preview
@Composable
fun BalanceScreenPreview() {
	StaxTheme {
		Surface {
			val accountList = mutableListOf<Account>()
			accountList.add(fakeAccount())
			accountList.add(fakeAccount())
			accountList.add(fakeAccount())
			val context = LocalContext.current

			Column(modifier = Modifier.background(color = MaterialTheme.colors.background)) {
				BalanceHeader(onClickedAddAccount = {}, accountExists = true)
				ShowBalances(accountList = emptyList(),
					context = context,
					balanceTapListener = null,
					onClickedAddAccount = {})
			}
		}
	}
}

private fun fakeAccount(): Account {
	val account = Account(name = "Dummy account")
	account.latestBalance = "24,500"
	account.latestBalanceTimestamp = 12345
	return account
}
