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
package com.hover.stax.login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType.FLEXIBLE
import com.google.android.play.core.install.model.AppUpdateType.IMMEDIATE
import com.google.android.play.core.install.model.InstallStatus.DOWNLOADED
import com.google.android.play.core.install.model.UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
import com.google.android.play.core.install.model.UpdateAvailability.UPDATE_AVAILABLE
import com.hover.stax.BuildConfig
import com.hover.stax.R
import com.hover.stax.core.Utils
import com.hover.stax.utils.UIHelper
import timber.log.Timber

const val FORCED_VERSION = "force_update_app_version"

abstract class AbstractGoogleAuthActivity :
    AppCompatActivity(),
    StaxGoogleLoginInterface {

    protected abstract fun provideLoginViewModel(): LoginViewModel

    private lateinit var staxGoogleLoginInterface: StaxGoogleLoginInterface

    private lateinit var updateManager: AppUpdateManager
    private var installListener: InstallStateUpdatedListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        updateManager = AppUpdateManagerFactory.create(this)

        if (!BuildConfig.DEBUG) checkForUpdates()
    }

    // checks that the update has not stalled
    override fun onResume() {
        super.onResume()
        if (!BuildConfig.DEBUG) updateManager.appUpdateInfo.addOnSuccessListener { updateInfo -> // if the update is downloaded but not installed, notify user to complete the update
            if (updateInfo.installStatus() == DOWNLOADED) showSnackbarForCompleteUpdate()

            // if an in-app update is already running, resume the update
            if (updateInfo.updateAvailability() == DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                updateManager.startUpdateFlowForResult(
                    updateInfo, IMMEDIATE, this, UPDATE_REQUEST_CODE
                )
            }
        }
    }

    fun setGoogleLoginInterface(staxGoogleLoginInterface: StaxGoogleLoginInterface) {
        this.staxGoogleLoginInterface = staxGoogleLoginInterface
    }

    private fun checkForUpdates() {
        val updateInfoTask = updateManager.appUpdateInfo

        updateInfoTask.addOnSuccessListener { updateInfo ->
            val updateType = getUpdateType(updateInfo)
            if (updateInfo.updateAvailability() == UPDATE_AVAILABLE && updateInfo.isUpdateTypeAllowed(
                    updateType
                )
            ) {
                logAppUpdate(STARTED)
                requestUpdate(updateInfo, updateType)
            } else {
                Timber.i("No new update available")
            }
        }
    }

    private fun logAppUpdate(status: String) {
        com.hover.stax.utils.AnalyticsUtil.logAnalyticsEvent(
            getString(
                R.string.force_update_status,
                status
            ),
            this
        )
    }

    private fun getUpdateType(updateInfo: AppUpdateInfo): Int {
        val isGracePeriod =
            (updateInfo.clientVersionStalenessDays() ?: -1) <= DAYS_FOR_FLEXIBLE_UPDATE
        val mustForceUpdate = BuildConfig.VERSION_CODE < Utils.getInt(FORCED_VERSION, this)
        return if (mustForceUpdate || !isGracePeriod) IMMEDIATE
        else FLEXIBLE
    }

    private fun requestUpdate(updateInfo: AppUpdateInfo, updateType: Int) {
        if (updateType == FLEXIBLE) {
            installListener = InstallStateUpdatedListener {
                if (it.installStatus() == DOWNLOADED) showSnackbarForCompleteUpdate()
            }
            updateManager.registerListener(installListener!!)
        }

        updateManager.startUpdateFlowForResult(updateInfo, updateType, this, UPDATE_REQUEST_CODE)
    }

    private fun showSnackbarForCompleteUpdate() {
        Snackbar.make(
            findViewById(R.id.home_root),
            getString(R.string.update_downloaded),
            Snackbar.LENGTH_INDEFINITE
        ).apply {
            setAction(getString(R.string.restart)) {
                updateManager.completeUpdate(); installListener?.let {
                updateManager.unregisterListener(
                    it
                )
            }
            }
            setActionTextColor(
                ContextCompat.getColor(
                    this@AbstractGoogleAuthActivity, R.color.white
                )
            )
            show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) { //
            UPDATE_REQUEST_CODE -> if (resultCode == RESULT_OK) {
                logAppUpdate(COMPLETED)
            } else {
                Timber.e("Update flow failed. Result code : $resultCode")
                logAppUpdate(FAILED)
                checkForUpdates()
            }
        }
    }

    override fun googleLoginFailed() {
        UIHelper.flashAndReportMessage(this, R.string.login_google_err)
    }

    companion object {
        const val DAYS_FOR_FLEXIBLE_UPDATE = 3
        const val UPDATE_REQUEST_CODE = 90
        const val STARTED = "STARTED"
        const val COMPLETED = "COMPLETED"
        const val FAILED = "FAILED"
    }
}