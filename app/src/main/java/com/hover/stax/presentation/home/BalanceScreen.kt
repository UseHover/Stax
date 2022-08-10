package com.hover.stax.presentation.home

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.hover.stax.R
import com.hover.stax.domain.model.Account
import com.hover.stax.ui.theme.ColorSurface
import com.hover.stax.ui.theme.DarkGray
import com.hover.stax.ui.theme.OffWhite
import com.hover.stax.ui.theme.StaxTheme
import com.hover.stax.utils.DateUtils


interface BalanceTapListener {
    fun onTapBalanceRefresh(account: Account?)
    fun onTapBalanceDetail(accountId: Int)
}

@Composable
fun BalanceHeader(onClickedAddAccount: () -> Unit, accountExists: Boolean) {
    val size13 = dimensionResource(id = R.dimen.margin_13)

    Row(
        modifier = Modifier
            .padding(all = size13)
            .fillMaxWidth()
    ) {
        Text(
            text = stringResource(id = R.string.your_accounts),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.h4
        )

        if (accountExists) {
            Text(
                text = stringResource(id = R.string.add_an_account),
                style = MaterialTheme.typography.body2,
                modifier = Modifier
                    .clickable(onClick = onClickedAddAccount)
                    .padding(end = 5.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Icon(
                painter = painterResource(id = R.drawable.ic_add_white_16),
                contentDescription = null,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onClickedAddAccount)
                    .background(color = colorResource(id = R.color.brightBlue))
            )
        }
    }
}

@Composable
fun EmptyBalance(onClickedAddAccount: () -> Unit) {
    val size34 = dimensionResource(id = R.dimen.margin_34)
    val size16 = dimensionResource(id = R.dimen.margin_16)
    Column(modifier = Modifier.padding(horizontal = size34, vertical = size16)) {
        Text(
            text = stringResource(id = R.string.empty_balance_desc),
            style = MaterialTheme.typography.body1,
            color = colorResource(id = R.color.offWhite),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onClickedAddAccount,
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 0.dp),
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(width = 0.5.dp, color = DarkGray),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = ColorSurface,
                contentColor = OffWhite
            )
        ) {
            Text(
                text = stringResource(id = R.string.add_account),
                style = MaterialTheme.typography.button,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp, bottom = 5.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun BalanceItem(staxAccount: Account, balanceTapListener: BalanceTapListener?, context: Context) {
    val size34 = dimensionResource(id = R.dimen.margin_34)
    val size13 = dimensionResource(id = R.dimen.margin_13)
    Column {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 13.dp)
            .heightIn(min = 70.dp)
            .clickable { balanceTapListener?.onTapBalanceDetail(accountId = staxAccount.id) }) {

            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(staxAccount.logoUrl)
                    .crossfade(true)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build(),
                contentDescription = "",
                placeholder = painterResource(id = R.drawable.image_placeholder),
                error = painterResource(id = R.drawable.ic_stax),
                modifier = Modifier
                    .size(size34)
                    .clip(CircleShape)
                    .align(Alignment.CenterVertically),
                contentScale = ContentScale.Crop
            )

            Text(
                text = staxAccount.alias,
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

                Text(
                    text = DateUtils.timeAgo(context, staxAccount.latestBalanceTimestamp),
                    modifier = Modifier.align(Alignment.End),
                    color = colorResource(id = R.color.offWhite),
                    style = MaterialTheme.typography.caption
                )
            }

            Image(painter = painterResource(id = R.drawable.ic_refresh_white_24dp),
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

@Preview
@Composable
fun BalanceScreenPreview() {
    StaxTheme {
        Surface {
            Column(modifier = Modifier.background(color = colors.background)) {
                BalanceHeader(onClickedAddAccount = {}, accountExists = false)
                BalanceListForPreview(accountList = emptyList())
            }
        }
    }
}

@Composable
private fun BalanceListForPreview(accountList: List<Account>) {

    if (accountList.isEmpty()) {
        EmptyBalance {}
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(13.dp)
        ) {
            items(accountList) { account ->
                val context = LocalContext.current
                BalanceItem(staxAccount = account, context = context, balanceTapListener = null)
            }
        }
    }
}

