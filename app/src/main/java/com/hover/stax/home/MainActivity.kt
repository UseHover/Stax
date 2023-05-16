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
package com.hover.stax.home

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavDirections
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.api.Hover
import com.hover.sdk.permissions.PermissionHelper
import com.hover.stax.FRAGMENT_DIRECT
import com.hover.stax.MainNavigationDirections
import com.hover.stax.R
import com.hover.stax.databinding.ActivityMainBinding
import com.hover.stax.login.AbstractGoogleAuthActivity
import com.hover.stax.login.LoginViewModel
import com.hover.stax.login.StaxGoogleLoginInterface
import com.hover.stax.notifications.PushNotificationTopicsInterface
import com.hover.stax.presentation.bounties.BountyApplicationFragmentDirections
import com.hover.stax.presentation.financial_tips.FinancialTipsFragment
import com.hover.stax.requests.NewRequestViewModel
import com.hover.stax.requests.REQUEST_LINK
import com.hover.stax.requests.RequestSenderInterface
import com.hover.stax.requests.SMS
import com.hover.stax.settings.BiometricChecker
import com.hover.stax.transactions.TransactionHistoryViewModel
import com.hover.stax.transfers.TransferViewModel
import com.hover.stax.utils.UIHelper
import com.hover.stax.views.StaxDialog
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AbstractGoogleAuthActivity(), BiometricChecker.AuthListener, PushNotificationTopicsInterface, RequestSenderInterface {

    private lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var staxGoogleLoginInterface: StaxGoogleLoginInterface

    override fun provideLoginViewModel(): LoginViewModel {
        val loginViewModel: LoginViewModel by viewModels()
        return loginViewModel
    }
    lateinit var navHelper: NavHelper

    private val transferViewModel: TransferViewModel by viewModels()
    private val requestViewModel: NewRequestViewModel by viewModels()
    private val historyViewModel: TransactionHistoryViewModel by viewModels()

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loginViewModel = provideLoginViewModel()
        staxGoogleLoginInterface = this
        viewModelFactory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)

        navHelper = NavHelper(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        navHelper.setUpNav()
        initGoogleAuth()
        setLoginObserver()
        initFromIntent()
        checkForRequest(intent)
        checkForFragmentDirection(intent)
        observeForAppReview()
        setGoogleLoginInterface(this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        checkForRequest(intent!!)
        checkForFragmentDirection(intent)
    }

    override fun onResume() {
        super.onResume()
        navHelper.setUpNav()
    }

    fun checkPermissionsAndNavigate(navDirections: NavDirections) {
        navHelper.checkPermissionsAndNavigate(navDirections)
    }

    private fun observeForAppReview() = historyViewModel.showAppReviewLiveData().observe(this@MainActivity) {
        if (it) StaxAppReview.launchStaxReview(this@MainActivity)
    }

    private fun checkForRequest(intent: Intent) {
        if (intent.hasExtra(REQUEST_LINK)) {
            createFromRequest(intent.getStringExtra(REQUEST_LINK)!!)
        }
    }

    private fun checkForFragmentDirection(intent: Intent) {
        if (intent.hasExtra(FRAGMENT_DIRECT)) {
            val toWhere = intent.extras!!.getInt(FRAGMENT_DIRECT, 0)

            if (toWhere == NAV_EMAIL_CLIENT)
                com.hover.stax.utils.Utils.openEmail(getString(R.string.stax_emailing_subject, Hover.getDeviceId(this)), this)
            else
                navHelper.checkPermissionsAndNavigate(toWhere)
        }
    }
    private fun initGoogleAuth() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.google_server_client_id)).requestEmail().build()
        loginViewModel.signInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun setLoginObserver() = with(loginViewModel) {
        error.observe(this@MainActivity) {
            it?.let { staxGoogleLoginInterface.googleLoginFailed() }
        }

        googleUser.observe(this@MainActivity) {
            it?.let { staxGoogleLoginInterface.googleLoginSuccessful() }
        }
    }

    fun signIn() = loginForResult.launch(loginViewModel.signInClient.signInIntent)

    private val loginForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                loginViewModel.signIntoGoogle(result.data)
            } else {
                Timber.e("Google sign in failed")
                staxGoogleLoginInterface.googleLoginFailed()
            }
        }
    private fun initFromIntent() {
        when {
            intent.hasExtra(REQUEST_LINK) -> createFromRequest(intent.getStringExtra(REQUEST_LINK)!!)
            intent.hasExtra(FinancialTipsFragment.TIP_ID) -> navHelper.navigateWellness(intent.getStringExtra(FinancialTipsFragment.TIP_ID)!!)

            else -> com.hover.stax.utils.AnalyticsUtil.logAnalyticsEvent(getString(R.string.visit_screen, intent.action), this)
        }
    }

    private fun createFromRequest(link: String) {
        navHelper.checkPermissionsAndNavigate(MainNavigationDirections.actionGlobalTransferFragment(HoverAction.P2P))
        addLoadingDialog()
        transferViewModel.load(link)
        com.hover.stax.utils.AnalyticsUtil.logAnalyticsEvent(getString(R.string.clicked_request_link), this)
    }

    private fun addLoadingDialog() {
        val alertDialog = StaxDialog(this).setDialogMessage(R.string.loading_link_dialoghead).showIt()
        transferViewModel.isLoading.observe(this@MainActivity) { if (!it) alertDialog?.dismiss() }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == SMS && PermissionHelper(this).permissionsGranted(grantResults)) {
            com.hover.stax.utils.AnalyticsUtil.logAnalyticsEvent(getString(R.string.perms_sms_granted), this)
            sendSms(requestViewModel, this)
        } else if (requestCode == SMS) {
            com.hover.stax.utils.AnalyticsUtil.logAnalyticsEvent(getString(R.string.perms_sms_denied), this)
            UIHelper.flashAndReportMessage(this, getString(R.string.toast_error_smsperm))
        }
    }

    override fun googleLoginSuccessful() {
        if (loginViewModel.staxUser.value?.isMapper == true) BountyApplicationFragmentDirections.actionBountyApplicationFragmentToBountyListFragment()
    }

    override fun onAuthError(error: String) {
        Timber.e("Error : $error")
    }

    override fun onAuthSuccess(action: HoverAction?) {
        Timber.v("Auth success on action: ${action?.public_id}")
    }
}