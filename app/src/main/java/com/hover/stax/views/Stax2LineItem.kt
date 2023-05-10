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
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import com.hover.stax.R
import com.hover.stax.database.models.StaxContact
import com.hover.stax.databinding.Stax2lineitemBinding

class Stax2LineItem(context: Context, attrs: AttributeSet?) : RelativeLayout(context, attrs) {

    private val binding: Stax2lineitemBinding = Stax2lineitemBinding.inflate(LayoutInflater.from(context), this, true)

    fun setContent(title: String?, sub: String?) {
        if (title == null) {
            setTitle(sub)
            setSubtitle("")
        } else {
            setTitle(title)
            setSubtitle(sub)
        }
    }

    fun setTitle(title: String?) {
        if (title != null) binding.title.text = title
    }

    fun setSubtitle(sub: String?) {
        if (sub != null) {
            binding.subtitle.text = sub
            binding.subtitle.visibility = VISIBLE
        }
    }

    fun setContact(contact: StaxContact?) {
        if (contact == null) {
            setContent("", "")
            return
        }
        setTitle(contact.shortName())
        if (contact.shortName() != null && contact.shortName() != contact.accountNumber) setSubtitle(contact.accountNumber)
        else setSubtitle("")
    }

    init {
        binding.title.textAlignment = TEXT_ALIGNMENT_TEXT_END
        binding.subtitle.textAlignment = TEXT_ALIGNMENT_TEXT_END
        binding.subtitle.setTextColor(ContextCompat.getColor(context, R.color.offWhite))
    }
}