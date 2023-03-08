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
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hover.stax.R
import com.hover.stax.domain.model.FinancialTip
import com.hover.stax.presentation.components.StaxCard
import com.hover.stax.presentation.financial_tips.FinancialTipsViewModel
import com.hover.stax.presentation.home.HomeFragmentDirections
import com.hover.stax.utils.Utils
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FinancialTipScreen(viewModel: FinancialTipsViewModel, navTo: (dest: Int) -> Unit) {

    val context = LocalContext.current
    val tips by viewModel.tips.observeAsState(initial = emptyList())
    val showingTip = remember { mutableStateOf(true) }

    tips?.find {financialTipsAreOn() && !dismissed(it.id, showingTip, context)}?.apply {
        FinancialTipCard(this, showingTip, navTo, context)
    }
}

fun financialTipsAreOn(): Boolean {
    return true
}

fun isToday(secondTimestamp: Long): Boolean {
    val calendar1: Calendar = Calendar.getInstance()
    calendar1.timeInMillis = secondTimestamp * 1000
    val calendar2: Calendar = Calendar.getInstance()
    calendar2.timeInMillis = System.currentTimeMillis()
    return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) &&
            calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH) &&
            calendar1.get(Calendar.DAY_OF_MONTH) == calendar2.get(Calendar.DAY_OF_MONTH)
}

fun dismissed(id: String, showingTip: MutableState<Boolean>, context: Context): Boolean {
    return Utils.dismissedTips(context).contains(id) || !showingTip.value
}

@Composable
fun FinancialTipCard(financialTip: FinancialTip, showingTip: MutableState<Boolean>, navTo: (dest: Int) -> Unit, context: Context) {
    val size13 = dimensionResource(id = R.dimen.margin_13)
    StaxCard {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = size13)
            ) {
                Icon(painterResource(R.drawable.ic_tip_of_day), contentDescription = "Tip icon")

                Text(stringResource(R.string.tip_of_the_day),
                    modifier = Modifier.weight(1f).padding(horizontal = 13.dp))

                Icon(painterResource(id = R.drawable.ic_close_white),
                    contentDescription = "close tip",
                    modifier = Modifier.clickable { dismissTip(financialTip.id, showingTip, context) }
                )
            }

            Row(modifier = Modifier.padding(horizontal = size13)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = financialTip.title,
                        style = MaterialTheme.typography.body2,
                        textDecoration = TextDecoration.Underline
                    )

                    Text(
                        text = financialTip.snippet,
                        style = MaterialTheme.typography.body2,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(vertical = size13)
                    )

                    Text(
                        text = stringResource(id = R.string.read_more),
                        color = colorResource(id = R.color.brightBlue),
                        modifier = Modifier
                            .padding(bottom = size13)
                            .clickable { visitTip(financialTip.id, navTo) }
                    )
                }

                Image(
                    painter = painterResource(id = R.drawable.tips_fancy_icon),
                    contentDescription = null,
                    modifier = Modifier
                        .size(89.dp)
                        .padding(start = size13),
                )
            }
        }
    }
}

fun visitTip(id: String, navTo: (dest: Int) -> Unit) {
    navTo(R.id.action_navigation_home_to_wellnessFragment)
//    navController.navigate(HomeFragmentDirections.actionNavigationHomeToWellnessFragment().setTipId(id))
}

fun dismissTip(id: String, showingTip: MutableState<Boolean>, context: Context) {
    Utils.dismissTip(id, context)
    showingTip.value = false
}