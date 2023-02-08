package com.hover.stax.addChannels

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract


class AddAccountContract : ActivityResultContract<Void?, Intent?>() {

	override fun createIntent(context: Context, input: Void?): Intent {
		return Intent(context, AddAccountActivity::class.java)
	}

	override fun parseResult(resultCode: Int, intent: Intent?) : Intent? {
		return intent
	}
}