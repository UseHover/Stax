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