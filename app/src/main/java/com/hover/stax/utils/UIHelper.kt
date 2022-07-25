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
import android.view.WindowInsetsController
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.request.target.CustomTarget
import com.google.android.material.snackbar.Snackbar
import com.hover.stax.R
import com.hover.stax.domain.model.Account
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch
import timber.log.Timber

object UIHelper {

    private const val INITIAL_ITEMS_FETCH = 30

    fun flashMessage(context: Context, view: View?, message: String) {
        if (view == null) flashMessage(context, message) else showSnack(view, message)
    }

    private fun showSnack(view: View, message: String?) {
        val s = Snackbar.make(view, message!!, Snackbar.LENGTH_LONG)
        s.anchorView = view
        s.show()
    }

    fun flashMessage(context: Context, message: String, isAppError: Boolean = false) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        AnalyticsUtil.logAnalyticsEvent(message, context)
        if(isAppError) AnalyticsUtil.logErrorAndReportToFirebase(context.getString(R.string.toast_err_tag), message, null)
    }

    fun flashMessage(context: Context, messageRes: Int, isAppError: Boolean = false) {
        val message = context.getString(messageRes)
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        AnalyticsUtil.logAnalyticsEvent(message, context)
        if(isAppError) AnalyticsUtil.logErrorAndReportToFirebase(context.getString(R.string.toast_err_tag), message, null)
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

    fun ImageView.loadImage(fragment: Fragment, url: String) = GlideApp.with(fragment)
        .load(url)
        .placeholder(R.drawable.icon_bg_circle)
        .circleCrop()
        .override(80)
        .into(this)

    fun ImageView.loadImage(context: Context, url: String) = GlideApp.with(context)
        .load(url)
        .placeholder(R.drawable.icon_bg_circle)
        .circleCrop()
        .override(80)
        .into(this)

    fun ImageView.loadImage(context: Context, @DrawableRes iconId: Int) = GlideApp.with(context)
        .load(iconId)
        .override(100)
        .into(this)

    fun ImageButton.loadImage(context: Context, url: String) = GlideApp.with(context)
        .load(url)
        .placeholder(R.drawable.icon_bg_circle)
        .circleCrop()
        .into(this)

    fun loadImage(context: Context, url: String, target: CustomTarget<Drawable>) = GlideApp.with(context)
        .load(url)
        .placeholder(R.drawable.icon_bg_circle)
        .circleCrop()
        .override(context.resources.getDimensionPixelSize(R.dimen.logoDiam))
        .into(target)

}

fun <T> Fragment.collectLifecycleFlow(flow: Flow<T>, collector: FlowCollector<T>) {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collect(collector)
        }
    }
}

