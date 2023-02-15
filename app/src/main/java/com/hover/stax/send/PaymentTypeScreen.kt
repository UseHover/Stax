package com.hover.stax.send

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hover.sdk.actions.HoverAction
import com.hover.stax.domain.model.Account

@Composable
fun PaymentTypeScreen(
    onClickBack: () -> Unit,
    accounts: List<Account>,
    actions: List<HoverAction>?,

) {

    Column(modifier = Modifier
        .height(300.dp)
        .background(Color.Green)) {

        val harun = actions?.let { sort(it) }

        harun?.forEach {
            Text(text = it.toString())
        }
    }
}

fun sort(actions: List<HoverAction>): List<HoverAction> = actions.distinctBy { it.to_institution_id }.toList()