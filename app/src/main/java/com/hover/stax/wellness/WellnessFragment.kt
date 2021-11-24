package com.hover.stax.wellness

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.hover.stax.R
import com.hover.stax.databinding.FragmentWellnessBinding
import com.hover.stax.utils.UIHelper
import org.koin.androidx.viewmodel.ext.android.viewModel

class WellnessFragment : Fragment(), WellnessAdapter.SelectListener {

    private val viewModel: WellnessViewModel by viewModel()

    private var _binding: FragmentWellnessBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWellnessBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.tips.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                showWellnessTips(it)
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backPressedCallback)
    }

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (binding.wellnessDetail.visibility == View.VISIBLE)
                showTipList()
            else
                findNavController().popBackStack()
        }
    }

    private fun showWellnessTips(tips: List<WellnessTip>) {
        binding.wellnessTips.apply {
            layoutManager = UIHelper.setMainLinearManagers(requireActivity())
            isNestedScrollingEnabled = false
            adapter = WellnessAdapter(tips, this@WellnessFragment)
        }
    }

    override fun onTipSelected(tip: WellnessTip) {
        binding.tipsCard.visibility = View.GONE
        binding.wellnessDetail.apply {
            visibility = View.VISIBLE
            setTitle(tip.title)
            setOnClickIcon { showTipList() }
        }
        binding.contentText.text = tip.content

        binding.shareBtn.setOnClickListener {
            val share = Intent.createChooser(Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TITLE, tip.title)
                putExtra(Intent.EXTRA_TEXT, tip.title.plus(" \n\n ").plus(tip.content))
                type = "text/plain"
            }, getString(R.string.share_wellness_tip))
            startActivity(share)
        }
    }

    private fun showTipList() {
        binding.wellnessDetail.visibility = View.GONE
        binding.tipsCard.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}