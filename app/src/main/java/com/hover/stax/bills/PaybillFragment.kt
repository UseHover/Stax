package com.hover.stax.bills

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hover.stax.databinding.FragmentPaybillBinding

class PaybillFragment : Fragment() {

    private var _binding: FragmentPaybillBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentPaybillBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.saveBillLayout.saveBill.setOnCheckedChangeListener { _, isChecked -> binding.saveBillLayout.saveBillCard.visibility = if (isChecked) View.VISIBLE else View.GONE }
    }
}
