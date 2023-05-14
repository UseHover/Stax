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
package com.hover.stax.languages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.appsflyer.AppsFlyerLib
import com.hover.stax.R
import com.hover.stax.databinding.FragmentLanguageBinding
import com.hover.stax.core.Utils
import com.yariksoffice.lingver.Lingver

const val LANGUAGE_CHECK = "Language"

class LanguageSelectFragment : Fragment() {

    private var selectedCode: String? = null
    private var _binding: FragmentLanguageBinding? = null
    private val binding get() = _binding!!

    private val languageViewModel: LanguageViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLanguageBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        com.hover.stax.utils.AnalyticsUtil.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_language)), requireActivity())

        selectedCode = Lingver.getInstance().getLanguage()

        val radioGroup = binding.languageRadioGroup.apply {
            setOnCheckedChangeListener { _, checkedId -> onSelect(checkedId) }
        }

        languageViewModel.languages.observe(viewLifecycleOwner) {
            createRadios(it, radioGroup)
        }

        binding.continueLanguageButton.setOnClickListener { onContinue() }
    }

    override fun onStart() {
        super.onStart()
        AppsFlyerLib.getInstance().start(requireActivity())
    }

    private fun createRadios(languages: List<Lang>, radioGroup: RadioGroup) {
        languages.forEachIndexed { index, lang ->
            val radioButton = LayoutInflater.from(requireContext()).inflate(R.layout.stax_radio_button, null) as RadioButton
            with(radioButton) {
                id = index
                text = lang.name
                tag = lang.code
                isChecked = lang.code == selectedCode
            }

            radioGroup.addView(radioButton)
        }
    }

    private fun onSelect(checkedId: Int) {
        val radioButton = view?.findViewById<RadioButton>(checkedId)
        selectedCode = radioButton?.tag.toString()
    }

    private fun onContinue() {
        Lingver.getInstance().setLocale(requireActivity(), selectedCode!!)
        Lang.logChange(selectedCode!!, requireActivity())
        Utils.saveInt(LANGUAGE_CHECK, 1, requireActivity())

        requireActivity().recreate()
        findNavController().popBackStack()
    }
}