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
package com.hover.stax.permissions

import android.animation.ObjectAnimator
import android.app.Activity
import android.view.LayoutInflater
import android.view.animation.DecelerateInterpolator
import com.hover.stax.R
import com.hover.stax.views.StaxDialog

class StaxPermissionDialog private constructor(a: Activity, inflater: LayoutInflater) : StaxDialog(a) {

    constructor(a: Activity) : this(a, a.layoutInflater)

    init {
        ctx = a
        mView = inflater.inflate(R.layout.stax_permission_dialog, null)
        customPosListener = null
        customNegListener = null
    }

    fun animateProgressTo(percent: Int) {
        val animator = ObjectAnimator.ofInt(mView.findViewById(R.id.progress_indicator), "progress", percent)
        animator.apply {
            duration = 800
            interpolator = DecelerateInterpolator()
            start()
        }
    }
}