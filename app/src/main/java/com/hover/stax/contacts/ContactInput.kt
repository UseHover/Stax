package com.hover.stax.contacts

import android.content.Context
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView.OnItemClickListener
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout
import com.hover.stax.R
import com.hover.stax.databinding.ContactInputBinding
import com.hover.stax.utils.Utils
import com.hover.stax.views.AbstractAutocompleteInput
import com.hover.stax.views.AbstractStatefulInput
import com.hover.stax.views.StaxDropdownLayout


class ContactInput(context: Context, attrs: AttributeSet) : AbstractAutocompleteInput(context, attrs) {

    val binding = ContactInputBinding.inflate(LayoutInflater.from(context), this, true)

    override var inputLayout: StaxDropdownLayout = binding.contactDropdownLayout
    override var autocomplete: AutoCompleteTextView = binding.contactDropdownLayout.findViewById(R.id.autoCompleteView)

    init {
        initUI()
    }

    fun setRecent(contacts: List<StaxContact>, c: Context) {
        val adapter = StaxContactArrayAdapter(c, contacts)
        autocomplete.setAdapter(adapter)
    }

    fun setSelected(contact: StaxContact?) {
        if (contact != null) setText(contact.toString(), false)
    }

    fun setChooseContactListener(listener: OnClickListener?) {
        binding.contactButton.setOnClickListener(listener)
    }
}