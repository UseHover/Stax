package com.hover.stax.financialTips

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.hover.stax.R
import com.hover.stax.databinding.FragmentWellnessBinding
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.UIHelper
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class FinancialTipsFragment : Fragment(), FinancialTipsAdapter.SelectListener {

    private val viewModel: FinancialTipsViewModel by viewModel()

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
                showFinancialTips(it, tipId)
            }
        }

        initBackNavigation()
    }

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (binding.financialTipsDetail.visibility == View.VISIBLE)
                showTipList()
            else
                findNavController().popBackStack()
        }
    }

    private fun showFinancialTips(tips: List<FinancialTip>, id: String? = null) {
        if (id != null) {
            tips.firstOrNull { it.id == id }?.let { onTipSelected(it, true) }
        } else {
            binding.financialTips.apply {
                layoutManager = UIHelper.setMainLinearManagers(requireActivity())
                isNestedScrollingEnabled = false
                adapter = FinancialTipsAdapter(tips, this@FinancialTipsFragment)
            }

            AnalyticsUtil.logAnalyticsEvent(getString(R.string.visited_financial_tips), requireActivity())
        }
    }

    override fun onTipSelected(tip: FinancialTip, isFromDeeplink: Boolean) {
        logTipRead(tip, isFromDeeplink)

        binding.tipsCard.visibility = View.GONE
        binding.financialTipsDetail.apply {
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
        binding.contentText.text = Html.fromHtml(tip.content)

        binding.shareBtn.setOnClickListener {
            val shareableContent = buildString {
                append(tip.title)
                append("\n\n")
                append(tip.snippet ?: Html.fromHtml(tip.content))
                append(getString(R.string.stax_handle))
                append("\n\n")
                append("https://stax.me/financialTips?id=${tip.id}")
            }

            val share = Intent.createChooser(Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareableContent)
                type = "text/plain"
            }, getString(R.string.share_wellness_tip))
            startActivity(share)

            logTipShare(tip)
        }
    }

    private fun logTipShare(tip: FinancialTip) {
        val data = JSONObject()

        try {
            data.put("tipId", tip.id)
            data.put("date", tip.date)
            data.put("title", tip.title)
        } catch (e: Exception) {
            Timber.e(e)
        }

        AnalyticsUtil.logAnalyticsEvent(getString(R.string.shared_financial_tip), data, requireActivity())
    }

    private fun logTipRead(tip: FinancialTip, isFromDeeplink: Boolean) {
        val data = JSONObject()

        try {
            data.put("tipId", tip.id)
            data.put("date", tip.date)
            data.put("title", tip.title)
            data.put("fromShareLink", isFromDeeplink)
        } catch (e: Exception) {
            Timber.e(e)
        }

        AnalyticsUtil.logAnalyticsEvent(getString(R.string.read_financial_tip), data, requireActivity())
    }

    private fun showTipList() {
        binding.financialTipsDetail.visibility = View.GONE
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