package com.hover.stax.transactionDetails

import android.app.Activity
import android.view.View
import android.widget.RadioGroup
import androidx.appcompat.widget.AppCompatButton
import com.hover.stax.R
import com.hover.stax.views.StaxDialog

class UpdateStatusDialog(a: Activity): StaxDialog(a) {

	init {
		ctx = a
		mView = a.layoutInflater.inflate(R.layout.dialog_update_status, null)
		customPosListener = null
		customNegListener = null
	}

	fun getSelected(): Int {
		val group = mView.findViewById<RadioGroup>(R.id.status_radio)
		return group.checkedRadioButtonId;
	}
}