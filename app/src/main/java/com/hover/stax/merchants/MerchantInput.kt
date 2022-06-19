package com.hover.stax.merchants

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.AutoCompleteTextView
import com.hover.stax.R
import com.hover.stax.databinding.MerchantInputBinding
import com.hover.stax.views.AbstractAutocompleteInput
import com.hover.stax.views.StaxDropdownLayout

class MerchantInput(context: Context, attrs: AttributeSet) : AbstractAutocompleteInput(context, attrs) {

	val binding = MerchantInputBinding.inflate(LayoutInflater.from(context), this, true)

	override var inputLayout: StaxDropdownLayout = binding.dropdownLayout
	override var autocomplete: AutoCompleteTextView = binding.dropdownLayout.findViewById(R.id.autoCompleteView)

	init {
		initUI()
	}

	fun setRecent(merchants: List<Merchant>, c: Context) {
		val adapter = MerchantArrayAdapter(c, merchants)
		autocomplete.setAdapter(adapter)
	}

	fun setSelected(merchant: Merchant?) {
		if (merchant != null) setText(merchant.toString(), false)
	}
}