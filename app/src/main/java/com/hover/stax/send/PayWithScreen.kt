package com.hover.stax.send

import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
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
                text = "Select Payment Type",
                onClickBack = onClickBack
            )
        },
        content = {
            items(accounts) { account ->
                StaxModalCell(
                    onClick = { onAccountSelected(account) },
                    header = account.institutionName,
                    subHeader = account.latestBalance ?: "0.00",
                    footer = account.isDefault.toString(),
                    leftIcon = {
                        StaxImage(imageUrl = account.logoUrl)
                    }
                )
            }
        },
        footer = {
            // add button here
        }
    )
}