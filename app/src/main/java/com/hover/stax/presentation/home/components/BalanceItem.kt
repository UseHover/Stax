/*
 * Copyright 2022 Stax
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hover.stax.presentation.home.components

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
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
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.hover.stax.R
import com.hover.stax.database.models.Account
import com.hover.stax.presentation.home.BalanceTapListener
import com.hover.stax.core.DateUtils

@Composable
fun BalanceItem(staxAccount: Account, balanceTapListener: BalanceTapListener?, context: Context) {
    val size34 = dimensionResource(id = R.dimen.margin_34)
    val size13 = dimensionResource(id = R.dimen.margin_13)
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 13.dp)
                .heightIn(min = 70.dp)
                .clickable { balanceTapListener?.onTapBalanceDetail(accountId = staxAccount.id) }
        ) {

            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(staxAccount.logoUrl)
                    .crossfade(true)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build(),
                contentDescription = "",
                placeholder = painterResource(id = R.drawable.img_placeholder),
                error = painterResource(id = R.drawable.img_placeholder),
                modifier = Modifier
                    .size(size34)
                    .clip(CircleShape)
                    .align(Alignment.CenterVertically),
                contentScale = ContentScale.Crop
            )

            Text(
                text = staxAccount.userAlias,
                style = MaterialTheme.typography.body2,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = size13)
                    .align(Alignment.CenterVertically),
                color = colorResource(id = R.color.white)
            )

            Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                Text(
                    text = staxAccount.latestBalance ?: " - ",
                    modifier = Modifier.align(Alignment.End),
                    style = MaterialTheme.typography.subtitle2,
                    color = colorResource(id = R.color.offWhite)
                )

                Spacer(modifier = Modifier.height(2.dp))

                if (staxAccount.latestBalance != null)
                    Text(
                        text = com.hover.stax.core.DateUtils.timeAgo(context, staxAccount.latestBalanceTimestamp),
                        modifier = Modifier.align(Alignment.End),
                        color = colorResource(id = R.color.offWhite),
                        style = MaterialTheme.typography.caption
                    )
            }

            Image(
                painter = painterResource(id = R.drawable.ic_refresh_white_24dp),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(start = size13)
                    .clickable { balanceTapListener?.onTapBalanceRefresh(staxAccount) }
                    .size(32.dp)
            )
        }

        Divider(
            color = colorResource(id = R.color.nav_grey),
            modifier = Modifier.padding(horizontal = 13.dp)
        )
    }
}