package com.hover.stax.financialTips

import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.appsflyer.internal.by
import com.hover.stax.R
import com.hover.stax.databinding.FragmentWellnessBinding
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.UIHelper
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class FinancialTipsFragment : Fragment(), FinancialTipsAdapter.SelectListener {

    private val viewModel: FinancialTipsViewModel by viewModel()
    private val args: FinancialTipsFragmentArgs by navArgs()

    private var _binding: FragmentWellnessBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWellnessBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tipId = args.tipId

        binding.title.text = getString(R.string.financial_wellness_tips)
        viewModel.tips.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                showFinancialTips(it, tipId)
            }
        }
    }

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (binding.financialTipsDetail.visibility == View.VISIBLE)
                showTipList()
            else
                findNavController().popBackStack(R.id.navigation_home, true)
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

        initBackNavigation()
    }

    override fun onTipSelected(tip: FinancialTip, isFromDeeplink: Boolean) {
        logTipRead(tip, isFromDeeplink)

        binding.tipsCard.visibility = View.GONE
        binding.financialTipsDetail.apply {
            visibility = View.VISIBLE
            setTitle(tip.title)

            //ensures proper back navigation
            setOnClickIcon {
                showTipList()
            }
        }

        binding.contentText.apply {
            text = HtmlCompat.fromHtml(tip.content, HtmlCompat.FROM_HTML_MODE_LEGACY)
            movementMethod = LinkMovementMethod.getInstance()
        }

        binding.shareBtn.setOnClickListener {
            val shareableContent = buildString {
                append(tip.title)
                append("\n\n")
                append(tip.snippet ?: HtmlCompat.fromHtml(tip.content, HtmlCompat.FROM_HTML_MODE_LEGACY))
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

        if (!viewModel.tips.value.isNullOrEmpty())
            showFinancialTips(viewModel.tips.value!!, null)
    }

    private fun initBackNavigation() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backPressedCallback)
        binding.backButton.setOnClickListener { findNavController().navigate(R.id.action_wellnessFragment_to_navigation_home) }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    companion object {
        const val TIP_ID = "tipId"
    }
}