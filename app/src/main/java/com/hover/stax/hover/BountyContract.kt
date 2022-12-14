package com.hover.stax.hover

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.api.HoverParameters
import com.hover.stax.notifications.PushNotificationTopicsInterface
import timber.log.Timber

class BountyContract : ActivityResultContract<HoverAction, Intent?>(),
	PushNotificationTopicsInterface {

	override fun createIntent(context: Context, a: HoverAction): Intent {
		updatePushNotifGroupStatus(context, a)
		return HoverParameters.Builder(context).request(a.public_id).setEnvironment(HoverParameters.MANUAL_ENV).buildIntent()
	}

	override fun parseResult(resultCode: Int, intent: Intent?) : Intent? {
		// We don't care about the resultCode - bounties are currently always cancelled because they rely on the timer running out to end.
		if (intent == null || intent.extras == null || intent.extras!!.getString("uuid") == null) {
			Timber.e("Bounty result got null transaction data")
		}
		return intent
	}

	private fun updatePushNotifGroupStatus(c: Context, a: HoverAction) {
		joinAllBountiesGroup(c)
		joinBountyCountryGroup(a.country_alpha2, c)
	}
}