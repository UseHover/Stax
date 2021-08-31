package com.hover.stax

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.amplitude.api.Amplitude
import com.appsflyer.AppsFlyerLib
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.api.Hover
import com.hover.stax.channels.UpdateChannelsWorker
import com.hover.stax.databinding.SplashScreenLayoutBinding
import com.hover.stax.destruct.SelfDestructActivity

import com.hover.stax.faq.FaqViewModel
import com.hover.stax.home.MainActivity
import com.hover.stax.inapp_banner.BannerUtils
import com.hover.stax.onboarding.OnBoardingActivity
import com.hover.stax.pushNotification.PushNotificationTopicsInterface
import com.hover.stax.schedules.ScheduleWorker
import com.hover.stax.settings.BiometricChecker
import com.hover.stax.utils.Constants
import com.hover.stax.utils.Constants.FRAGMENT_DIRECT
import com.hover.stax.utils.UIHelper
import com.hover.stax.utils.Utils
import com.hover.stax.utils.blur.StaxBlur

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONObject


import org.koin.androidx.viewmodel.ext.android.getViewModel

import timber.log.Timber


class SplashScreenActivity : AppCompatActivity(), BiometricChecker.AuthListener, PushNotificationTopicsInterface {

    private lateinit var binding: SplashScreenLayoutBinding
    private lateinit var remoteConfig: FirebaseRemoteConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        UIHelper.setFullscreenView(this)
        super.onCreate(savedInstanceState)

        binding = SplashScreenLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        remoteConfig = FirebaseRemoteConfig.getInstance()

        startForegroundSequence()
        startBackgroundProcesses()
    }

    override fun onStart() {
        super.onStart()
        AppsFlyerLib.getInstance().start(this)
    }

    private fun startForegroundSequence() {
        blurBackground()
        fadeInLogo()
    }

    private fun startBackgroundProcesses() {
        initAmplitude()
        logPushNotificationIfRequired()
        initHover()
        createNotificationChannel()
        startWorkers()
        initFirebaseMessagingTopics()

        FirebaseInstallations.getInstance().getToken(false)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) Timber.i("Installation auth token: ${task.result?.token}")
            }
        FirebaseInstallations.getInstance().id.addOnCompleteListener { Timber.i("Firebase installation ID is ${it.result}") }

        initRemoteConfigs()
        updateBannerSessionCounter()
        initFAQ()
    }

    private fun initFAQ() {
        val faqViewModel: FaqViewModel = getViewModel()
        faqViewModel.faqLiveData
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

    private fun blurBackground() {
        Handler(Looper.getMainLooper()).postDelayed({
            val bg = BitmapFactory.decodeResource(resources, R.drawable.splash_background)
            val bitmap = StaxBlur(this@SplashScreenActivity, 16, 1).transform(bg)
            binding.splashImageBlur.apply {
                setImageBitmap(bitmap)
                visibility = View.VISIBLE
                animation = loadFadeIn(this@SplashScreenActivity)
            }
        }, BLUR_DELAY)
    }

    private fun fadeInLogo() {
        val tv = binding.splashContent
        setSplashContentTopDrawable(tv)

        Handler(Looper.getMainLooper()).postDelayed({
            tv.apply {
                visibility = View.VISIBLE
                animation = loadFadeIn(this@SplashScreenActivity)
            }
        }, LOGO_DELAY)
    }

    private fun loadFadeIn(context: Context) = AnimationUtils.loadAnimation(context, android.R.anim.fade_in)

    private fun setSplashContentTopDrawable(tv: TextView) {
        val dr = ResourcesCompat.getDrawable(resources, R.mipmap.stax, null)
        val bitmap = (dr as BitmapDrawable).bitmap
        val d = BitmapDrawable(resources, Bitmap.createScaledBitmap(bitmap, SPLASH_ICON_WIDTH, SPLASH_ICON_HEIGHT, true))
        tv.setCompoundDrawablesRelativeWithIntrinsicBounds(null, d, null, null)
    }

    private fun validateUser() = runBlocking {
        launch {
            delay(NAV_DELAY)

            if (!OnBoardingActivity.hasPassedThrough(this@SplashScreenActivity))
                goToOnBoardingActivity()
            else
                BiometricChecker(this@SplashScreenActivity, this@SplashScreenActivity).startAuthentication(null)
        }
    }

    private fun initAmplitude() = Amplitude.getInstance().initialize(this, getString(R.string.amp))
            .enableForegroundTracking(application)

    private fun logPushNotificationIfRequired() = intent.extras?.let {
        val fcmTitle = it.getString(Constants.FROM_FCM)
        fcmTitle?.let { title -> Utils.logAnalyticsEvent(getString(R.string.clicked_push_notification, title), this) }
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
                if (!selfDestructWhenAppVersionExpires())
                    validateUser()
            }
        }
    }

    private fun selfDestructWhenAppVersionExpires(): Boolean {
        return try {
            val currentVersionCode = packageManager.getPackageInfo(packageName, 0).versionCode
            Timber.e("Current version code :  $currentVersionCode")

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
        startChannelWorker(wm)
        startScheduleWorker(wm)
    }

    private fun startChannelWorker(wm: WorkManager) {
        wm.beginUniqueWork(UpdateChannelsWorker.CHANNELS_WORK_ID, ExistingWorkPolicy.KEEP, UpdateChannelsWorker.makeWork()).enqueue()
        wm.enqueueUniquePeriodicWork(UpdateChannelsWorker.TAG, ExistingPeriodicWorkPolicy.KEEP, UpdateChannelsWorker.makeToil())
    }

    private fun startScheduleWorker(wm: WorkManager) {
        wm.enqueueUniquePeriodicWork(ScheduleWorker.TAG, ExistingPeriodicWorkPolicy.KEEP, ScheduleWorker.makeToil())
    }

    private fun chooseNavigation(intent: Intent) {
        when {
            !OnBoardingActivity.hasPassedThrough(this) -> goToOnBoardingActivity()
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

    private fun goToOnBoardingActivity() {
        startActivity(Intent(this, OnBoardingActivity::class.java))
        finish()
    }

    private fun goToFulfillRequestActivity(intent: Intent) =
        startActivity(Intent(this, MainActivity::class.java).putExtra(Constants.REQUEST_LINK, intent.data.toString()))

    private fun goToMainActivity(redirectLink: String?) {
        val intent = Intent(this, MainActivity::class.java)

        try {
            redirectLink?.let { intent.putExtra(FRAGMENT_DIRECT, redirectLink.toInt()) }
        } catch (e: NumberFormatException) {
            Utils.logErrorAndReportToFirebase(SplashScreenActivity::class.java.simpleName, getString(R.string.firebase_fcm_redirect_format_err), e)
        }

        startActivity(intent)
    }

    override fun onAuthError(error: String?) = UIHelper.flashMessage(this, getString(R.string.toast_error_auth))

    override fun onAuthSuccess(action: HoverAction?) = chooseNavigation(intent)

    private fun redirectionIsExternal(redirectTo: String): Boolean = redirectTo.contains("https")

    private fun isToRedirectFromMainActivity(intent: Intent): Boolean = intent.extras?.getString(FRAGMENT_DIRECT) != null

    private fun isForFulfilRequest(intent: Intent): Boolean = intent.action != null && intent.action == Intent.ACTION_VIEW && intent.data != null

    private fun openUrl(url: String) = startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse(url)))

    companion object {
        const val BLUR_DELAY = 1000L
        const val LOGO_DELAY = 1200L
        const val NAV_DELAY = 1500L
        const val SPLASH_ICON_WIDTH = 177
        const val SPLASH_ICON_HEIGHT = 57
    }
}