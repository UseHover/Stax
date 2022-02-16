package com.hover.stax.channels

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import com.hover.stax.databinding.StaxSpinnerItemWithLogoBinding
import com.hover.stax.utils.Constants
import com.hover.stax.utils.UIHelper
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import timber.log.Timber

class ChannelsDropdownAdapter(private val channelList: List<Channel>, context: Context) : ArrayAdapter<Channel>(context, 0, channelList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val c = channelList[position]
        var view = convertView
        val viewHolder: ViewHolder

        if (view == null) {
            val binding = StaxSpinnerItemWithLogoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            view = binding.root
            viewHolder = ViewHolder(binding)
        } else
            viewHolder = view.tag as ViewHolder

        viewHolder.setChannel(c)
        return view
    }

    override fun getCount(): Int = channelList.size

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getItem(position: Int): Channel? = if (channelList.isEmpty()) null else channelList[position]

    inner class ViewHolder(val binding: StaxSpinnerItemWithLogoBinding) : Target {

        fun setChannel(channel: Channel) {
            binding.serviceItemNameId.text = channel.toString()
            UIHelper.loadPicasso(channel.logoUrl, Constants.size55, this)
        }

        override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
            val d = RoundedBitmapDrawableFactory.create(binding.root.context.resources, bitmap)
            d.isCircular = true
            binding.serviceItemImageId.setImageDrawable(d)
        }

        override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
            Timber.e(e)
        }

        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}

    }
}