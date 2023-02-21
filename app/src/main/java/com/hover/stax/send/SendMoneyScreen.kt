package com.hover.stax.send

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.hover.stax.R
import com.hover.stax.ui.theme.StaxTheme
import com.hover.stax.views.composables.AmountTextField
import com.hover.stax.views.composables.SendMoneyOption

@Composable
fun SendMoneyScreen(
    navTo: () -> Unit
) {

    val amountText = ""
    val account = ""

    StaxTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
            Scaffold(
                topBar = {
//                    NavTopBar(title = R.string.nav_send_money, navTo)
                }
            ) { padding ->
                ConstraintLayout(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                ) {
                    val (amountField, accountField, numKeypad, buttons) = createRefs()

                    AmountTextField(modifier = Modifier.constrainAs(amountField) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        height = Dimension.fillToConstraints
                    }, amount = amountText)

                    SendMoneyOption(modifier = Modifier.constrainAs(accountField) {
                        top.linkTo(amountField.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        height = Dimension.fillToConstraints
                    }, selectedAccount = {
                        // Open Bottom Sheet
                        navTo.invoke()
                    })

//                    AmountKeyboard(
//                        modifier = Modifier.constrainAs(numKeypad) {
//                            top.linkTo(accountField.bottom)
//                            start.linkTo(parent.start)
//                            end.linkTo(parent.end)
//                            bottom.linkTo(buttons.top)
//                            height = Dimension.fillToConstraints
//                        }
//                    ) {
////                        viewModel.onKeyboardButtonClick(it)
//                    }

                    Row(modifier = Modifier.constrainAs(buttons) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                    }) {
                        Button(
                            onClick = {

                            }, modifier = Modifier
                                .weight(1f)
                                .padding(padding)
                        ) {
                            Text(
                                text = stringResource(id = R.string.payment_type_screen_cancel),
                                color = Color.White,
                                fontSize = 28.sp
                            )
                        }
                        Button(
                            onClick = {

                            }, modifier = Modifier
                                .weight(1f)
                                .padding(padding)
                        ) {
                            Text(
                                text = stringResource(id = R.string.payment_type_screen_next),
                                color = Color.White,
                                fontSize = 28.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

object SendMoneyScreenTags {
    const val TAG_AMOUNT_FIELD = "amount_field"
    const val TAG_ACCOUNT_FIELD = "account_field"
    const val TAG_NUM_KEYPAD = "num_keypad"
    const val TAG_CANCEL_BUTTON = "cancel_button"
    const val TAG_NEXT_BUTTON = "next_button"
}

@Preview
@Composable
fun SendMoneyScreenPreview() {
    SendMoneyScreen {}
}