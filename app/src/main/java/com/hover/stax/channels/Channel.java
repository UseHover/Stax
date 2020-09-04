package com.hover.stax.channels;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.json.JSONException;
import org.json.JSONObject;

@Entity(tableName = "channels")
public class Channel {

	public Channel(JSONObject jsonObject) {
		try {
			this.id = jsonObject.getInt("id");
			this.name = jsonObject.getString("name");
			this.countryAlpha2 = jsonObject.getString("country_alpha2").toUpperCase();
			this.hniList = jsonObject.getString("hni_list");
			this.primaryColorHex = jsonObject.getString("primary_color_hex");
			this.secondaryColorHex = jsonObject.getString("secondary_color_hex");
		} catch (JSONException e) {
			Log.d("exception", e.getMessage());
		}
	}

	public Channel(int id, String name, String countryAlpha2, String hniList, String primaryColorHex, String secondaryColorHex) {
		this.id = id;
		this.name = name;
		this.countryAlpha2 = countryAlpha2;
		this.hniList = hniList;
		this.primaryColorHex = primaryColorHex;
		this.secondaryColorHex = secondaryColorHex;
	}

	@PrimaryKey
	@NonNull
	public int id;

	@NonNull
	@ColumnInfo(name = "name")
	public String name;

	@NonNull
	@ColumnInfo(name = "country_alpha2")
	public String countryAlpha2;

	@ColumnInfo(name = "hni_list")
	public String hniList;

	@NonNull
	@ColumnInfo(name = "primary_color_hex")
	public String primaryColorHex;

	@ColumnInfo(name = "secondary_color_hex")
	public String secondaryColorHex;

	@NonNull
	@ColumnInfo(name = "selected", defaultValue = "false")
	public boolean selected;

	@NonNull
	@ColumnInfo(name = "defaultAccount", defaultValue = "false")
	public boolean defaultAccount;

	@ColumnInfo(name = "pin")
	public String pin;
}
