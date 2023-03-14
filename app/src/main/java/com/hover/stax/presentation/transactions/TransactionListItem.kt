package com.hover.stax.presentation.transactions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hover.stax.transactions.TransactionHistoryItem

@Composable
fun TransactionListItem(transaction: TransactionHistoryItem) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
//			.clickable { goToDetail() }
			.padding(13.dp)
	) {

	}
}