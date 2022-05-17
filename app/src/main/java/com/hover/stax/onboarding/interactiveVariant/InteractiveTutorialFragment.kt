package com.hover.stax.onboarding.interactiveVariant

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.hover.stax.R
import com.hover.stax.databinding.FragmentInteractiveTutorialBinding
import com.hover.stax.onboarding.OnBoardingActivity
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.NavUtil
import org.json.JSONObject
import timber.log.Timber

internal class InteractiveTutorialFragment : Fragment() {

    private var _binding: FragmentInteractiveTutorialBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentInteractiveTutorialBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AnalyticsUtil.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_interactive_tutorial)), requireActivity())

        setTopBarClicks()
        setContinueClick()
    }

    private fun setTopBarClicks() {
        binding.backButton.setOnClickListener { findNavController().popBackStack() }
        binding.skipTutorial.setOnClickListener {
            AnalyticsUtil.logAnalyticsEvent(getString(R.string.clicked_skip_tutorial), requireActivity())
            (activity as OnBoardingActivity).checkPermissionsAndNavigate()
        }
    }

    private fun setContinueClick() = binding.continueBtn.setOnClickListener {
        val variation = if (isNoneApply()) 3 else 2
        logOptionsSelected()
        NavUtil.navigate(findNavController(), InteractiveTutorialFragmentDirections.actionInteractiveTutorialFragmentToWelcomeFragment(variation))
    }

    private fun logOptionsSelected() {
        val checkBox1 = binding.variant2Checkbox1
        val checkBox2 = binding.variant2Checkbox2
        val checkBox3 = binding.variant2Checkbox3

        val data = JSONObject()
        try {
            data.put(checkBox1.text.toString(), checkBox1.isChecked)
            data.put(checkBox2.text.toString(), checkBox2.isChecked)
            data.put(checkBox3.text.toString(), checkBox3.isChecked)
        } catch (e: Exception) {
            Timber.e(e)
        }

        AnalyticsUtil.logAnalyticsEvent(getString(R.string.interacted_with_tutorial), data, requireActivity())
    }

    private fun isNoneApply(): Boolean {
        return !binding.variant2Checkbox1.isChecked &&
                !binding.variant2Checkbox2.isChecked &&
                !binding.variant2Checkbox3.isChecked
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}