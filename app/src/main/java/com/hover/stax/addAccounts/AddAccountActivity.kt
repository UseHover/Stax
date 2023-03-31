package com.hover.stax.addAccounts

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.channels.UpdateChannelsWorker
import com.hover.stax.domain.model.USSDAccount
import com.hover.stax.hover.HoverSession
import com.hover.stax.hover.TransactionContract
import com.hover.stax.presentation.add_accounts.AddAccountNavHost
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.UIHelper
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class AddAccountActivity : AppCompatActivity() {

    private val viewModel: AddAccountViewModel by viewModel()
    private val usdcViewModel: UsdcViewModel by viewModel()

    @ExperimentalAnimationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        refreshChannels()
        AnalyticsUtil.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_link_account)), this)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.checkBalanceEvent.collect {
                        callHover(checkBalance, generateSessionBuilder(it.first, it.second))
                    } 
                }
                launch {
                    viewModel.doneEvent.collect { isDone ->
                        if (isDone) { finish() }
                    } 
                }
                launch {
                    usdcViewModel.downloadEvent.collect {
                        if (it != null) chooseFileLocation()
                    } 
                }
                launch {
                    usdcViewModel.doneEvent.collect { isDone ->
                        if (isDone) { finish() }
                    } 
                }
            }
        }

        setContent {
            AddAccountNavHost()
        }
    }

    private fun generateSessionBuilder(account: USSDAccount, action: HoverAction): HoverSession.Builder {
        return HoverSession.Builder(action, account, this)
    }

    private val checkBalance = registerForActivityResult(TransactionContract()) { data: Intent? ->
        Timber.e("got check balance result")
        if (data != null && data.extras != null && data.extras!!.getString("uuid") != null) {
//            NavUtil.showTransactionDetailsFragment(navController, data.extras!!.getString("uuid")!!)
            finish()
        }
    }

    private fun callHover(launcher: ActivityResultLauncher<HoverSession.Builder>, b: HoverSession.Builder) {
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
            AnalyticsUtil.logErrorAndReportToFirebase(b.action.public_id, getString(R.string.error_running_action_log), e)
        }
    }

    private fun chooseFileLocation() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_TITLE, "usdcSecretKey.txt")
        }
        startActivityForResult(intent, 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0) {
            data?.data?.let {
                try {
                    val outputStream = contentResolver.openOutputStream(it) ?: return
                    Timber.e("Secret should be ${usdcViewModel.secret.value}")
                    outputStream.write(usdcViewModel.secret.value!!.toByteArray(charset("UTF-8")))
                    outputStream.close()
                    Toast.makeText(this, R.string.key_downloaded, Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(this, R.string.key_download_error, Toast.LENGTH_LONG).show()
                    Timber.e("Something went wrong", e)
                }
            }
        }
    }

    private fun refreshChannels() {
        val wm = WorkManager.getInstance(this)
        wm.beginUniqueWork(
            UpdateChannelsWorker.CHANNELS_WORK_ID, ExistingWorkPolicy.KEEP,
            UpdateChannelsWorker.makeWork()
        ).enqueue()
    }
}