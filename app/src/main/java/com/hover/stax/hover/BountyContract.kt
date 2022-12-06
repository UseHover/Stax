package com.hover.stax.hover

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.api.HoverParameters
import com.hover.stax.notifications.PushNotificationTopicsInterface

class BountyContract : ActivityResultContract<HoverAction, Intent?>(),
	PushNotificationTopicsInterface {

	override fun createIntent(context: Context, a: HoverAction): Intent {
		updatePushNotifGroupStatus(context, a)
		return HoverParameters.Builder(context).request(a.public_id).setEnvironment(HoverParameters.MANUAL_ENV).buildIntent()
	}

	override fun parseResult(resultCode: Int, result: Intent?) : Intent? {
		if (resultCode != Activity.RESULT_OK) {
			return null
		}
		return result
	}

	private fun updatePushNotifGroupStatus(c: Context, a: HoverAction) {
		joinAllBountiesGroup(c)
		joinBountyCountryGroup(a.country_alpha2, c)
	}
}