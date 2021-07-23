package com.hover.stax.onboarding

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.hover.stax.R

class SlidesPagerAdapter(fragmentManager: FragmentManager, behaviour: Int) : FragmentPagerAdapter(fragmentManager, behaviour) {

    override fun getCount(): Int = SLIDES_SIZE

    override fun getItem(position: Int): Fragment = when (position) {
        0 -> SlidesFragment.newInstance(R.layout.slide1_fragment)
        1 -> SlidesFragment.newInstance(R.layout.slide2_fragment)
        2 -> SlidesFragment.newInstance(R.layout.slide3_fragment)
        else -> Fragment()
    }

    companion object {
        const val SLIDES_SIZE = 3
    }
}