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
import com.hover.stax.views.AbstractStatefulInput
import com.hover.stax.views.StaxDropdownLayout


class ContactInput(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private val binding = ContactInputBinding.inflate(LayoutInflater.from(context), this, true)

    private var contactInputLayout: StaxDropdownLayout = binding.contactDropdownLayout
    private var contactAutocomplete: AutoCompleteTextView = binding.contactDropdownLayout.findViewById(R.id.autoCompleteView)

    init {
        contactAutocomplete.apply {
            setOnFocusChangeListener { _, hasFocus -> setState(hasFocus) }
            setOnClickListener { Utils.showSoftKeyboard(context, it) }
            imeOptions = EditorInfo.IME_ACTION_DONE
        }
    }

    fun setRecent(contacts: List<StaxContact>, c: Context) {
        val adapter = StaxContactArrayAdapter(c, contacts)
        contactAutocomplete.setAdapter(adapter)
    }

    fun setSelected(contact: StaxContact?) {
        if (contact != null) setText(contact.toString(), false)
    }

    fun setText(number: String?, filter: Boolean) {
        if (!number.isNullOrEmpty()) setState(null, AbstractStatefulInput.SUCCESS)
        contactAutocomplete.setText(number, filter)
    }

    fun setHint(hint: String?) {
        contactInputLayout.setHint(hint)
    }

    fun setAutocompleteClickListener(listener: OnItemClickListener?) {
        contactAutocomplete.onItemClickListener = listener
    }

    fun setChooseContactListener(listener: OnClickListener?) {
        binding.contactButton.setOnClickListener(listener)
    }

    fun addTextChangedListener(listener: TextWatcher?) {
        contactAutocomplete.addTextChangedListener(listener)
    }

    fun setState(message: String?, state: Int) {
        contactInputLayout.setState(message, state)
    }

    private fun setState(hasFocus: Boolean) {
        if (!hasFocus) contactInputLayout.setState(
            null,
            if (!contactAutocomplete.text.isNullOrEmpty()) AbstractStatefulInput.SUCCESS else AbstractStatefulInput.NONE
        )
    }
}