package com.hover.stax.channels

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import com.hover.stax.R
import com.hover.stax.utils.Constants.size55
import com.hover.stax.utils.UIHelper
import com.hover.stax.views.StaxDropdownLayout
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target

class ChannelDropdown(context: Context, attrs: AttributeSet) : StaxDropdownLayout(context, attrs), Target {

    private var showSelected = false
    private var initialHelperText: String? = null
    private var highlightedChannel: Channel? = null
    private var highlightListener: HighlightListener? = null

    init {
        getAttrs(context, attrs)
    }

    private fun getAttrs(context: Context, attrs: AttributeSet) {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.ChannelDropdown, 0, 0)

        try {
            showSelected = a.getBoolean(R.styleable.ChannelDropdown_show_selected, true)
            initialHelperText = a.getString(R.styleable.ChannelDropdown_initial_helper_text)
        } finally {
            a.recycle()
        }
    }

    fun setListener(listener: HighlightListener) {
        highlightListener = listener
    }

    fun channelUpdateIfNull(channels: List<Channel>) {
        if(!channels.isNullOrEmpty() && !hasExistingContent()){
            setState(context.getString(R.string.channels_error_nosim), INFO)
            updateChoices(channels)
        } else if(!hasExistingContent())
            setEmptyState()
    }

    private fun hasExistingContent(): Boolean = autoCompleteTextView.adapter != null && autoCompleteTextView.adapter.count > 0

    private fun setEmptyState() {
        autoCompleteTextView.dropDownHeight = 0
        setState(context.getString(R.string.channels_error_nodata), ERROR)
    }

    private fun setDropdownValue(c: Channel?) {
        autoCompleteTextView.setText(c?.toString() ?: "", false)
        c?.let { UIHelper.loadPicasso(it.logoUrl, size55, this) }
    }

    private fun updateChoices(channels: List<Channel>) {
        if(highlightedChannel == null) setDropdownValue(null)

        val channelsDropdownAdapter = ChannelsDropdownAdapter(Channel.sort(channels, showSelected), context)
        autoCompleteTextView.apply {
            setAdapter(channelsDropdownAdapter)
            dropDownHeight = UIHelper.dpToPx(300)
            setOnItemClickListener { parent, _, position, _ -> onSelect(parent.getItemAtPosition(position) as Channel) }
        }

        for (channel in channels){
            if(channel.defaultAccount && showSelected)
                setDropdownValue(channel)
        }
    }

    private fun onSelect(c: Channel) {
        setDropdownValue(c)

        highlightListener?.highlightChannel(c)
        highlightedChannel = c
    }

    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
        val d = RoundedBitmapDrawableFactory.create(context.resources, bitmap)
        d.isCircular = true
        autoCompleteTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(d, null, null, null)
    }

    override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
    }

    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
        autoCompleteTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_grey_circle_small, 0, 0, 0)
    }

    interface HighlightListener {
        fun highlightChannel(channel: Channel)
    }
}