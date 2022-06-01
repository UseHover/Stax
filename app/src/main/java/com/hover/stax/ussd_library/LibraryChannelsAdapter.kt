package com.hover.stax.ussd_library

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hover.stax.R
import com.hover.stax.channels.Channel
import com.hover.stax.databinding.LibraryListItemBinding
import com.hover.stax.utils.Utils

class LibraryChannelsAdapter(private val favoriteClickInterface: FavoriteClickInterface) : ListAdapter<Channel, LibraryChannelsAdapter.ViewHolder>(diffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LibraryListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(getItem(holder.adapterPosition))
    }

    inner class ViewHolder(val binding: LibraryListItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bindItems(channel: Channel) {
            with(binding) {
                setFavoriteIcon(favoriteIcon, channel)
                liTitle.text = channel.name
                liButton.apply {
                    text = liButton.context.getString(R.string.dial_btn, channel.rootCode)
                    setOnClickListener { Utils.dial(channel.rootCode, binding.root.context) }
                }
            }
        }
    }

    private fun setFavoriteIcon(favoriteImage: ImageView, channel: Channel ) {
        setFavColorIcon(favoriteImage, channel)
        favoriteImage.setOnClickListener {
            channel.isFavorite = !channel.isFavorite
            setFavColorIcon(favoriteImage, channel)
            favoriteClickInterface.onFavoriteIconClicked(channel)
        }
    }
    private fun setFavColorIcon(favoriteImage: ImageView, channel: Channel) {
        if(channel.isFavorite) favoriteImage.setImageResource(R.drawable.favorite_filled)
        else favoriteImage.setImageResource(R.drawable.favorite_empty);
    }


    interface FavoriteClickInterface {
        fun onFavoriteIconClicked(channel: Channel);
    }

    companion object {
        private val diffUtil = object: DiffUtil.ItemCallback<Channel>() {
            override fun areItemsTheSame(oldItem: Channel, newItem: Channel): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Channel, newItem: Channel): Boolean {
                return oldItem == newItem
            }
        }
    }
}