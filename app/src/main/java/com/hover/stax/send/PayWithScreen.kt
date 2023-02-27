package com.hover.stax.send

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import com.hover.stax.R
import com.hover.stax.domain.model.Account
import com.hover.stax.views.composables.StaxHeader
import com.hover.stax.views.composables.StaxImage
import com.hover.stax.views.composables.StaxLayout
import com.hover.stax.views.composables.StaxModalCell

@Composable
fun PayWithScreen(
    onClickBack: () -> Unit,
    accounts: List<Account>,
    onAccountSelected: (Account) -> Unit
) {

    StaxLayout(
        title = {
            StaxHeader(
                text = "Pay with",
                onClickBack = onClickBack,
                modifier = Modifier.testTag(PayWithScreenTags.PAY_WITH_SCREEN_HEADER)
            )
        },
        content = {
            items(accounts) { account ->
                StaxModalCell(
                    modifier = Modifier
                        .testTag(PayWithScreenTags.PAY_WITH_SCREEN_ACCOUNT_CELL)
                        .heightIn(min = 70.dp),
                    onClick = { onAccountSelected(account) },
                    header = account.institutionName,
                    subHeader = account.latestBalance ?: "0.00",
                    footer = account.isDefault.toString(),
                    leftIcon = {
                        StaxImage(
                            imageUrl = account.logoUrl,
                            modifier = Modifier
                                .size(dimensionResource(id = R.dimen.margin_34))
                                .clip(CircleShape)
                        )
                    }
                )
            }
        },
        footer = {
            // add button here
        }
    )
}

object PayWithScreenTags {
    const val PAY_WITH_SCREEN_HEADER = "pay_with_screen_header"
    const val PAY_WITH_SCREEN_ACCOUNT_CELL = "pay_with_screen_account_cell"
}