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

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.hover.stax.R
import com.hover.stax.domain.model.USSD_TYPE
import com.hover.stax.domain.use_case.ActionableAccount
import com.hover.stax.presentation.add_accounts.components.SampleAccountProvider
import com.hover.stax.presentation.components.Logo
import com.hover.stax.presentation.components.TimeStringGenerator

@Composable
fun BalanceItem(account: ActionableAccount, goToDetail: (ActionableAccount) -> Unit, refresh: (ActionableAccount) -> Unit) {
    val size13 = dimensionResource(id = R.dimen.margin_13)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { goToDetail(account) }
            .padding(13.dp)
    ) {
        if (account.account.type == USSD_TYPE)
            Logo(account.account.logoUrl, "Account Institution Logo")
        else {
            Icon(
                painterResource(R.drawable.stellar_logo),
                contentDescription = "Stellar USDC logo",
                modifier = Modifier.height(34.dp).padding(5.dp).align(Alignment.CenterVertically)
            )
        }

        Text(
            text = account.account.userAlias,
            style = MaterialTheme.typography.body2,
            modifier = Modifier
                .weight(1f)
                .padding(start = size13)
                .align(Alignment.CenterVertically),
            color = colorResource(id = R.color.white)
        )

        Column(modifier = Modifier.align(Alignment.CenterVertically)) {
            Text(
                text = account.account.latestBalance ?: " - ",
                modifier = Modifier.align(Alignment.End),
                style = MaterialTheme.typography.subtitle2,
                color = colorResource(id = R.color.offWhite)
            )

            Spacer(modifier = Modifier.height(2.dp))

            if (account.account.latestBalance != null)
                Text(
                    text = TimeStringGenerator(account.account.latestBalanceTimestamp),
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
                .clickable { refresh(account) }
                .size(32.dp)
        )
    }

//        Divider(
//            color = colorResource(id = R.color.nav_grey),
//            modifier = Modifier.padding(horizontal = 13.dp)
//        )
}

@Preview
@Composable
fun USSDBalanceItemPreview(@PreviewParameter(SampleAccountProvider::class) accounts: List<ActionableAccount>) {
    BalanceItem(accounts[0], {}, {})
}

@Preview
@Composable
fun USDCBalanceItemPreview(@PreviewParameter(SampleAccountProvider::class) accounts: List<ActionableAccount>) {
    BalanceItem(accounts[3], {}, {})
}