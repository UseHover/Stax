package com.hover.stax.views

import android.content.Context
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout
import androidx.viewbinding.ViewBinding
import com.hover.stax.R
import com.hover.stax.utils.Utils

abstract class AbstractAutocompleteInput(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

	abstract var inputLayout: StaxDropdownLayout
	abstract var autocomplete: AutoCompleteTextView

	protected fun initUI() {
		autocomplete.apply {
			setOnFocusChangeListener { _, hasFocus -> setState(hasFocus) }
			setOnClickListener { Utils.showSoftKeyboard(context, it) }
			imeOptions = EditorInfo.IME_ACTION_DONE
		}
	}

	fun setText(number: String?, filter: Boolean) {
		if (!number.isNullOrEmpty()) setState(null, AbstractStatefulInput.SUCCESS)
		autocomplete.setText(number, filter)
	}

	fun setHint(hint: String?) {
		inputLayout.setHint(hint)
	}

	fun setAutocompleteClickListener(listener: AdapterView.OnItemClickListener?) {
		autocomplete.onItemClickListener = listener
	}

	fun addTextChangedListener(listener: TextWatcher?) {
		autocomplete.addTextChangedListener(listener)
	}

	fun setState(message: String?, state: Int) {
		inputLayout.setState(message, state)
	}

	private fun setState(hasFocus: Boolean) {
		if (!hasFocus) inputLayout.setState(
			null,
			if (!autocomplete.text.isNullOrEmpty()) AbstractStatefulInput.SUCCESS else AbstractStatefulInput.NONE
		)
	}
}