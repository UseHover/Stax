package com.hover.stax

import android.app.Activity
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.AppUpdateType.IMMEDIATE
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.hover.stax.utils.AnalyticsUtil

class StaxForceUpdateManager(private val activity: Activity,) {
	private val appUpdateManager: AppUpdateManager by lazy { AppUpdateManagerFactory.create(activity) }
	private val listener: InstallStateUpdatedListener = InstallStateUpdatedListener { installState ->
		//Only log an actionable step. Ignore downloading progress
		if (installState.installStatus() != InstallStatus.DOWNLOADING) {
			val status = getReadableInstallStatus(installState.installStatus())
			AnalyticsUtil.logAnalyticsEvent(activity.getString(R.string.app_force_update_status, status), activity)
		}
	}

	fun runImmediateAppUpdate() {
		// Each AppUpdateInfo instance can be used to start an update only once
		val appUpdateInfoTask = appUpdateManager.appUpdateInfo
		appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
			if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
				AnalyticsUtil.logAnalyticsEvent(activity.getString(R.string.app_force_update_status, STARTED), activity)
				appUpdateManager.startUpdateFlowForResult(appUpdateInfo, IMMEDIATE, activity, IMMEDIATE_UPDATE_REQUEST_CODE)
			}
		}
	}

	fun resumeAppUpdate() {
		// If an in-app update is already running, resume the update.
		appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
				if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
					appUpdateManager.startUpdateFlowForResult(
						appUpdateInfo,
						IMMEDIATE,
						activity,
						IMMEDIATE_UPDATE_REQUEST_CODE
					);
				}
			}
	}
	fun registerListener() {
		appUpdateManager.registerListener(listener)
	}
	fun unregisterListener() {
		appUpdateManager.unregisterListener(listener)
	}

	private fun getReadableInstallStatus(statusCode: Int) : String {
		return when(statusCode) {
			InstallStatus.DOWNLOADED -> "DOWNLOADED"
			InstallStatus.INSTALLED -> "COMPLETED INSTALLATION"
			InstallStatus.PENDING -> "PENDING"
			InstallStatus.CANCELED -> "CANCELED"
			InstallStatus.FAILED -> "FAILED"
			InstallStatus.UNKNOWN -> "UNKNOWN"
			else -> "EVENT NOT CAPTURED"
		}
	}
	companion object {
		const val IMMEDIATE_UPDATE_REQUEST_CODE = 4500
		const val STARTED = "STARTED"
		const val NEEDS_UPDATE = "NEEDS_UPDATE"
		const val OPTIMAL = "OPTIMAL"
		const val COULD_NOT_CHECK = "COULD_NOT_CHECK"
		const val FORCED_VERSION = "force_update_app_version"
	}
}