package com.hover.stax.presentation.sims.components

import android.content.Context
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.hover.sdk.sims.SimInfo
import com.hover.stax.R
import com.hover.stax.domain.model.Account
import com.hover.stax.domain.use_case.sims.SimWithAccount
import com.hover.stax.presentation.components.DisabledButton
import com.hover.stax.presentation.components.PrimaryButton
import com.hover.stax.presentation.components.SecondaryButton
import com.hover.stax.presentation.components.StaxCard
import com.hover.stax.presentation.home.BalanceTapListener
import com.hover.stax.ui.theme.TextGrey
import com.hover.stax.utils.DateUtils
import com.hover.stax.utils.Utils

@Composable
internal fun SimItem(
	simWithAccount: SimWithAccount,
	refreshBalance: (Account) -> Unit,
	buyAirtime: (Account) -> Unit
) {
	StaxCard {
		val context = LocalContext.current

		SimItemTopRow(simWithAccount, refreshBalance)
		if (simWithAccount.account.channelId != -1) {
			val notYetChecked = stringResource(id = R.string.not_yet_checked)

			Text(
				text = stringResource(
					id = R.string.airtime_balance_holder,
					simWithAccount.account.latestBalance ?: notYetChecked
				),
				color = TextGrey,
				style = MaterialTheme.typography.body1
			)

			if (simWithAccount.account.latestBalance != null) {
				Text(
					text = stringResource(
						id = R.string.as_of,
						DateUtils.humanFriendlyDateTime(simWithAccount.account.latestBalanceTimestamp)
					),
					color = TextGrey,
					modifier = Modifier.padding(bottom = 26.dp),
					style = MaterialTheme.typography.body1
				)
			}
		} else {
			Text(
				text = stringResource(
					id = R.string.unsupported_sim_info
				),
				color = TextGrey,
				modifier = Modifier.padding(bottom = 7.dp),
				style = MaterialTheme.typography.body2
			)
		}

		if (simWithAccount.account.channelId == -1) {
			SecondaryButton(context.getString(R.string.email_support),null,
				onClick = { emailStax(simWithAccount, context) })
		}
		else {
			val bonus = simWithAccount.bonus
			SecondaryButton(
				getAirtimeButtonLabel(bonus, context), getAirtimeButtonIcon(bonus),
				onClick = { buyAirtime(simWithAccount.account) })
		}
	}
}
private fun getAirtimeButtonLabel(bonus: Int, context: Context) : String {
	var label = context.getString(R.string.nav_airtime)
	if (bonus > 0) {
		val bonusPercent = bonus.toString().plus("%")
		label = context.getString(R.string.buy_airitme_with_discount, bonusPercent)
	}
	return label
}

private fun getAirtimeButtonIcon(bonus: Int) : Int? {
	var icon : Int? = null
	if (bonus > 0) { icon = R.drawable.ic_bonus }
	return icon
}

private fun emailStax(simWithAccount: SimWithAccount, context: Context) {
	val emailBody = context.getString(
		R.string.sim_card_support_request_emailBody,
		simWithAccount.sim.osReportedHni ?: "Null",
		simWithAccount.sim.operatorName ?: simWithAccount.account.alias,
		simWithAccount.sim.networkOperator ?: "Null",
		simWithAccount.sim.countryIso ?: "Null"
	)

	Utils.openEmail(R.string.sim_card_support_request_emailSubject, context, emailBody)
}

@Composable
private fun SimItemTopRow(
	simWithAccount: SimWithAccount,
	refreshBalance: (Account) -> Unit,
) {
	val size34 = dimensionResource(id = R.dimen.margin_34)

	Row(modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.margin_13)),
		verticalAlignment = Alignment.CenterVertically

	) {
		AsyncImage(
			model = ImageRequest.Builder(LocalContext.current).data(simWithAccount.account.logoUrl).crossfade(true)
				.diskCachePolicy(CachePolicy.ENABLED).build(),
			contentDescription = simWithAccount.account.alias + " logo",
			placeholder = painterResource(id = R.drawable.img_placeholder),
			error = painterResource(id = R.drawable.img_placeholder),
			modifier = Modifier
				.size(size34)
				.clip(CircleShape)
				.align(Alignment.CenterVertically),
			contentScale = ContentScale.Crop
		)

		Column(
			modifier = Modifier
				.padding(horizontal = 13.dp)
				.weight(1f)
		) {
			Text(text = simWithAccount.account.alias, style = MaterialTheme.typography.body1)
			Text(
				text = getSimSlot(simWithAccount.sim, LocalContext.current),
				color = TextGrey,
				style = MaterialTheme.typography.body2
			)
		}

		if (simWithAccount.balanceAction != null) {
			PrimaryButton(stringResource(id = R.string.check_balance_capitalized), null,
				onClick = { refreshBalance(simWithAccount.account) }
			)
		} else {
			DisabledButton(stringResource(id = R.string.unsupported), null) { }
		}

	}
}

private fun getSimSlot(simInfo: SimInfo, context: Context): String {
	return if (simInfo.slotIdx >= 0)
		context.getString(R.string.sim_index, simInfo.slotIdx + 1)
	else
		context.getString(R.string.not_present)
}

//@Composable
//@Preview
//private fun SimItemsPreview() {
//	StaxTheme {
//		Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
//			Scaffold(topBar = {
//				TopBar(title = R.string.nav_home, isInternetConnected = false, {}, {})
//			}, content = { innerPadding ->
//				Column(modifier = Modifier.padding(innerPadding)) {
//					SimItem(
//						simIndex = 2,
//						secondaryClickItem = { },
//						balanceTapListener = null
//					)
//
//					SimItem(
//						simIndex = 1,
//						account = Account.generateDummy("MTN Nigeria"),
//						bonus = 1,
//						secondaryClickItem = { },
//						balanceTapListener = null
//					)
//
//					SimItem(
//						simIndex = -1,
//						account = Account.generateDummy("Airtel"),
//						bonus = 1,
//						secondaryClickItem = { },
//						balanceTapListener = null
//					)
//				}
//			})
//		}
//	}
//}