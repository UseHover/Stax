package com.hover.stax

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import com.amplitude.api.Amplitude
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.api.Hover
import com.hover.stax.addChannels.ChannelsViewModel
import com.hover.stax.channels.ImportChannelsWorker
import com.hover.stax.channels.UpdateChannelsWorker
import com.hover.stax.presentation.financial_tips.FinancialTipsFragment
import com.hover.stax.home.MainActivity
import com.hover.stax.hover.PERM_ACTIVITY
import com.hover.stax.inapp_banner.BannerUtils
import com.hover.stax.notifications.PushNotificationTopicsInterface
import com.hover.stax.onboarding.OnBoardingActivity
import com.hover.stax.requests.REQUEST_LINK
import com.hover.stax.schedules.ScheduleWorker
import com.hover.stax.settings.BiometricChecker
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.UIHelper
import com.hover.stax.utils.Utils
import com.uxcam.OnVerificationListener
import com.uxcam.UXCam
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

const val FRAGMENT_DIRECT = "fragment_direct"
const val FROM_FCM = "from_notification"
const val VARIANT = "variant"

class RoutingActivity : AppCompatActivity(), BiometricChecker.AuthListener, PushNotificationTopicsInterface {

    private val channelsViewModel: ChannelsViewModel by viewModel()
    private lateinit var remoteConfig: FirebaseRemoteConfig
    private lateinit var workManager: WorkManager
    private var hasAccounts = false

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition { true }

        remoteConfig = FirebaseRemoteConfig.getInstance()
        workManager = WorkManager.getInstance(this)
        startBackgroundProcesses()
    }

    private fun startBackgroundProcesses() {
        with(channelsViewModel) {
            accounts.observe(this@RoutingActivity) { hasAccounts = it.isNotEmpty() }
            allChannels.observe(this@RoutingActivity) {
                if (it.isEmpty())
                    workManager.enqueue(ImportChannelsWorker.channelsImportRequest())
            }
        }

        lifecycleScope.launch(Dispatchers.IO) {
            initAmplitude()
            logPushNotificationIfRequired()
            initHover()
            initFirebaseMessagingTopics()
            updateBannerSessionCounter()
            initUxCam()
            registerUXCamPushNotification()
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
        val fcmTitle = it.getString(FROM_FCM)
        fcmTitle?.let { title -> AnalyticsUtil.logAnalyticsEvent(getString(R.string.clicked_push_notification, title), this) }
    }

    private fun initHover() {
        Hover.initialize(this)
        Hover.setBranding(getString(R.string.app_name), R.mipmap.stax, R.drawable.ic_stax, this)
        Hover.setPermissionActivity(PERM_ACTIVITY, this)
    }

    private fun initRemoteConfigs() {
        val configSettings = FirebaseRemoteConfigSettings.Builder().setMinimumFetchIntervalInSeconds(3600).build()
        remoteConfig.apply {
            setConfigSettingsAsync(configSettings)
            setDefaultsAsync(R.xml.remote_config_default)
            fetchAndActivate().addOnCompleteListener {
                val variant = remoteConfig.getString("onboarding_variant")
                Timber.i("Onboarding variant fetched $variant")
                Utils.saveString(VARIANT, variant, this@RoutingActivity)

                validateUser()
            }
        }
    }

    private fun initUxCam() {
        if (!BuildConfig.DEBUG) {
            UXCam.startWithKey(getString(R.string.uxcam_key))

            UXCam.addVerificationListener(object : OnVerificationListener {
                override fun onVerificationSuccess() {
                    FirebaseCrashlytics.getInstance()
                        .setCustomKey(getString(R.string.uxcam_session_url), UXCam.urlForCurrentSession())

                    val eventProperties = JSONObject()
                    val userProperties = JSONObject()
                    try {
                        eventProperties.put(getString(R.string.uxcam_session_url), UXCam.urlForCurrentSession())
                        userProperties.put(getString(R.string.uxcam_session_url), UXCam.urlForCurrentUser())
                    } catch (exception: JSONException) {
                    }

                    Amplitude.getInstance().logEvent("uxcam_session", eventProperties)
                    Amplitude.getInstance().setUserProperties(userProperties)
                }

                override fun onVerificationFailed(errorMessage: String) {}
            })
        }

    }

    private fun registerUXCamPushNotification(){
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@OnCompleteListener
            }
            val token = task.result

            UXCam.setPushNotificationToken(token)
        })
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(getString(R.string.default_notification_channel_id), getString(R.string.notify_default_title), importance)
            channel.description = getString(R.string.notify_default_channel_descrip)
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun startWorkers() {
        startChannelWorker(workManager)
        startScheduleWorker(workManager)
    }

    private fun startChannelWorker(wm: WorkManager) {
        wm.enqueueUniquePeriodicWork(UpdateChannelsWorker.TAG, ExistingPeriodicWorkPolicy.KEEP, UpdateChannelsWorker.makeToil())
    }

    private fun startScheduleWorker(wm: WorkManager) {
        wm.enqueueUniquePeriodicWork(ScheduleWorker::class.java.simpleName, ExistingPeriodicWorkPolicy.KEEP, ScheduleWorker.makeToil())
    }

    private fun chooseNavigation(intent: Intent) {
        when {
            !hasPassedOnboarding() -> goToOnBoardingActivity()
            redirectToFinancialTips() -> goToFinancialTips()
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

    private fun goToFinancialTips() {
        val tipId = Uri.parse(intent.getStringExtra("redirect")).getQueryParameter("id")
        startActivity(Intent(this, MainActivity::class.java).putExtra(FinancialTipsFragment.TIP_ID, tipId))
        finish()
    }

    private fun validateUser() = lifecycleScope.launchWhenStarted {
        when {
            !hasPassedOnboarding() -> goToOnBoardingActivity()
            hasAccounts -> BiometricChecker(this@RoutingActivity, this@RoutingActivity).startAuthentication(null)
            else -> {
                if (redirectToFinancialTips())
                    goToFinancialTips()
                else
                    goToMainActivity(null)
            }
        }
    }

    private fun goToOnBoardingActivity() {
        startActivity(Intent(this, OnBoardingActivity::class.java))
        finish()
    }

    private fun goToFulfillRequestActivity(intent: Intent) {
        startActivity(Intent(this, MainActivity::class.java).putExtra(REQUEST_LINK, intent.data.toString()))
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

    private fun hasPassedOnboarding(): Boolean = Utils.getBoolean(OnBoardingActivity::class.java.simpleName, this)

    private fun redirectToFinancialTips(): Boolean = intent.hasExtra("redirect") && intent.getStringExtra("redirect")!!.contains(getString(R.string.deeplink_financial_tips))

}