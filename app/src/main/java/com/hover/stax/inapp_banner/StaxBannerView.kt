package com.hover.stax.inapp_banner

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.hover.stax.databinding.InAppBannerLayoutBinding

class StaxBannerView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    private val binding: InAppBannerLayoutBinding = InAppBannerLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    fun display(banner: Banner) {
        binding.bannerTitle.setText(banner.title)
        banner.desc?.let { binding.bannerDesc.setText(it) }
        binding.cta.setText(banner.cta)
        binding.bannerIcon.setImageResource(banner.iconRes)
        binding.primaryBackground.setBackgroundResource(banner.primaryColor)
        binding.secondayBackground.setBackgroundResource(banner.secondaryColor)
    }
}