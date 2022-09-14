package com.hover.stax.channels

import android.content.Context
import com.hover.stax.R
import com.hover.stax.database.AppDatabase
import org.json.JSONArray
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

object ChannelUtil : KoinComponent {

    private val db: AppDatabase by inject()
    private val channelDao = db.channelDao()

    fun updateChannels(data: JSONArray, context: Context) {
        Timber.e("updating channels. Data length: ${data.length()}")
        for (j in 0 until data.length()) {
            var channel = channelDao.getChannel(data.getJSONObject(j).getJSONObject("attributes").getInt("id"))
            if (channel?.id == 555057)
                Timber.e("found channel with id 555057: ${channel.name}. Published: ${channel.published}")
            if (channel == null) {
                channel = Channel(data.getJSONObject(j).getJSONObject("attributes"), context.getString(R.string.root_url))
                if (channel?.id == 555057)
                    Timber.e("created channel with id 555057: ${channel.name}. Published: ${channel.published}")
                channelDao.insert(channel)
            } else channelDao.update(channel.update(data.getJSONObject(j).getJSONObject("attributes"), context.getString(R.string.root_url)))
        }
    }
}