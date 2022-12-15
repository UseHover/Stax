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