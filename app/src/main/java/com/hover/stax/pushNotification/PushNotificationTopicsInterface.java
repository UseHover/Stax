package com.hover.stax.pushNotification;

import android.content.Context;

import com.google.firebase.messaging.FirebaseMessaging;
import com.hover.stax.R;
import com.hover.stax.utils.Utils;

public interface PushNotificationTopicsInterface {
	default void joinAllNotifications(Context c) {
		Utils.setFirebaseMessagingTopic(c.getString(R.string.firebase_topic_everyone));
	}
	default void joinByNoActivityTopicNotifGroup(Context c) {
		if(Utils.isFirebaseTopicInDefaultState(c.getString(R.string.firebase_topic_no_usage_activity), c)) {
			Utils.setFirebaseMessagingTopic(c.getString(R.string.firebase_topic_no_usage_activity));
		}
	}
	default void joinByNoRequestMoneyNotifGroup(Context c) {
		if(Utils.isFirebaseTopicInDefaultState(c.getString(R.string.firebase_topic_no_request_money), c)) {
			Utils.setFirebaseMessagingTopic(c.getString(R.string.firebase_topic_no_request_money));
		}
	}

	default void joinAllBountiesTopicNotifGroup(Context c) {
		Utils.setFirebaseMessagingTopic(c.getString(R.string.firebase_topic_yes_bounty_yes_try));
	}
	default void joinByBountyCountryTopicNotifGroup(String countryCode, Context c) {
		Utils.setFirebaseMessagingTopic(c.getString(R.string.firebase_topic_bounty_in_country, countryCode));
	}
	default void joinByChannelNotifGroup(int channelId ,Context c){
		FirebaseMessaging.getInstance().subscribeToTopic(c.getString(R.string.firebase_topic_channel, channelId));
	}
	default void joinByRequestMoneyNotifGroup(Context c) {
		Utils.setFirebaseMessagingTopic(c.getString(R.string.firebase_topic_yes_request_money));
	}

	default void stopReceivingNoRequestMoneyNotifGroup(Context c) {
		Utils.removeFirebaseMessagingTopic(c.getString(R.string.firebase_topic_no_request_money));
		Utils.alterFirebaseTopicState(c.getString(R.string.firebase_topic_no_request_money), c);
	}
	default void stopReceivingNoActivityTopicNotifGroup(Context c) {
		Utils.removeFirebaseMessagingTopic(c.getString(R.string.firebase_topic_no_usage_activity));
		Utils.alterFirebaseTopicState(c.getString(R.string.firebase_topic_no_usage_activity), c);
	}

}
