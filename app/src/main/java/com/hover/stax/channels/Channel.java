package com.hover.stax.channels;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.hover.stax.R;
import com.hover.stax.data.local.channels.ChannelDao;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import timber.log.Timber;

@Entity(tableName = "channels")
public class Channel implements Comparable<Channel> {
    public static final String BANK_TYPE = "bank", TELECOM_TYPE = "telecom", MOBILE_MONEY = "mmo";

    @PrimaryKey
    @NonNull
    public int id;

    @NonNull
    @ColumnInfo(name = "name")
    public String name;

    @NonNull
    @ColumnInfo(name = "country_alpha2")
    public String countryAlpha2;

    @ColumnInfo(name = "root_code")
    public String rootCode;

    @NonNull
    @ColumnInfo(name = "currency")
    public String currency;

    @NonNull
    @ColumnInfo(name = "hni_list")
    public String hniList;

    @NonNull
    @ColumnInfo(name = "logo_url")
    public String logoUrl;

    @NonNull
    @ColumnInfo(name = "institution_id")
    public int institutionId;

    @NonNull
    @ColumnInfo(name = "primary_color_hex")
    public String primaryColorHex;

    @NonNull
    @ColumnInfo(name = "published", defaultValue = "0")
    public Boolean published;

    @NonNull
    @ColumnInfo(name = "secondary_color_hex")
    public String secondaryColorHex;

    @NonNull
    @ColumnInfo(name = "institution_type", defaultValue = BANK_TYPE)
    public String institutionType;

    @NonNull
    @ColumnInfo(name = "isFavorite", defaultValue = "0")
    public boolean isFavorite;

    public Channel() {}

    public Channel(int _id, String addChannel) {
        this.id = _id;
        this.name = addChannel;
    }

    public Channel(JSONObject jsonObject, Context context) {
        update(jsonObject, context);
    }

    public static void load(JSONArray data, ChannelDao channelDao, Context context) {
        for (int j = 0; j < data.length(); j++) {
            Channel channel = channelDao.getChannel(data.optJSONObject(j).optJSONObject("attributes").optInt("id"));
            if (channel == null) {
                channel = new Channel(data.optJSONObject(j).optJSONObject("attributes"), context);
                channelDao.insert(channel);
            } else channelDao.update(channel.update(data.optJSONObject(j).optJSONObject("attributes"), context));
        }
    }

    Channel update(JSONObject jsonObject, Context context) {
        try {
            id = jsonObject.getInt("id");
            name = jsonObject.getString("name");
            rootCode = jsonObject.getString("root_code");
            countryAlpha2 = jsonObject.getString("country_alpha2").toUpperCase();
            currency = jsonObject.getString("currency");
            hniList = jsonObject.getString("hni_list");
            published = jsonObject.getBoolean("published");
            logoUrl = context.getString(R.string.root_url) + jsonObject.getString("logo_url");
            institutionId = jsonObject.getInt("institution_id");
            primaryColorHex = jsonObject.getString("primary_color_hex");
            secondaryColorHex = jsonObject.getString("secondary_color_hex");
            institutionType = jsonObject.getString("institution_type");
        } catch (JSONException e) {
            Timber.d(e.getLocalizedMessage());
        }
        return this;
    }

    public String getUssdName() {
        return name + " - " + rootCode + " - " + countryAlpha2;
    }

    @Override
    public String toString() {
        return name + " " + countryAlpha2;
    }

    @Override
    public int compareTo(Channel cOther) {
        return this.toString().compareTo(cOther.toString());
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Channel)) return false;
        Channel c = (Channel) other;
        return id == c.id;
    }
}