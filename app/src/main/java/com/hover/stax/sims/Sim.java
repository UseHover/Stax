package com.hover.stax.sims;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

// This Entity reads the SDK's database, so the fields below have to match the SDK's SQL definition EXACTLY
// since the SDK does not currently use Room
@Entity(tableName = "hsdk_sims")
public class Sim {

	@PrimaryKey
	@ColumnInfo(name = "_id")
	public int id;

	@NonNull
	@ColumnInfo(name = "iccid")
	public String iccid;

	@ColumnInfo(name = "slot_idx")
	public int slot_idx;

	@ColumnInfo(name = "sub_id")
	public int sub_id;

	@ColumnInfo(name = "imei")
	public String imei;

	@ColumnInfo(name = "state", defaultValue = "-1")
	public int state;

	@NonNull
	@ColumnInfo(name = "imsi")
	public String imsi;

	@NonNull
	@ColumnInfo(name = "mcc")
	public String mcc;

	@ColumnInfo(name = "mnc")
	public String mnc;

	@ColumnInfo(name = "hni")
	public String hni;

	@ColumnInfo(name = "operator_name")
	public String operator_name;

	@ColumnInfo(name = "country_iso")
	public String country_iso;

	@ColumnInfo(name = "is_roaming", defaultValue = "0")
	public int is_roaming;

	@ColumnInfo(name = "network_code")
	public String network_code;

	@ColumnInfo(name = "network_name")
	public String network_name;

	@ColumnInfo(name = "network_country")
	public String network_country;

	@ColumnInfo(name = "network_type")
	public Integer network_type;
}
