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
import com.hover.stax.channels.Channel
import com.hover.stax.domain.use_case.sims.SimWithAccount
import com.hover.stax.ui.theme.TextGrey


@Composable
fun SimTitle(sim: SimInfo, title: String, logo: String?, content: @Composable () -> Unit) {
	Row(
		modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.margin_13)),
		verticalAlignment = Alignment.CenterVertically

	) {
		Logo(logo, "$title logo")
		Column(
			modifier = Modifier
				.padding(horizontal = 13.dp)
				.weight(1f)
		) {
			Text(text = title, style = MaterialTheme.typography.body1)
			Text(
				text = getSimSlot(sim),
				color = TextGrey,
				style = MaterialTheme.typography.body2
			)
		}

		content()
	}
}

@Composable
fun SimTitle(sim: SimInfo, channel: Channel?, content: @Composable () -> Unit) {
	SimTitle(sim, channel?.name ?: sim.operatorName, channel?.logoUrl, content)
}

@Composable
private fun getSimSlot(simInfo: SimInfo): String {
	return if (simInfo.slotIdx >= 0)
		stringResource(id = R.string.sim_index, simInfo.slotIdx + 1)
	else
		stringResource(R.string.not_present)
}