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
package com.hover.stax.views

import android.content.Context
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout
import com.hover.stax.core.Utils

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