package com.hover.stax.presentation.sim.components

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.hover.stax.R
import com.hover.stax.domain.model.Account
import com.hover.stax.presentation.home.BalanceTapListener
import com.hover.stax.presentation.home.components.TopBar
import com.hover.stax.ui.theme.BrightBlue
import com.hover.stax.ui.theme.ColorPrimary
import com.hover.stax.ui.theme.ColorSurface
import com.hover.stax.ui.theme.DarkGray
import com.hover.stax.ui.theme.OffWhite
import com.hover.stax.ui.theme.StaxTheme
import com.hover.stax.ui.theme.TextGrey
import com.hover.stax.utils.DateUtils

private fun isSimSupportedAccount(accountId: Int): Boolean = accountId >= 0

@Composable
internal fun SimItem(
	simIndex: Int,
	account: Account,
	bonusPercent: Int,
	secondaryClickItem: () -> Unit,
	balanceTapListener: BalanceTapListener?
) {
	val size13 = dimensionResource(id = R.dimen.margin_13)

	Box(
		modifier = Modifier
			.fillMaxWidth()
			.padding(vertical = size13)
			.shadow(elevation = 0.dp)
			.border(width = 1.dp, color = DarkGray, shape = RoundedCornerShape(5.dp))
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(size13)
		) {
			SimItemTopRow(
				simIndex = simIndex, account = account, balanceTapListener = balanceTapListener
			)
			Column {
				val notYetChecked = stringResource(id = R.string.not_yet_checked)
				Text(
					text = stringResource(
						id = R.string.airtime_balance_holder, account.latestBalance ?: notYetChecked
					),
					color = TextGrey,
					modifier = Modifier.padding(top = size13),
					style = MaterialTheme.typography.body1
				)

				Text(
					text = stringResource(
						id = R.string.as_of,
						DateUtils.humanFriendlyDateTime(account.latestBalanceTimestamp)
					),
					color = TextGrey,
					modifier = Modifier.padding(bottom = 26.dp),
					style = MaterialTheme.typography.body1
				)

				if (!isSimSupportedAccount(account.id)) {
					Text(
						text = stringResource(
							id = R.string.unsupported_sim_info
						),
						color = TextGrey,
						modifier = Modifier.padding(bottom = 7.dp),
						style = MaterialTheme.typography.body2
					)
				}

				OutlinedButton(
					onClick = secondaryClickItem,
					modifier = Modifier
						.padding(bottom = 6.dp)
						.shadow(elevation = 0.dp)
						.wrapContentWidth(),
					shape = MaterialTheme.shapes.medium,
					border = BorderStroke(width = 0.5.dp, color = DarkGray),
					colors = ButtonDefaults.buttonColors(
						backgroundColor = ColorSurface, contentColor = OffWhite
					)
				) {
					Row(
						modifier = Modifier
							.wrapContentWidth()
							.padding(all = 5.dp)
					) {
						Text(
							text = getSecondaryButtonLabel(account.id, bonusPercent, LocalContext.current),
							style = MaterialTheme.typography.button,
							modifier = Modifier.padding(end = 5.dp),
							textAlign = TextAlign.Start,
							fontSize = 14.sp
						)

						if (isSimSupportedAccount(account.id) && bonusPercent > 0) {
							Spacer(modifier = Modifier.width(3.dp))

							Image(
								painter = painterResource(id = R.drawable.ic_bonus),
								contentDescription = null,
								modifier = Modifier.size(18.dp)
							)
						}
					}
				}
			}
		}
	}
}

private fun getSecondaryButtonLabel(accountId: Int, bonus: Int, context: Context): String {
	var label = context.getString(R.string.email_support)
	if (isSimSupportedAccount(accountId)) {
		label = context.getString(R.string.nav_airtime)
		if (bonus > 0) {
			val bonusPercent = bonus.toString().plus("%")
			label = context.getString(R.string.buy_airitme_with_discount, bonusPercent)
		}
	}
	return label
}

@Composable
private fun SimItemTopRow(
	simIndex: Int,
	account: Account,
	balanceTapListener: BalanceTapListener?
) {
	val size34 = dimensionResource(id = R.dimen.margin_34)

	Row {
		AsyncImage(
			model = ImageRequest.Builder(LocalContext.current).data(account.logoUrl).crossfade(true)
				.diskCachePolicy(CachePolicy.ENABLED).build(),
			contentDescription = "",
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
				.weight(1f)
				.padding(horizontal = 13.dp)
		) {
			Text(text = account.name, style = MaterialTheme.typography.body1)
			Text(
				text = stringResource(id = R.string.sim_index, simIndex),
				color = TextGrey,
				style = MaterialTheme.typography.body2
			)
		}

		if (isSimSupportedAccount(account.id)) {
			Button(
				onClick = { balanceTapListener?.onTapBalanceRefresh(account) },
				modifier = Modifier
					.weight(1f)
					.shadow(elevation = 0.dp),
				shape = MaterialTheme.shapes.medium,
				colors = ButtonDefaults.buttonColors(
					backgroundColor = BrightBlue, contentColor = ColorPrimary
				)
			) {
				Text(
					text = stringResource(id = R.string.check_balance_capitalized),
					style = MaterialTheme.typography.body2,
					modifier = Modifier.fillMaxWidth(),
					textAlign = TextAlign.Center
				)
			}
		} else {
			Text(
				text = stringResource(id = R.string.unsupported),
				style = MaterialTheme.typography.body2,
				modifier = Modifier
					.weight(1f)
					.align(Alignment.CenterVertically),
				textAlign = TextAlign.End
			)
		}

	}
}

@Composable
@Preview
private fun SimItemsPreview() {
	StaxTheme {
		Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
			Scaffold(topBar = {
				TopBar(title = R.string.nav_home, isInternetConnected = false, {}, {})
			}, content = { innerPadding ->
				Column(modifier = Modifier.padding(innerPadding)) {
					SimItem(
						simIndex = 1,
						account = Account.generateDummy("Dummy Account", 1),
						bonusPercent = 1,
						secondaryClickItem = { },
						balanceTapListener = null
					)

					SimItem(
						simIndex = 1,
						account = Account.generateDummy("MTN Nigeria"),
						bonusPercent = 1,
						secondaryClickItem = { },
						balanceTapListener = null
					)
				}
			})
		}
	}
}