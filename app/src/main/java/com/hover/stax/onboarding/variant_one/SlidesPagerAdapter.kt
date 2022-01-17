package com.hover.stax.onboarding.variant_one

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.PagerAdapter
import com.hover.stax.R
import androidx.viewpager.widget.ViewPager
import com.hover.stax.databinding.OnboardingVariantOneSlideBinding
import com.hover.stax.utils.UIHelper
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import timber.log.Timber
import java.lang.Exception

private data class SlideData (val imgRes : Int, val titleRes: Int)

class SlidesPagerAdapter(private val context: Context, private val binding : OnboardingVariantOneSlideBinding) : PagerAdapter(), Target {
    override fun destroyItem(container: ViewGroup, position: Int, arg1: Any) {
        super.destroyItem(container, position, arg1)
        (container as ViewPager).removeView(arg1 as View?)
    }
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val slideData = getSlideData(position)
        binding.onboardingV1Title.setText(slideData.titleRes)
        Picasso.get().load(slideData.imgRes).config(Bitmap.Config.RGB_565).into(this)

        return binding.root
    }

    override fun getCount(): Int {return 4 }

    override fun isViewFromObject(view: View, arg1: Any): Boolean {
        return view == arg1 as View?
    }

    private fun getSlideData(position: Int) : SlideData {
        return when(position) {
            0 -> SlideData(R.drawable.img_check, R.string.onboarding_v1_slide1_title)
            1 -> SlideData(R.drawable.img_copy, R.string.onboarding_v1_slide2_title)
            2 -> SlideData(R.drawable.img_sms, R.string.onboarding_v1_slide3_title)
            else -> SlideData(R.drawable.img_whatsapp, R.string.onboarding_v1_slide4_title)
        }
    }

    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
        val d = RoundedBitmapDrawableFactory.create(context.resources, bitmap)
        d.isCircular = true
        binding.onboardingV1Image.setImageDrawable(d)
    }

    override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
        Timber.i("Onboarding slide image bitmap failed due to: $e")
    }
    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
        Timber.i("Onboarding slide image bitmap preparing to load")
    }

}

