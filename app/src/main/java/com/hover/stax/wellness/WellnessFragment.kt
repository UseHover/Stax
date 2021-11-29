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

        val tipId = arguments?.getString(TIP_ID)

        binding.title.text = getString(R.string.financial_wellness_tips)
        viewModel.tips.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                showWellnessTips(it, tipId)
            }
        }

        initBackNavigation()
    }

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (binding.wellnessDetail.visibility == View.VISIBLE)
                showTipList()
            else
                findNavController().popBackStack()
        }
    }

    private fun showWellnessTips(tips: List<WellnessTip>, id: String? = null) {
        if (id != null) {
            tips.firstOrNull { it.id == id }?.let { onTipSelected(it, true) }
        } else {
            binding.wellnessTips.apply {
                layoutManager = UIHelper.setMainLinearManagers(requireActivity())
                isNestedScrollingEnabled = false
                adapter = WellnessAdapter(tips, this@WellnessFragment)
            }
        }
    }

    override fun onTipSelected(tip: WellnessTip, isFromDeeplink: Boolean) {
        binding.tipsCard.visibility = View.GONE
        binding.wellnessDetail.apply {
            visibility = View.VISIBLE
            setTitle(tip.title)

            //ensures proper back navigation
            setOnClickIcon {
                if (isFromDeeplink)
                    findNavController().navigate(R.id.action_wellnessFragment_to_navigation_home)
                else
                    showTipList()
            }
        }
        binding.contentText.text = tip.content

        binding.shareBtn.setOnClickListener {
            val shareableContent = buildString {
                append(tip.title)
                append("\n\n")
                append(tip.snippet ?: tip.content)
                append(getString(R.string.stax_handle))
                append("\n\n")
                append("https://stax.me/wellnessTips?id=${tip.id}")
            }

            val share = Intent.createChooser(Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareableContent)
                type = "text/plain"
            }, getString(R.string.share_wellness_tip))
            startActivity(share)
        }
    }

    private fun showTipList() {
        binding.wellnessDetail.visibility = View.GONE
        binding.tipsCard.visibility = View.VISIBLE
    }

    private fun initBackNavigation() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backPressedCallback)
        binding.backButton.setOnClickListener { findNavController().popBackStack() }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    companion object {
        const val TIP_ID = "tipId"
    }
}