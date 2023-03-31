package com.hover.stax.presentation.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.hover.stax.transactions.StaxTransaction
import com.hover.stax.transactions.TransactionHistoryItem

@Composable
fun TransactionListItem(t: TransactionHistoryItem, goToDetail: (StaxTransaction) -> Unit) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { goToDetail(t.staxTransaction) }
            .padding(13.dp)
            .background(t.staxTransaction.getBackgroundColor())
    ) {
        Column(modifier = Modifier.align(Alignment.CenterVertically)) {
            Text(t.staxTransaction.transaction_type)
            Text(t.staxTransaction.shortStatusExplain(t.action, t.institutionName, context))
        }
        Text(
            t.staxTransaction.getSignedAmount(t.staxTransaction.amount) ?: "",
            style = TextStyle(textDecoration = if (t.staxTransaction.isFailed) TextDecoration.LineThrough else TextDecoration.None),
            modifier = Modifier.alpha(if (t.staxTransaction.isFailed) 0.5f else 1f).align(Alignment.CenterVertically)
        )
    }
}