package com.hover.stax.bounty;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

@Entity(tableName = "bountyUser")
public class BountyUser {

	public BountyUser(@NonNull String deviceId, @NotNull String email) {
		this.deviceId = deviceId;
		this.email = email;
	}

	@NonNull
	@PrimaryKey
	@ColumnInfo(name = "deviceId")
	public String deviceId;

	@NonNull
	@ColumnInfo(name = "email")
	public String email;

	@ColumnInfo(name = "isUploaded", defaultValue = "0")
	public boolean isUploaded;

	@ColumnInfo(name = "timestamp", defaultValue = "CURRENT_TIMESTAMP")
	public Long timestamp;

	@ColumnInfo(name = "uploadedTimestamp", defaultValue = "CURRENT_TIMESTAMP")
	public Long uploadedTimestamp;

	protected JSONObject toJson() throws JSONException {
		String format = "{ \"stax_bounty_hunter\": { \"device_id\": " + deviceId + ", \"email\": " + email + "} }";
		return new JSONObject(format);
	}

}
