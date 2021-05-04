package com.hover.stax.pushNotification;

import android.content.Context;

import com.hover.stax.R;
import com.hover.stax.utils.Utils;

public interface PushNotificationTopicsInterface {
	default void receiveNoActivityTopicNotificationIfValid(Context c) {
		if(Utils.isFirebaseTopicInDefaultState(c.getString(R.string.firebase_topic_no_usage_activity), c)) {
			Utils.setFirebaseMessagingTopic(c.getString(R.string.firebase_topic_no_usage_activity));
		}
	}

	default void receiveAllNotifications(Context c) {
		Utils.setFirebaseMessagingTopic(c.getString(R.string.firebase_topic_everyone));
	}

	default void stopReceivingNoActivityTopicNotification(Context c) {
		Utils.removeFirebaseMessagingTopic(c.getString(R.string.firebase_topic_no_usage_activity));
		Utils.alterFirebaseTopicState(c.getString(R.string.firebase_topic_no_usage_activity), c);
	}

}
