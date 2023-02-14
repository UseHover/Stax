package com.hover.stax.channels

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import com.hover.stax.R
import com.hover.stax.data.local.channels.ChannelDao
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.util.*

class Channel : Comparable<Channel> {
    @PrimaryKey
    var id = 0

    @ColumnInfo(name = "name")
    var name: String = null

    @ColumnInfo(name = "country_alpha2")
    var countryAlpha2: String = null

    @ColumnInfo(name = "root_code")
    var rootCode: String? = null

    @ColumnInfo(name = "currency")
    var currency: String = null

    @ColumnInfo(name = "hni_list")
    var hniList: String = null

    @ColumnInfo(name = "logo_url")
    var logoUrl: String = null

    @ColumnInfo(name = "institution_id")
    var institutionId = 0

    @ColumnInfo(name = "primary_color_hex")
    var primaryColorHex: String = null

    @ColumnInfo(name = "published", defaultValue = "0")
    var published: Boolean = null

    @ColumnInfo(name = "secondary_color_hex")
    var secondaryColorHex: String = null

    @ColumnInfo(name = "institution_type", defaultValue = BANK_TYPE)
    var institutionType: String = null

    @ColumnInfo(name = "isFavorite", defaultValue = "0")
    var isFavorite = false

    constructor() {}
    constructor(_id: Int, addChannel: String) {
        id = _id
        name = addChannel
    }

    constructor(jsonObject: JSONObject, context: Context) {
        update(jsonObject, context)
    }

    fun update(jsonObject: JSONObject, context: Context): Channel {
        try {
            id = jsonObject.getInt("id")
            name = jsonObject.getString("name")
            rootCode = jsonObject.getString("root_code")
            countryAlpha2 = jsonObject.getString("country_alpha2")
            currency = jsonObject.getString("currency")
            hniList = jsonObject.getString("hni_list")
            published = jsonObject.getBoolean("published")
            logoUrl = context.getString(R.string.root_url) + jsonObject.getString("logo_url")
            institutionId = jsonObject.getInt("institution_id")
            primaryColorHex = jsonObject.getString("primary_color_hex")
            secondaryColorHex = jsonObject.getString("secondary_color_hex")
            institutionType = jsonObject.getString("institution_type")
        } catch (e: JSONException) {
            Timber.d(e.localizedMessage)
        }
        return this
    }

    val ussdName: String
        get() = name + " - " + rootCode + " - " + countryAlpha2.uppercase(Locale.getDefault())

    override fun toString(): String {
        return name + " " + countryAlpha2.uppercase(Locale.getDefault())
    }

    override fun compareTo(cOther: Channel): Int {
        return this.toString().compareTo(cOther.toString())
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Channel) return false
        return id == other.id
    }

    companion object {
        const val BANK_TYPE = "bank"
        const val TELECOM_TYPE = "telecom"
        const val MOBILE_MONEY = "mmo"
        fun load(data: JSONArray, channelDao: ChannelDao, context: Context) {
            for (j in 0 until data.length()) {
                var channel = channelDao.getChannel(
                    data.optJSONObject(j).optJSONObject("attributes").optInt("id")
                )
                if (channel == null) {
                    channel = Channel(data.optJSONObject(j).optJSONObject("attributes"), context)
                    channelDao.insert(channel)
                } else channelDao.update(
                    channel.update(
                        data.optJSONObject(j).optJSONObject("attributes"), context
                    )
                )
            }
        }
    }
}