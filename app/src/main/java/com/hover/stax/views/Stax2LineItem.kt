package com.hover.stax.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import com.hover.stax.R
import com.hover.stax.contacts.StaxContact
import com.hover.stax.databinding.Stax2lineitemBinding

class Stax2LineItem(context: Context, attrs: AttributeSet?) : RelativeLayout(context, attrs) {

    private val binding: Stax2lineitemBinding = Stax2lineitemBinding.inflate(LayoutInflater.from(context), this, true)

    fun setContent(title: String?, sub: String?) {
        setTitle(title)
        setSubtitle(sub)
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
        if (contact == null) return
        setTitle(contact.shortName())
        if (contact.shortName() != null && contact.shortName() != contact.accountNumber) setSubtitle(contact.accountNumber)
    }

    init {
        binding.title.textAlignment = TEXT_ALIGNMENT_TEXT_END
        binding.subtitle.textAlignment = TEXT_ALIGNMENT_TEXT_END
        binding.subtitle.setTextColor(ContextCompat.getColor(context, R.color.offWhite))
    }
}