package com.hover.stax.presentation.send_money

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.accounts.AccountsViewModel
import com.hover.stax.domain.model.USSDAccount
import com.hover.stax.hover.HoverSession
import com.hover.stax.hover.TransactionContract
import com.hover.stax.presentation.home.BalancesViewModel
import com.hover.stax.transfers.TransferViewModel
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.UIHelper
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class SendMoneyActivity : AppCompatActivity() {

    val accountsViewModel: AccountsViewModel by viewModel()
    private val viewModel: TransferViewModel by viewModel()
    private val balancesViewModel: BalancesViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    accountsViewModel.activeAccount.observe(this@SendMoneyActivity) {

                    }
                }
            }
        }

        setContent {
            SendMoneyNavHost()
        }
    }

//    private fun generateSessionBuilder(): HoverSession.Builder {
//        return HoverSession.Builder(
//            actionSelectViewModel.activeAction.value!!,
//            payWithDropdown.getHighlightedAccount() ?: accountsViewModel.activeAccount.value!!,
//            getExtras(), this
//        )
//    }

    private val transfer = registerForActivityResult(TransactionContract()) { data: Intent? ->

    }

    private fun callHover(
        launcher: ActivityResultLauncher<HoverSession.Builder>,
        b: HoverSession.Builder
    ) {
        try {
            launcher.launch(b)
        } catch (e: Exception) {
            runOnUiThread {
                UIHelper.flashAndReportMessage(
                    this,
                    getString(
                        R.string.error_running_action
                    )
                )
            }
            AnalyticsUtil.logErrorAndReportToFirebase(
                b.action.public_id,
                getString(R.string.error_running_action_log),
                e
            )
        }
    }

    private fun checkBalance(account: USSDAccount, action: HoverAction) {
        val b = generateSessionBuilder(account, action)
        callHover(checkBalance, b)
    }
}