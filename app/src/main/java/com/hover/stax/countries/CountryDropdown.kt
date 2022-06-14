package com.hover.stax.countries

import android.content.Context
import android.util.AttributeSet
import com.hover.stax.R
import com.hover.stax.views.StaxDropdownLayout

class CountryDropdown(context: Context, attributeSet: AttributeSet) : StaxDropdownLayout(context, attributeSet) {

    private var countryAdapter: CountryAdapter? = null
    private var selectListener: CountryAdapter.SelectListener? = null

    fun updateChoices(countryList: List<String>, currentCountry: String?) {
        if (countryList.isEmpty()) {
            setEmptyState()
            return
        }

        countryAdapter = CountryAdapter(countryList.toTypedArray(), context)
        autoCompleteTextView.apply {
            setAdapter(countryAdapter)
            setOnItemClickListener { parent, _, position, _ -> onSelect(parent.getItemAtPosition(position) as String) }
        }

        setDropdownValue(currentCountry)
    }

    private fun setEmptyState() {
        setState(context.getString(R.string.channels_error_nodata), ERROR)
    }

    fun setListener(sl: CountryAdapter.SelectListener) {
        selectListener = sl
    }

    private fun onSelect(code: String) {
        setDropdownValue(code)
        selectListener?.countrySelect(code)
    }

    fun setDropdownValue(countryCode: String?) {
        autoCompleteTextView.setText(countryAdapter?.getCountryString(countryCode) ?: "")
    }
}