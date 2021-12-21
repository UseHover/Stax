package com.hover.stax.channels

import android.content.Context
import com.hover.stax.R
import com.hover.stax.database.AppDatabase
import org.json.JSONArray
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object ChannelUtil : KoinComponent {

    private val db: AppDatabase by inject()
    private val channelDao = db.channelDao()

    fun updateChannels(data: JSONArray, context: Context) {
        for (j in 0 until data.length()) {
            var channel = channelDao.getChannel(data.getJSONObject(j).getJSONObject("attributes").getInt("id"))
            if (channel == null) {
                channel = Channel(data.getJSONObject(j).getJSONObject("attributes"), context.getString(R.string.root_url))
                channelDao.insert(channel)
            } else channelDao.update(channel.update(data.getJSONObject(j).getJSONObject("attributes"), context.getString(R.string.root_url)))
        }
    }
}