package com.hover.stax.balances

import android.content.Context
import android.widget.Toast
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
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.danchoo.glideimage.GlideImage
import com.hover.stax.R
import com.hover.stax.domain.model.Account
import com.hover.stax.ui.theme.StaxTheme
import com.hover.stax.utils.DateUtils

class BalanceScreen {

	@Composable
	fun showBalances(accountList: List<Account>,
	                 balanceTapListener: BalanceTapListener?,
	                 context: Context) {
		LazyColumn(modifier = Modifier.fillMaxWidth()) {
			items(accountList) { account ->
				balanceItem(staxAccount = account,
					context = context, balanceTapListener = balanceTapListener )
			}
		}
	}

	@Composable
	private fun balanceItem(staxAccount: Account,
	                        balanceTapListener: BalanceTapListener?,
	                        context: Context) {
		val size24 = dimensionResource(id = R.dimen.margin_24)
		val size13 = dimensionResource(id = R.dimen.margin_13)
		val size18 = dimensionResource(id = R.dimen.margin_18)
		Column {
			Row(modifier = Modifier
				.background(color = colorResource(id = R.color.colorBackground))
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
						.clickable{balanceTapListener?.onTapRefresh(staxAccount)})

			}
			Divider(color = colorResource(id = R.color.nav_grey))
		}
	}

	interface BalanceTapListener {
		fun onTapRefresh(account: Account?)
		fun onTapDetail(accountId: Int)
	}
	@Preview
	@Composable
	fun balancePreview() {
		StaxTheme {
			Surface {
				val accountList = mutableListOf<Account>()
				accountList.add(fakeAccount())
				accountList.add(fakeAccount())
				accountList.add(fakeAccount())
				val context = LocalContext.current
				showBalances(accountList = accountList,
					context = context, balanceTapListener = null)
			}
		}
	}

	private fun fakeAccount(): Account {
		val account = Account(name = "Dummy account")
		account.latestBalance = "24,500"
		account.latestBalanceTimestamp = 12345
		return account
	}
}
