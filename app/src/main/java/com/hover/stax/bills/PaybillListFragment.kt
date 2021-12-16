package com.hover.stax.bills

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hover.stax.databinding.FragmentPaybillListBinding

class PaybillListFragment: Fragment() {

    private var _binding: FragmentPaybillListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentPaybillListBinding.inflate(inflater, container, false)
        return binding.root
    }
}