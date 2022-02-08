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
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.hover.stax.R
import com.hover.stax.RoutingActivity
import com.hover.stax.financialTips.FinancialTipsFragment
import com.hover.stax.utils.Constants
import com.hover.stax.utils.DateUtils
import timber.log.Timber
import kotlin.random.Random

class FirebaseMessageReceiver : FirebaseMessagingService() {

    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)
        // update token on backend if used for notifications
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        val redirect = data["redirect"]

        if (message.notification != null) {
            Timber.e("Showing notification message")
            showNotification(message.notification!!.title!!, message.notification!!.body!!, redirect)
        } else {
            Timber.e("Showing data message")
            showNotification(data["title"]!!, data["body"]!!, redirect)
        }
    }

    private fun showNotification(title: String, message: String, redirect: String?) {
        val channelId: String = DateUtils.now().toString()

        val pendingIntent = getPendingIntent(title, redirect)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(channelId, notificationManager)

        val builder = NotificationCompat.Builder(applicationContext, channelId).apply {
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

        notificationManager.notify(Random.nextInt(), builder.build())
    }

    private fun createNotificationChannel(channelId: String, notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun getPendingIntent(title: String, redirect: String?): PendingIntent {
        Timber.e("Redirect: $redirect")

        return if (redirect != null && redirect.contains(getString(R.string.deeplink_financial_tips))) {
            val tipId = Uri.parse(redirect).getQueryParameter("id")

            NavDeepLinkBuilder(this)
                .setGraph(R.navigation.home_navigation)
                .setDestination(R.id.wellnessFragment)
                .setArguments(bundleOf(FinancialTipsFragment.TIP_ID to tipId))
                .createPendingIntent()
        } else {
            val intent = Intent(this, RoutingActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra(Constants.FROM_FCM, title)
                putExtra(Constants.FRAGMENT_DIRECT, redirect)
            }
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        }
    }

}
