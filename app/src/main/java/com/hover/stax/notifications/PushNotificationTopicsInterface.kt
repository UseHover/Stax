package com.hover.stax.notifications

import android.content.Context
import com.hover.stax.utils.Utils.setFirebaseMessagingTopic
import com.hover.stax.utils.Utils.isFirebaseTopicInDefaultState
import com.hover.stax.utils.Utils.removeFirebaseMessagingTopic
import com.hover.stax.utils.Utils.alterFirebaseTopicState
import com.hover.stax.R
import com.google.firebase.messaging.FirebaseMessaging

interface PushNotificationTopicsInterface {

    fun joinAllNotifications(c: Context) {
        setFirebaseMessagingTopic(c.getString(R.string.firebase_topic_everyone))
    }

    fun joinNoUsageGroup(c: Context) {
        if (isFirebaseTopicInDefaultState(c.getString(R.string.firebase_topic_no_usage_activity), c)) {
            setFirebaseMessagingTopic(c.getString(R.string.firebase_topic_no_usage_activity))
        }
    }

    fun joinNoRequestMoneyGroup(c: Context) {
        if (isFirebaseTopicInDefaultState(c.getString(R.string.firebase_topic_no_request_money), c)) {
            setFirebaseMessagingTopic(c.getString(R.string.firebase_topic_no_request_money))
        }
    }

    fun joinAllBountiesGroup(c: Context) {
        setFirebaseMessagingTopic(c.getString(R.string.firebase_topic_yes_bounty_yes_try))
    }

    fun joinBountyCountryGroup(countryCode: String?, c: Context) {
        setFirebaseMessagingTopic(c.getString(R.string.firebase_topic_bounty_in_country, countryCode))
    }

    fun joinChannelGroup(channelId: Int, c: Context) {
        FirebaseMessaging.getInstance().subscribeToTopic(c.getString(R.string.firebase_topic_channel, channelId))
    }

    fun joinRequestMoneyGroup(c: Context) {
        setFirebaseMessagingTopic(c.getString(R.string.firebase_topic_yes_request_money))
    }

    fun joinTransactionGroup(c: Context) {
        setFirebaseMessagingTopic(c.getString(R.string.firebase_topic_yes_transaction))
    }

    fun leaveNoRequestMoneyGroup(c: Context) {
        removeFirebaseMessagingTopic(c.getString(R.string.firebase_topic_no_request_money))
        alterFirebaseTopicState(c.getString(R.string.firebase_topic_no_request_money), c)
    }

    fun leaveNoUsageGroup(c: Context) {
        removeFirebaseMessagingTopic(c.getString(R.string.firebase_topic_no_usage_activity))
        alterFirebaseTopicState(c.getString(R.string.firebase_topic_no_usage_activity), c)
    }
}