package com.hover.stax.countries

import android.content.Context
import android.util.AttributeSet
import com.hover.stax.R
import com.hover.stax.channels.Channel
import com.hover.stax.utils.UIHelper
import com.hover.stax.views.StaxDropdownLayout
import kotlinx.coroutines.*

class CountryDropdown(context: Context, attributeSet: AttributeSet) : StaxDropdownLayout(context, attributeSet) {

    private var countryAdapter: CountryAdapter? = null
    private var selectListener: CountryAdapter.SelectListener? = null

    fun updateChoices(channels: List<Channel>, currentCountry: String?) {
        if (channels.isNullOrEmpty()) {
            setEmptyState()
            return
        }

        countryAdapter = CountryAdapter(getCountryCodes(channels), context)
        autoCompleteTextView.apply {
            setAdapter(countryAdapter)
            setOnItemClickListener { parent, _, position, _ -> onSelect(parent.getItemAtPosition(position) as String) }
        }

        setDropdownValue(currentCountry)
    }

    private fun getCountryCodes(channelList: List<Channel>): Array<String> {
        val codes: Deferred<Array<String>> = CoroutineScope(Dispatchers.IO).async {
            val countryCodes = mutableListOf(CountryAdapter.CODE_ALL_COUNTRIES)
            countryCodes.addAll(channelList.distinctBy { it.countryAlpha2 }.sortedBy { it.countryAlpha2 }.map { it.countryAlpha2 })

            return@async countryCodes.toTypedArray()
        }

        return runBlocking {
            codes.await()
        }
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
        countryAdapter?.let {
            autoCompleteTextView.setText(it.getCountryString(countryCode
                    ?: CountryAdapter.CODE_ALL_COUNTRIES))
        }
    }

}