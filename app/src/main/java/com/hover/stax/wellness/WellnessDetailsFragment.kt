package com.hover.stax.wellness

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hover.stax.databinding.FragmentWellnessDetailBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class WellnessDetailsFragment : Fragment() {

    private val viewModel: WellnessViewModel by viewModel()

    private var _binding: FragmentWellnessDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentWellnessDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val id = arguments?.getString(TIP_ID)
        id?.let {
            showTip(it)
        }
    }

    private fun showTip(id: String) {
        val wellnessTip = viewModel.tips.value?.firstOrNull { it.id == id }
        wellnessTip?.let {
            binding.wellnessDetail.setTitle(it.title)
            binding.content.text = it.content
        }
    }

    companion object {
        const val TIP_ID = "tipId"
    }
}