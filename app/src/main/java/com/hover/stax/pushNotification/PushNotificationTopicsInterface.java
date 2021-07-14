package com.hover.stax.pushNotification;

import android.content.Context;

import com.google.firebase.messaging.FirebaseMessaging;
import com.hover.stax.R;

public interface PushNotificationTopicsInterface {
	default void joinAllNotifications(Context c) {
		Utils.setFirebaseMessagingTopic(c.getString(R.string.firebase_topic_everyone));
	}
	default void joinNoUsageGroup(Context c) {
		if(Utils.isFirebaseTopicInDefaultState(c.getString(R.string.firebase_topic_no_usage_activity), c)) {
			Utils.setFirebaseMessagingTopic(c.getString(R.string.firebase_topic_no_usage_activity));
		}
	}
	default void joinNoRequestMoneyGroup(Context c) {
		if(Utils.isFirebaseTopicInDefaultState(c.getString(R.string.firebase_topic_no_request_money), c)) {
			Utils.setFirebaseMessagingTopic(c.getString(R.string.firebase_topic_no_request_money));
		}
	}

	default void joinAllBountiesGroup(Context c) {
		Utils.setFirebaseMessagingTopic(c.getString(R.string.firebase_topic_yes_bounty_yes_try));
	}
	default void joinBountyCountryGroup(String countryCode, Context c) {
		Utils.setFirebaseMessagingTopic(c.getString(R.string.firebase_topic_bounty_in_country, countryCode));
	}
	default void joinChannelGroup(int channelId , Context c){
		FirebaseMessaging.getInstance().subscribeToTopic(c.getString(R.string.firebase_topic_channel, channelId));
	}
	default void joinRequestMoneyGroup(Context c) {
		Utils.setFirebaseMessagingTopic(c.getString(R.string.firebase_topic_yes_request_money));
	}
	default void joinTransactionGroup(Context c) {
		Utils.setFirebaseMessagingTopic(c.getString(R.string.firebase_topic_yes_transaction));
	}

	default void leaveNoRequestMoneyGroup(Context c) {
		Utils.removeFirebaseMessagingTopic(c.getString(R.string.firebase_topic_no_request_money));
		Utils.alterFirebaseTopicState(c.getString(R.string.firebase_topic_no_request_money), c);
	}
	default void leaveNoUsageGroup(Context c) {
		Utils.removeFirebaseMessagingTopic(c.getString(R.string.firebase_topic_no_usage_activity));
		Utils.alterFirebaseTopicState(c.getString(R.string.firebase_topic_no_usage_activity), c);
	}

}
