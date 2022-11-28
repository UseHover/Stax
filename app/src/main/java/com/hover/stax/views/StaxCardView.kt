package com.hover.stax.views

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.hover.stax.R
import com.hover.stax.databinding.StaxCardViewBinding
import com.hover.stax.utils.UIHelper
import com.hover.stax.utils.UIHelper.loadImage

open class StaxCardView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    private var title: String? = null
    private var subtitle: String? = null
    private var showBack = false
    private var useContextBackPress = false
    private var backDrawable = 0
    private var bgColor = 0
    private var isFlatView: Boolean = false
    private val binding: StaxCardViewBinding

    private fun getAttrs(context: Context, attrs: AttributeSet) {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.StaxCardView, 0, 0)
        try {
            title = a.getString(R.styleable.StaxCardView_title)
            subtitle = a.getString(R.styleable.StaxCardView_subtitle)
            showBack = a.getBoolean(R.styleable.StaxCardView_showBack, false)
            useContextBackPress = a.getBoolean(R.styleable.StaxCardView_defaultBackPress, true)
            backDrawable = a.getResourceId(R.styleable.StaxCardView_backRes, 0)
            bgColor = a.getColor(R.styleable.StaxCardView_staxCardColor, ContextCompat.getColor(context, R.color.colorPrimary))
            isFlatView = a.getBoolean(R.styleable.StaxCardView_isFlatView, true)
        } finally {
            a.recycle()
        }
    }

    private fun makeFlatView() {
        val zero = 0
        binding.cardView.cardElevation = zero.toFloat()
        removeCardMargin()
    }

    @SuppressLint("ResourceType")
    override fun setBackgroundColor(colorRes: Int) {
        bgColor = ContextCompat.getColor(context, colorRes)
        binding.content.setBackgroundColor(bgColor)
    }

    fun showProgressIndicator() {
        binding.progressIndicator.show()
    }

    fun hideProgressIndicator() {
        binding.progressIndicator.hide()
    }

    fun setBackButtonVisibility(visibility: Int) {
        binding.backButton.visibility = visibility
    }

    fun setTitle(t: String?) {
        if (t != null) binding.title.text = t
    }

    fun setTitle(titleString: Int) {
        if (titleString != 0) binding.title.text = context.getString(titleString)
    }

    fun setSubtitle(titleString: String?) {
        if (titleString != null) {
            binding.subtitle.visibility = VISIBLE
            binding.subtitle.text = titleString
        } else binding.subtitle.visibility = GONE
    }

    fun setIcon(icon: Int) {
        if (icon != 0) {
            binding.backButton.setImageResource(icon)
        }
    }

    fun setIcon(iconUrl: String) {
        binding.backButton.loadImage(context, iconUrl)
    }

    fun setOnClickIcon(listener: OnClickListener?) {
        if (listener != null) {
            binding.backButton.setOnClickListener(listener)
        }
    }

    private fun fillFromAttrs() {
        if (title != null) binding.title.text = title else binding.cardHeader.visibility = GONE
        if (subtitle != null) {
            binding.subtitle.text = subtitle
            binding.subtitle.visibility = VISIBLE
        }

        if (useContextBackPress) binding.backButton.setOnClickListener { triggerBack() }
        if (showBack) binding.backButton.visibility = VISIBLE
        if (backDrawable != 0) binding.backButton.setImageResource(backDrawable)

        binding.progressIndicator.setVisibilityAfterHide(View.GONE)

        if (isFlatView) makeFlatView()
    }

    private fun removeCardMargin() {
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        params.setMargins(0, 0, 0, 0)
        binding.cardView.layoutParams = params
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        // Since ViewBinding serves to eliminate null with views,this is the only way to catch this
        try {
            binding.content.addView(child, index, params)
        } catch (npe: NullPointerException) {
            super.addView(child, index, params)
        }
    }

    private fun triggerBack() {
        try {
            (context as Activity).onBackPressed()
        } catch (ignored: Exception) {
        }
    }

    fun updateState(icon: Int, backgroundColor: Int, title: Int) {
        binding.cardView.apply {
            setBackButtonVisibility(View.VISIBLE)
            setIcon(icon)
            setTitle(title)
            setBackgroundColor(backgroundColor)
        }
    }

    init {
        getAttrs(context, attrs)
        binding = StaxCardViewBinding.inflate(LayoutInflater.from(context), this, true)
        fillFromAttrs()
    }
}