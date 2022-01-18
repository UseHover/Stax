package com.hover.stax.onboarding.variant_one

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.hover.stax.R
import androidx.viewpager.widget.ViewPager
import android.widget.TextView

private data class SlideData (val imgRes : Int, val titleRes: Int)

class SlidesPagerAdapter(private val context: Context) : PagerAdapter(){
    override fun destroyItem(container: ViewGroup, position: Int, arg1: Any) {
        (container as ViewPager).removeView(arg1 as View?)
    }
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.onboarding_variant_one_slide, container, false)

        val slideData = getSlideData(position)
        val textView =   view.findViewById<TextView>(R.id.onboarding_v1_title)
        textView.setText(slideData.titleRes)
        val imageView =   view.findViewById<ImageView>(R.id.onboarding_v1_image)
        imageView.setImageResource(slideData.imgRes)

        container.addView(view)
        return view
    }

    override fun getCount(): Int {return 4 }

    override fun isViewFromObject(view: View, arg1: Any): Boolean {
        return view == arg1 as View?
    }

    private fun getSlideData(position: Int) : SlideData {
        return when(position) {
            0 -> SlideData(R.drawable.img_sms, R.string.onboarding_v1_slide1_title)
            1 -> SlideData(R.drawable.img_copy, R.string.onboarding_v1_slide2_title)
            2 -> SlideData(R.drawable.img_check, R.string.onboarding_v1_slide3_title)
            else -> SlideData(R.drawable.img_whatsapp, R.string.onboarding_v1_slide4_title)
        }
    }

}

