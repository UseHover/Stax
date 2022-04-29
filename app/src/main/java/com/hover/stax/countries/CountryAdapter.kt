package com.hover.stax.countries

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.hover.stax.R
import com.hover.stax.databinding.CountryItemBinding
import com.yariksoffice.lingver.Lingver
import java.util.*

class CountryAdapter(val codes: Array<String>, context: Context) : ArrayAdapter<String>(context, 0) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: ViewHolder
        var view = convertView

        if (view == null) {
            val binding = CountryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            view = binding.root

            holder = ViewHolder(binding)
            view.tag = holder
        } else
            holder = view.tag as ViewHolder

        holder.setText(getCountryString(codes[position]))

        return view
    }

    fun getCountryString(code: String?): String = if (code.isNullOrEmpty() || code == CODE_ALL_COUNTRIES)
        context.getString(R.string.all_countries_with_emoji)
    else
        context.getString(R.string.country_with_emoji, countryCodeToEmoji(code), getFullCountryName(code))

    private fun getFullCountryName(code: String): String {
        val locale = Locale(Lingver.getInstance().getLanguage(), code)
        return locale.displayCountry
    }

    private fun countryCodeToEmoji(countryCode: String): String {
        val firstLetter = Character.codePointAt(countryCode.uppercase(), 0) - 0x41 + 0x1F1E6
        val secondLetter = Character.codePointAt(countryCode.uppercase(), 1) - 0x41 + 0x1F1E6
        return String(Character.toChars(firstLetter)) + String(Character.toChars(secondLetter))
    }

    override fun getItem(position: Int): String? = if (count > 0) codes[position] else null

    override fun getCount(): Int = codes.size

    class ViewHolder(val binding: CountryItemBinding) {

        private var countryText: TextView = binding.countryTextId

        fun setText(country: String) {
            countryText.text = country
        }
    }

    interface SelectListener {
        fun countrySelect(countryCode: String)
    }

    companion object {
        const val CODE_ALL_COUNTRIES: String = "00"
    }
}