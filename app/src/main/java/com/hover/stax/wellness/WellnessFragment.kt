package com.hover.stax.wellness

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.hover.stax.R
import com.hover.stax.databinding.FragmentWellnessBinding
import com.hover.stax.utils.UIHelper

class WellnessFragment : Fragment(), WellnessAdapter.SelectListener {

    private val viewModel: WellnessViewModel by viewModels()

    private var _binding: FragmentWellnessBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWellnessBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getTips()
        viewModel.tips.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                showWellnessTips(it)
            }
        }
    }

    private fun showWellnessTips(tips: List<WellnessTip>) {
        binding.wellnessTips.apply {
            layoutManager = UIHelper.setMainLinearManagers(requireActivity())
            adapter = WellnessAdapter(tips, this@WellnessFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    override fun onTipSelected(id: String) {
        findNavController().navigate(R.id.action_wellnessFragment_to_wellnessDetailsFragment, bundleOf(WellnessDetailsFragment.TIP_ID to id))
    }
}