package com.hover.stax.transactionDetails

import android.app.Activity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.RadioGroup
import androidx.appcompat.widget.AppCompatButton
import com.hover.stax.R
import com.hover.stax.countries.CountryAdapter
import com.hover.stax.views.StaxDialog
import com.hover.stax.views.StaxDropdownLayout

class UpdateStatusDialog(a: Activity): StaxDialog(a), RadioGroup.OnCheckedChangeListener {

	init {
		ctx = a
		mView = a.layoutInflater.inflate(R.layout.dialog_update_status, null)
		customPosListener = null
		customNegListener = null
		mView.findViewById<RadioGroup>(R.id.status_radio).setOnCheckedChangeListener(this)
		setCategoryChoices(mView)
	}

	private fun setCategoryChoices(v: View) {
		val categories = ctx.resources.getStringArray(R.array.category_options)
		val categoryAdapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_item, categories)
		v.findViewById<StaxDropdownLayout>(R.id.categoryDropdown).autoCompleteTextView.setAdapter(categoryAdapter)
	}

	fun getSelected(): Int {
		val group = mView.findViewById<RadioGroup>(R.id.status_radio)
		return group.checkedRadioButtonId;
	}

	fun getCategory(): String {
		val dropdown = mView.findViewById<StaxDropdownLayout>(R.id.categoryDropdown)
		return dropdown.autoCompleteTextView.text.toString()
	}

	override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
		if (checkedId == R.id.failure) {
			mView.findViewById<StaxDropdownLayout>(R.id.categoryDropdown).visibility = View.VISIBLE
		}
	}
}