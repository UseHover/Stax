package com.hover.stax.addAccounts

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.hover.stax.domain.model.Account


class AddAccountContract : ActivityResultContract<Void?, Intent?>() {

	override fun createIntent(context: Context, input: Void?): Intent {
		return Intent(context, AddAccountActivity::class.java)
	}

	override fun parseResult(resultCode: Int, intent: Intent?) : Intent? {
		return intent
	}
}