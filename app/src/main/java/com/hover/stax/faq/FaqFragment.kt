/*
 * Copyright 2022 Stax
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hover.stax.faq

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import com.hover.sdk.api.Hover
import com.hover.stax.R
import com.hover.stax.databinding.FragmentFaqBinding
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.UIHelper
import com.hover.stax.utils.network.NetworkMonitor
import org.koin.androidx.viewmodel.ext.android.viewModel

class FaqFragment : Fragment(), FAQAdapter.SelectListener {

    private var _binding: FragmentFaqBinding? = null
    private val binding get() = _binding!!

    private val faqViewModel: FaqViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        AnalyticsUtil.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.FAQs)), requireContext())
        _binding = FragmentFaqBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeFAQRecycler()
    }

    private fun observeFAQRecycler() {
        val faqRecyclerView = binding.faqRecyclerView
        faqRecyclerView.layoutManager = UIHelper.setMainLinearManagers(requireContext())

        faqViewModel.faqLiveData.observe(viewLifecycleOwner) { faqs ->
            faqs?.let {
                if (it.isEmpty()) {
                    if (NetworkMonitor(requireActivity()).isNetworkConnected) updateLoadingStatus(Status.FAILED)
                    else updateLoadingStatus(Status.FAILED_NO_INTERNET)
                } else {
                    updateLoadingStatus(Status.SUCCESS)
                    val faqAdapter = FAQAdapter(faqs, this@FaqFragment)
                    faqRecyclerView.adapter = faqAdapter
                }
            } ?: updateLoadingStatus(Status.LOADING)
        }
    }

    private fun setShowingContent(showing: Boolean) {
        if (showing) {
            binding.faqRecyclerView.visibility = View.GONE
            binding.faqContentId.visibility = View.VISIBLE
            binding.faqListCard.setOnClickIcon { setShowingContent(false) }
        } else {
            binding.faqRecyclerView.visibility = View.VISIBLE
            binding.faqContentId.visibility = View.GONE
            binding.faqListCard.setTitle(R.string.faq_title)
            binding.faqListCard.setOnClickIcon { requireActivity().onBackPressed() }
        }
    }

    private fun showResponseText(resId: Int) {
        binding.responseText.setText(resId)
        binding.responseText.visibility = View.VISIBLE
        binding.faqRecyclerView.visibility = View.GONE
    }

    private fun updateLoadingStatus(status: Status) {
        when (status) {
            Status.SUCCESS -> {
                binding.responseText.visibility = View.GONE
                binding.faqRecyclerView.visibility = View.VISIBLE
            }
            Status.LOADING -> showResponseText(R.string.loading)
            Status.FAILED -> showResponseText(R.string.loading_error)
            Status.FAILED_NO_INTERNET -> showResponseText(R.string.faq_internet_error)
        }
    }

    private enum class Status {
        SUCCESS, LOADING, FAILED, FAILED_NO_INTERNET
    }

    override fun onTopicClicked(faq: FAQ) {
        binding.faqListCard.setTitle(faq.topic)
        binding.faqContentId.text = HtmlCompat.fromHtml(getString(R.string.faq_content, faq.content, deviceId()), HtmlCompat.FROM_HTML_MODE_LEGACY)
        binding.faqContentId.movementMethod = LinkMovementMethod.getInstance()
        setShowingContent(true)
    }

    private fun deviceId(): String {
        val id: String = Hover.getDeviceId(requireContext())
        return if (id == "null") return "" else id
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}