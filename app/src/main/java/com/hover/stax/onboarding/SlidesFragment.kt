package com.hover.stax.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment

class SlidesFragment : Fragment() {

    private var resLayout: Int? = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let { resLayout = arguments?.getInt(TAG, 0) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(resLayout!!, container, false)
    }

    companion object {
        val TAG: String = SlidesFragment::class.java.simpleName

        fun newInstance(resLayout: Int): SlidesFragment = SlidesFragment().apply {
            arguments = bundleOf(TAG to resLayout)
        }
    }
}
