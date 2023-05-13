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

import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
import com.hover.stax.R
import com.hover.stax.core.Utils.alterFirebaseTopicState
import com.hover.stax.core.Utils.isFirebaseTopicInDefaultState
import com.hover.stax.core.Utils.removeFirebaseMessagingTopic
import com.hover.stax.core.Utils.setFirebaseMessagingTopic

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
        setFirebaseMessagingTopic(c.getString(R.string.firebase_topic_bounty_in_country, countryCode?.uppercase()))
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