package com.hover.stax.presentation.transactions

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.hover.stax.R
import com.hover.stax.presentation.home.components.HomeTopBar
import com.hover.stax.transactions.TransactionHistoryItem
import com.hover.stax.transactions.TransactionHistoryViewModel
import com.hover.stax.utils.DateUtils
import org.koin.androidx.compose.getViewModel

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun TransactionHistoryScreen(viewModel: TransactionHistoryViewModel = getViewModel()) {
    val transactions by viewModel.transactionHistoryItems.observeAsState()

    Scaffold(topBar = {
        HomeTopBar(title = R.string.transactions_cardhead, { })
    }) {
        TransactionHistoryList(transactions)
    }
}

@Composable
fun TransactionHistoryList(transactions: List<TransactionHistoryItem>?) {
    if (!transactions.isNullOrEmpty()) {
        LazyColumn(modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.margin_13))) {
            itemsIndexed(transactions) { i, t ->
                if (i == 0 || DateUtils.humanFriendlyDate(t.staxTransaction.initiated_at) !=
                    DateUtils.humanFriendlyDate(transactions[i - 1].staxTransaction.initiated_at)
                ) {
                    Text(DateUtils.humanFriendlyDate(t.staxTransaction.initiated_at))
                }
                TransactionListItem(t = t) {}
            }
        }
    }
}