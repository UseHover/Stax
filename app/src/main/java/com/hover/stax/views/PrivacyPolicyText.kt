package com.hover.stax.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.hover.stax.R
import com.hover.stax.databinding.PrivacypolicyBinding
import com.hover.stax.utils.Utils

class PrivacyPolicyText(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    var binding = PrivacypolicyBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        binding.privacyPolicyText.setOnClickListener { Utils.openUrl(R.string.privacy_policy_link, this.context) }
    }
}