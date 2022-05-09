package com.hover.stax.utils

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.request.target.CustomTarget
import com.google.android.material.snackbar.Snackbar
import com.hover.stax.R
import timber.log.Timber

object UIHelper {

    private const val INITIAL_ITEMS_FETCH = 30

    fun flashMessage(context: Context, view: View?, message: String?) {
        if (view == null) flashMessage(context, message) else showSnack(view, message)
    }

    private fun showSnack(view: View, message: String?) {
        val s = Snackbar.make(view, message!!, Snackbar.LENGTH_SHORT)
        s.anchorView = view
        s.show()
    }

    fun flashMessage(context: Context, message: String?) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun flashMessage(context: Context, messageRes: Int) {
        Toast.makeText(context, context.getString(messageRes), Toast.LENGTH_SHORT).show()
    }

    fun setMainLinearManagers(context: Context?): LinearLayoutManager {
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.initialPrefetchItemCount = INITIAL_ITEMS_FETCH
        linearLayoutManager.isSmoothScrollbarEnabled = true
        return linearLayoutManager
    }

    fun getColor(hex: String?, isBackground: Boolean, c: Context?): Int {
        return try {
            Color.parseColor(hex)
        } catch (e: IllegalArgumentException) {
            ContextCompat.getColor(c!!, if (isBackground) R.color.offWhite else R.color.brightBlue)
        }
    }

    fun setFullscreenView(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.window.setDecorFitsSystemWindows(false)
        } else {
            activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        }
    }

    fun setTextUnderline(textView: TextView, cs: String?) {
        val content = SpannableString(cs)
        content.setSpan(UnderlineSpan(), 0, content.length, 0)
        content.setSpan(Typeface.BOLD, 0, content.length, 0)
        try {
            textView.text = content
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    fun loadImage(fragment: Fragment, url: String, imageView: ImageView) = GlideApp.with(fragment)
        .load(url)
        .placeholder(R.drawable.icon_bg_circle)
        .circleCrop()
        .into(imageView)

    fun loadImage(context: Context, url: String, imageView: ImageView) = GlideApp.with(context)
        .load(url)
        .placeholder(R.drawable.icon_bg_circle)
        .circleCrop()
        .into(imageView)

    fun loadImage(context: Context, url: String, target: CustomTarget<Drawable>) = GlideApp.with(context)
        .load(url)
        .placeholder(R.drawable.icon_bg_circle)
        .circleCrop()
        .override(Constants.size55)
        .into(target)
}