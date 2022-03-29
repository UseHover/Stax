package com.hover.stax.utils

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.hover.stax.R
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
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

    @JvmStatic
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

    fun loadPicasso(url: String?, size: Int, target: Target?) {
        Picasso.get()
            .load(url)
            .config(Bitmap.Config.RGB_565)
            .resize(size, size).into(target!!)
    }

    fun loadPicasso(url: String?, imageView: ImageView?) {
        Picasso.get().load(url).config(Bitmap.Config.RGB_565).placeholder(R.color.buttonColor).into(imageView)
    }

    fun loadPicasso(resId: Int, size: Int, target: Target?) {
        Picasso.get()
            .load(resId)
            .config(Bitmap.Config.RGB_565)
            .resize(size, size).into(target!!)
    }

    fun loadPicasso(url: String, target: Target) = Picasso.get().load(url).config(Bitmap.Config.RGB_565).into(target)

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
}