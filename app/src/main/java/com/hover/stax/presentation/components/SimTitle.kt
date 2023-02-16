package com.hover.stax.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.hover.sdk.sims.SimInfo
import com.hover.stax.R
import com.hover.stax.domain.use_case.sims.SimWithAccount
import com.hover.stax.ui.theme.TextGrey

@Composable
fun SimTitle(
	simWithAccount: SimWithAccount,
	content: @Composable () -> Unit,
) {
	Row(
		modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.margin_13)),
		verticalAlignment = Alignment.CenterVertically

	) {
		Logo(simWithAccount.account.logoUrl, simWithAccount.account.userAlias + " logo")
		Column(
			modifier = Modifier
				.padding(horizontal = 13.dp)
				.weight(1f)
		) {
			Text(text = simWithAccount.account.userAlias, style = MaterialTheme.typography.body1)
			Text(
				text = getSimSlot(simWithAccount.sim),
				color = TextGrey,
				style = MaterialTheme.typography.body2
			)
		}

		content()
	}
}

@Composable
private fun getSimSlot(simInfo: SimInfo): String {
	return if (simInfo.slotIdx >= 0)
		stringResource(id = R.string.sim_index, simInfo.slotIdx + 1)
	else
		stringResource(R.string.not_present)
}