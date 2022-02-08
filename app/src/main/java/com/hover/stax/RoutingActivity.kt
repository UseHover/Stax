package com.hover.stax

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.amplitude.api.Amplitude
import com.appsflyer.AppsFlyerLib
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.api.Hover
import com.hover.stax.channels.ChannelsViewModel
import com.hover.stax.channels.ImportChannelsWorker
import com.hover.stax.channels.UpdateChannelsWorker
import com.hover.stax.destruct.SelfDestructActivity
import com.hover.stax.home.MainActivity
import com.hover.stax.inapp_banner.BannerUtils
import com.hover.stax.onboarding.OnBoardingActivity
import com.hover.stax.notifications.PushNotificationTopicsInterface
import com.hover.stax.schedules.ScheduleWorker
import com.hover.stax.settings.BiometricChecker
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.Constants
import com.hover.stax.utils.Constants.FRAGMENT_DIRECT
import com.hover.stax.utils.UIHelper
import com.hover.stax.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class RoutingActivity : AppCompatActivity(), BiometricChecker.AuthListener, PushNotificationTopicsInterface {

    private val channelsViewModel: ChannelsViewModel by viewModel()
    private lateinit var remoteConfig: FirebaseRemoteConfig
    private var hasAccounts = false

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition { true }

        remoteConfig = FirebaseRemoteConfig.getInstance()
        startBackgroundProcesses()

//        getFirebaseToken()
    }

    override fun onStart() {
        super.onStart()

        AppsFlyerLib.getInstance().start(this)
    }

//    private fun getFirebaseToken(){
//        FirebaseMessaging.getInstance().token.addOnCompleteListener {
//            if (!it.isSuccessful) {
//                Timber.w(it.exception, RoutingActivity::class.java.simpleName, "Fetching FCM registration token failed")
//            }
//
//            // Get new FCM registration token
//            val token = it.result
//            Timber.e("Messaging token: $token")
//        }
//    }

    private fun startBackgroundProcesses() {
        with(channelsViewModel) {
            accounts.observe(this@RoutingActivity) { hasAccounts = it.isNotEmpty() }
            migrateAccounts()
        }

        lifecycleScope.launch(Dispatchers.IO) {
            initAmplitude()
            logPushNotificationIfRequired()
            initHover()
            initFirebaseMessagingTopics()
            updateBannerSessionCounter()
        }

        createNotificationChannel()
        startWorkers()

        with(FirebaseInstallations.getInstance()) {
            getToken(false)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) Timber.i("Installation auth token: ${task.result?.token}")
                }
            id.addOnCompleteListener { Timber.i("Firebase installation ID is ${it.result}") }
        }

        initRemoteConfigs()
    }

    private fun updateBannerSessionCounter() {
        val currentCount: Int = Utils.getInt(BannerUtils.APP_SESSIONS, this)
        if (currentCount < 5) Utils.saveInt(BannerUtils.APP_SESSIONS, currentCount + 1, this)
    }

    private fun initFirebaseMessagingTopics() {
        joinAllNotifications(this)
        joinNoUsageGroup(this)
        joinNoRequestMoneyGroup(this)
    }

    private fun initAmplitude() = Amplitude.getInstance().initialize(this, getString(R.string.amp)).enableForegroundTracking(application)

    private fun logPushNotificationIfRequired() = intent.extras?.let {
        val fcmTitle = it.getString(Constants.FROM_FCM)
        fcmTitle?.let { title -> AnalyticsUtil.logAnalyticsEvent(getString(R.string.clicked_push_notification, title), this) }
    }

    private fun initHover() {
        Hover.initialize(this)
        Hover.setBranding(getString(R.string.app_name), R.mipmap.stax, this)
        Hover.setPermissionActivity(Constants.PERM_ACTIVITY, this)
    }

    private fun initRemoteConfigs() {
        val configSettings = FirebaseRemoteConfigSettings.Builder().setMinimumFetchIntervalInSeconds(3600).build()
        remoteConfig.apply {
            setConfigSettingsAsync(configSettings)
            setDefaultsAsync(R.xml.remote_config_default)
            fetchAndActivate().addOnCompleteListener {
                val variant = remoteConfig.getString("onboarding_variant")
                Timber.e("Variant fetched $variant")
                Utils.saveString(Constants.VARIANT, variant, this@RoutingActivity)

                if (!selfDestructWhenAppVersionExpires())
                    validateUser()
            }
        }
    }

    private fun selfDestructWhenAppVersionExpires(): Boolean {
        return try {
            val currentVersionCode = packageManager.getPackageInfo(packageName, 0).versionCode

            val forceUpdateVersionCode = remoteConfig.getString("force_update_app_version").toInt()
            if (forceUpdateVersionCode > currentVersionCode) {
                startActivity(Intent(this, SelfDestructActivity::class.java))
                finish()
                true
            } else false
        } catch (e: PackageManager.NameNotFoundException) {
            false
        } catch (e: NumberFormatException) {
            false
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("DEFAULT", getString(R.string.notify_default_title), importance)
            channel.description = getString(R.string.notify_default_channel_descrip)
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun startWorkers() {
        val wm = WorkManager.getInstance(this)
        wm.enqueue(ImportChannelsWorker.channelsImportRequest())
        startChannelWorker(wm)
        startScheduleWorker(wm)
    }

    private fun startChannelWorker(wm: WorkManager) {
        wm.beginUniqueWork(UpdateChannelsWorker.CHANNELS_WORK_ID, ExistingWorkPolicy.KEEP, UpdateChannelsWorker.makeWork()).enqueue()
        wm.enqueueUniquePeriodicWork(UpdateChannelsWorker.TAG, ExistingPeriodicWorkPolicy.KEEP, UpdateChannelsWorker.makeToil())
    }

    private fun startScheduleWorker(wm: WorkManager) {
        wm.enqueueUniquePeriodicWork(ScheduleWorker::class.java.simpleName, ExistingPeriodicWorkPolicy.KEEP, ScheduleWorker.makeToil())
    }

    private fun chooseNavigation(intent: Intent) {
        when {
            !hasPassedOnboarding() -> goToOnBoardingActivity()
            isToRedirectFromMainActivity(intent) -> {
                val redirectLink = intent.extras?.getString(FRAGMENT_DIRECT)
                redirectLink?.let {
                    if (redirectionIsExternal(it))
                        openUrl(it)
                    else
                        goToMainActivity(it)
                }
            }
            isForFulfilRequest(intent) -> goToFulfillRequestActivity(intent)
            else -> goToMainActivity(null)
        }

        finish()
    }

    private fun validateUser() = lifecycleScope.launchWhenStarted {
        delay(1500L)

        when {
            !hasPassedOnboarding() -> goToOnBoardingActivity()
            hasAccounts -> BiometricChecker(this@RoutingActivity, this@RoutingActivity).startAuthentication(null)
            else -> goToMainActivity(null)
        }
    }

    private fun goToOnBoardingActivity() {
        startActivity(Intent(this, OnBoardingActivity::class.java))
        finish()
    }

    private fun goToFulfillRequestActivity(intent: Intent) {
        startActivity(Intent(this, MainActivity::class.java).putExtra(Constants.REQUEST_LINK, intent.data.toString()))
        finish()
    }

    private fun goToMainActivity(redirectLink: String?) {
        val intent = Intent(this, MainActivity::class.java)


        try {
            redirectLink?.let { intent.putExtra(FRAGMENT_DIRECT, redirectLink.toInt()) }
        } catch (e: NumberFormatException) {
            AnalyticsUtil.logErrorAndReportToFirebase(RoutingActivity::class.java.simpleName, getString(R.string.firebase_fcm_redirect_format_err), e)
        }

        startActivity(intent)
        finish()
    }

    override fun onAuthError(error: String) = runOnUiThread { UIHelper.flashMessage(this, getString(R.string.toast_error_auth)) }

    override fun onAuthSuccess(action: HoverAction?) = chooseNavigation(intent)

    private fun redirectionIsExternal(redirectTo: String): Boolean = redirectTo.contains("https")

    private fun isToRedirectFromMainActivity(intent: Intent): Boolean = intent.extras?.getString(FRAGMENT_DIRECT) != null

    private fun isForFulfilRequest(intent: Intent): Boolean = intent.action != null && intent.action == Intent.ACTION_VIEW && intent.data != null

    private fun openUrl(url: String) = startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse(url)))

    private fun hasPassedOnboarding(): Boolean = Utils.getBoolean(OnBoardingActivity::class.java.simpleName,this)

}