package com.hover.stax.onboarding.slidingVariant

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.hover.stax.R
import com.hover.stax.databinding.OnboardingVariantOneSlideBinding

private data class SlideData(val imgRes: Int, val titleRes: Int, val descRes: Int)

class SlidesPagerAdapter(private val context: Context) : PagerAdapter() {

    override fun destroyItem(container: ViewGroup, position: Int, arg1: Any) {
        (container as ViewPager).removeView(arg1 as View?)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val binding = OnboardingVariantOneSlideBinding.inflate(LayoutInflater.from(context), container, false)

        val slideData = getSlideData(position)
        binding.onboardingV1Title.setText(slideData.titleRes)
        binding.onboardingV1Desc.setText(slideData.descRes)
        binding.onboardingV1Image.setImageResource(slideData.imgRes)

        container.addView(binding.root)
        return binding.root
    }

    override fun getCount(): Int {
        return 4
    }

    override fun isViewFromObject(view: View, arg1: Any): Boolean {
        return view == arg1 as View?
    }

    private fun getSlideData(position: Int): SlideData {
        return when (position) {
            0 -> SlideData(R.drawable.send_illustration, R.string.onboarding_v1_slide1_title, R.string.slide1_desc)
            1 -> SlideData(R.drawable.send_illustration, R.string.onboarding_v1_slide2_title, R.string.slide2_desc)
            2 -> SlideData(R.drawable.request_illustration, R.string.onboarding_v1_slide3_title, R.string.slide3_desc)
            else -> SlideData(R.drawable.airtime_illustration, R.string.onboarding_v1_slide4_title, R.string.slide4_desc)
        }
    }

}

