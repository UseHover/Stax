package com.hover.stax.scheduled;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "schedules")
public class Schedule {

	@PrimaryKey(autoGenerate = true)
	@NonNull
	public int id;

	@NonNull
	@ColumnInfo(name = "type") // request, airtime, p2p
	public String type;

	@NonNull
	@ColumnInfo(name = "channel_id")
	public int channel_id;

	@ColumnInfo(name = "action_id")
	public String action_id;

	@NonNull
	@ColumnInfo(name = "recipient")
	public String recipient;

	@ColumnInfo(name = "amount")
	public String amount;

	@ColumnInfo(name = "reason")
	public String reason;

	@NonNull
	@ColumnInfo(name = "message")
	public String message;

	@NonNull
	@ColumnInfo(name = "start_date", defaultValue = "CURRENT_TIMESTAMP")
	public Long start_date;

	@ColumnInfo(name = "end_date", defaultValue = "CURRENT_TIMESTAMP")
	public Long end_date;

	@ColumnInfo(name = "frequency") // once, weekly, bi-weekly, monthly
	public String frequency;
}
