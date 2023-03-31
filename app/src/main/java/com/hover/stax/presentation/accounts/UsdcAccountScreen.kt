package com.hover.stax.presentation.accounts

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.hover.stax.R
import com.hover.stax.accounts.AccountDetailViewModel
import com.hover.stax.addAccounts.UsdcViewModel
import com.hover.stax.domain.use_case.AccountDetail
import com.hover.stax.presentation.add_accounts.*
import com.hover.stax.presentation.components.*
import com.hover.stax.presentation.home.components.BalanceItem
import com.hover.stax.presentation.home.components.HomeTopBar
import com.hover.stax.presentation.transactions.TransactionHistoryList
import com.hover.stax.ui.theme.StaxTheme
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel

const val DETAILS = "details"
const val DOWNLOAD_KEY = "download_key"

@Composable
fun UsdcAccountScreen(
    viewModel: AccountDetailViewModel = getViewModel(),
    usdcVM: UsdcViewModel = getViewModel(),
    navController: NavController = rememberNavController()
) {

    val account by viewModel.account.collectAsState()

    val enterPin = remember { mutableStateOf("") }
    val currentPage = remember { mutableStateOf(DETAILS) }
    val errorMessage = remember { mutableStateOf(0) }

    StaxTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
            Crossfade(targetState = currentPage.value) { page ->
                when (page) {
                    DETAILS -> UsdcAccountDetailsScreen(account, viewModel, usdcVM, currentPage) { navController.popBackStack() }
                    DOWNLOAD_KEY -> PinEntryScreen(
                        R.string.enter_stellar_pin, enterPin, R.string.download_key, errorMessage,
                        doneAction = {
                            val secret = usdcVM.decryptSecret(account!!.usdcAccount!!, enterPin.value)
                            if (secret != null) {
                                usdcVM.downloadKey(secret)
                                currentPage.value = DETAILS
                            } else {
                                enterPin.value = ""
                                errorMessage.value = R.string.usdc_pin_error
                            }
                        },
                        backAction = { currentPage.value = DETAILS }
                    )
                }
            }
        }
    }
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun UsdcAccountDetailsScreen(
    account: AccountDetail?,
    viewModel: AccountDetailViewModel = getViewModel(),
    usdcVM: UsdcViewModel = getViewModel(),
    currentPage: MutableState<String>,
    onBack: () -> Unit
) {

    var nicknameValue by remember { mutableStateOf(TextFieldValue(account?.account?.userAlias ?: "")) }
    var nicknameUpdated by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val showingRemove = remember { mutableStateOf(false) }

    val context = LocalContext.current

    account?.let {
        Scaffold(
            topBar = { HomeTopBar(it.account.userAlias, null) }
        ) { _ ->
            Column(
                modifier = Modifier
                    .padding(horizontal = 13.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(21.dp)
            ) {
                BalanceItem(account = it.account, goToDetail = {}, refresh = {
                    Toast.makeText(context, R.string.refresh_usdc_balance, Toast.LENGTH_LONG).show()
                    usdcVM.updateBalances(it.usdcAccount!!)
                })

                HeaderText(stringResource(id = R.string.transaction_history))
                if (it.transactions.isNullOrEmpty())
                    Text(stringResource(id = R.string.hist_zerobody))
                else
                    TransactionHistoryList(it.transactions)
                Spacer(modifier = Modifier.height(5.dp))

                HeaderText(stringResource(id = R.string.nav_account_detail))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        stringResource(id = R.string.account_number_label),
                        modifier = Modifier.padding(end = 13.dp)
                    )
                    it.account.accountNo?.let { it1 -> Text(it1) }
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        stringResource(id = R.string.currency_literal),
                        modifier = Modifier.padding(end = 13.dp)
                    )
                    it.usdcAccount?.assetType?.let { it1 -> Text(it1) }
                }
                Spacer(modifier = Modifier.height(5.dp))

                HeaderText(stringResource(id = R.string.manage_account))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StaxTextField(
                        textField = nicknameValue,
                        placeholder = R.string.nickname
                    ) { tfv ->
                        nicknameValue = tfv
                        nicknameUpdated = it.account.userAlias != tfv.text
                    }
                    StaxButton(
                        stringResource(id = R.string.btn_save),
                        modifier = Modifier.padding(start = 13.dp),
                        buttonType = if (nicknameUpdated) PRIMARY else DISABLED
                    ) {
                        viewModel.updateAccountName(nicknameValue.text)
                        nicknameUpdated = false
                    }
                }
                PrimaryButton(text = stringResource(R.string.usdc_account_2_action)) {
                    currentPage.value = DOWNLOAD_KEY
                }

                Spacer(modifier = Modifier.height(13.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DestructiveButton(stringResource(id = R.string.btn_removeaccount)) {
                        showingRemove.value = true
                    }
                }
            }
            showRemove(it, showingRemove, viewModel, onBack)
            BackHandler(showingRemove.value) {
                coroutineScope.launch { showingRemove.value = false }
            }
        }
    }
}

@Composable
fun showRemove(account: AccountDetail, showingRemove: MutableState<Boolean>, viewModel: AccountDetailViewModel, onRemove: () -> Unit) {
    if (showingRemove.value) {
        AlertDialog(
            title = { Text(stringResource(R.string.removeaccount_dialoghead, account.account.userAlias)) },
            text = { Text(stringResource(R.string.removeaccount_msg)) },
            buttons = {
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(13.dp)
                ) {
                    SecondaryButton(stringResource(R.string.btn_cancel), onClick = { showingRemove.value = false })
                    DestructiveButton(stringResource(R.string.btn_removeaccount), modifier = Modifier.padding(start = 13.dp)) {
                        viewModel.removeAccount(account.usdcAccount!!)
                        showingRemove.value = false
                        onRemove()
                    }
                } 
            },
            onDismissRequest = { showingRemove.value = false }
        )
    }
}

@Composable
fun HeaderText(text: String) {
    Text(text.uppercase(), style = HeaderTextStyle(), modifier = Modifier.fillMaxWidth())
}

@Composable
fun HeaderTextStyle() = TextStyle(color = colorResource(id = R.color.offWhite), fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)