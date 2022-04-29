package com.hover.stax.countries

import android.content.Context
import android.util.AttributeSet
import com.hover.stax.R
import com.hover.stax.channels.Channel
import com.hover.stax.views.StaxDropdownLayout
import kotlinx.coroutines.*
import timber.log.Timber

class CountryDropdown(context: Context, attributeSet: AttributeSet) : StaxDropdownLayout(context, attributeSet) {

    private var countryAdapter: CountryAdapter? = null
    private var selectListener: CountryAdapter.SelectListener? = null

    fun updateChoices(channels: List<Channel>, currentCountry: String?) {
        if (channels.isNullOrEmpty()) {
            setEmptyState()
            return
        }

        countryAdapter = CountryAdapter(getCountryCodes(channels, currentCountry), context)
        autoCompleteTextView.apply {
            setAdapter(countryAdapter)
            setOnItemClickListener { parent, _, position, _ -> onSelect(parent.getItemAtPosition(position) as String) }
        }

        setDropdownValue(currentCountry)
    }

    private fun getCountryCodes(channelList: List<Channel>, currentCountry: String?): Array<String> {
        val codes: Deferred<Array<String>> = CoroutineScope(Dispatchers.IO).async {
            val countryCodes = mutableListOf(CountryAdapter.CODE_ALL_COUNTRIES)
            countryCodes.addAll(channelList.map { it.countryAlpha2 }.distinct().sorted())
            Timber.e("udpdating list %s", countryCodes[1])
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
        Timber.e("Setting code: %s", countryCode)
        countryAdapter?.let {
            Timber.e("country count: %s", it.count)
            autoCompleteTextView.setText(it.getCountryString(countryCode))
            Timber.e("view says: %s", autoCompleteTextView.text)
        }
    }
}