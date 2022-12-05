package com.hover.stax.hover

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

class TransactionContract : ActivityResultContract<HoverSession.Builder, Intent?>() {

	private var builder: HoverSession.Builder? = null

	override fun createIntent(context: Context, b: HoverSession.Builder): Intent {
		builder = b
		return b.build().getIntent()
	}

	override fun parseResult(resultCode: Int, result: Intent?) : Intent? {
		if (resultCode != Activity.RESULT_OK) {
			return null
		}
		return result
	}
}