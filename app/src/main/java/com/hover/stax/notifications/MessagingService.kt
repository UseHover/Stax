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
import com.hover.stax.home.MainActivity
import com.hover.stax.presentation.financial_tips.FinancialTipsFragment
import kotlin.random.Random
import timber.log.Timber

class MessagingService : FirebaseMessagingService() {

    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)
        Timber.i("New token received: $newToken")
        AppsFlyerLib.getInstance().updateServerUninstallToken(applicationContext, newToken)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data

        if (data.containsKey("af-uinstall-tracking")) // ensures uninstall notifications remain silent
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
            setSmallIcon(R.mipmap.ic_launcher_round)
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

    private fun createNotificationChannel(
        channelId: String,
        notificationManager: NotificationManager
    ) {
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