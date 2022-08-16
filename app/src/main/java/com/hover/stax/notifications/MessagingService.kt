package com.hover.stax.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavDeepLinkBuilder
import com.appsflyer.AppsFlyerLib
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.hover.stax.FRAGMENT_DIRECT
import com.hover.stax.FROM_FCM
import com.hover.stax.R
import com.hover.stax.presentation.financial_tips.FinancialTipsFragment
import com.hover.stax.home.MainActivity
import timber.log.Timber
import kotlin.random.Random

class MessagingService : FirebaseMessagingService() {

    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)
        Timber.i("New token received: $newToken")
        AppsFlyerLib.getInstance().updateServerUninstallToken(applicationContext, newToken)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data

        if (data.containsKey("af-uinstall-tracking")) //ensures uninstall notifications remain silent
            return
        else {
            val redirect = data["redirect"]

            if (message.notification != null) {
                showNotification(message.notification!!.title!!, message.notification!!.body!!, redirect)
            } else {
                showNotification(data["title"]!!, data["body"]!!, redirect)
            }
        }
    }

    private fun showNotification(title: String, message: String, redirect: String?) {
        val channelId = getString(R.string.default_notification_channel_id)
        val pendingIntent = getPendingIntent(title, redirect)

        val builder = NotificationCompat.Builder(this, channelId).apply {
            setSmallIcon(R.drawable.ic_stax)
            setAutoCancel(true)
            setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE)
            priority = 2
            setTicker(title)
            setOngoing(false)
            setUsesChronometer(false)
            setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
            setOnlyAlertOnce(true)
            setContentTitle(title)
            setContentText(message)
            setContentIntent(pendingIntent)
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(channelId, notificationManager)

        notificationManager.notify(Random.nextInt(), builder.build())
    }

    private fun createNotificationChannel(channelId: String, notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun getPendingIntent(title: String, redirect: String?): PendingIntent {
        return if (redirect != null && redirect.contains(getString(R.string.deeplink_financial_tips))) {
            val tipId = Uri.parse(redirect).getQueryParameter("id")

            NavDeepLinkBuilder(this)
                .setGraph(R.navigation.home_navigation)
                .setDestination(R.id.tipsFragment)
                .setArguments(bundleOf(FinancialTipsFragment.TIP_ID to tipId))
                .setComponentName(MainActivity::class.java)
                .createPendingIntent()
        } else {
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra(FROM_FCM, title)
                putExtra(FRAGMENT_DIRECT, redirect)
            }
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        }
    }

}
