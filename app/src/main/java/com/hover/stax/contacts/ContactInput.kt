/*
 * Copyright 2022 Stax
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hover.stax.contacts

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.AutoCompleteTextView
import com.hover.stax.R
import com.hover.stax.database.models.StaxContact
import com.hover.stax.databinding.ContactInputBinding
import com.hover.stax.views.AbstractAutocompleteInput
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