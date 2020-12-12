package com.hover.stax.actions;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.hover.stax.R;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

// This Entity reads the SDK's database, so the fields below have to match the SDK's SQL definition EXACTLY
// since the SDK does not currently use Room
@Entity(tableName = "hsdk_actions")
public class Action {
	public final static String ID_KEY = "action_id";
	public final static String TRANSACTION_TYPE = "transaction_type", P2P = "p2p", AIRTIME = "airtime", ME2ME = "me2me", C2B = "c2b", BALANCE = "balance";
	public final static String STEP_IS_PARAM = "is_param", STEP_VALUE = "value",
			PIN_KEY = "pin", AMOUNT_KEY = "amount", PHONE_KEY = "phone", ACCOUNT_KEY = "account", FEE_KEY = "fee", NOTE_KEY = "reason";

	@PrimaryKey
	@ColumnInfo(name = "_id")
	public int id;

	@NonNull
	@ColumnInfo(name = "server_id")
	public String public_id;

	@NonNull
	@ColumnInfo(name = "channel_id")
	public int channel_id;

	@NonNull
	@ColumnInfo(name = "transaction_type")
	public String transaction_type;

	@NonNull
	@ColumnInfo(name = "from_institution_id")
	public int from_institution_id;

	@ColumnInfo(name = "to_institution_id")
	public Integer to_institution_id;

	@NonNull
	@ColumnInfo(name = "from_institution_name")
	public String from_institution_name;

	@ColumnInfo(name = "to_institution_name")
	public String to_institution_name;

	@ColumnInfo(name = "to_institution_logo")
	public String to_institution_logo;

	@NonNull
	@ColumnInfo(name = "network_name")
	public String network_name;

	@NonNull
	@ColumnInfo(name = "hni_list")
	public String hni_list;

	@ColumnInfo(name = "country_alpha2")
	public String country_alpha2;

	@ColumnInfo(name = "custom_steps")
	public String custom_steps;

	@ColumnInfo(name = "transport_type", defaultValue = "ussd")
	public String transport_type;

	@ColumnInfo(name = "created_timestamp", defaultValue = "CURRENT_TIMESTAMP")
	public Long created_timestamp;

	@ColumnInfo(name = "updated_timestamp", defaultValue = "CURRENT_TIMESTAMP")
	public Long updated_timestamp;

	@ColumnInfo(name = "responses_are_sms")
	public Integer responses_are_sms;

	@ColumnInfo(name = "root_code")
	public String root_code;

	@NotNull
	@Override
	public String toString() {
		return isOnNetwork() ? from_institution_name : to_institution_name;
	}

	public boolean isOnNetwork() {
		return to_institution_name == null || to_institution_name.equals("null") || from_institution_id == to_institution_id;
	}

	public String getLabel(Context c) {
		if (transaction_type.equals(AIRTIME)) return requiresRecipient() ?  c.getString(R.string.other_choice) : c.getString(R.string.self_choice);
		return this.toString();
	}

	public boolean hasToInstitution() {
		return to_institution_name != null && !to_institution_name.equals("null");
	}

	public boolean hasDiffToInstitution() {
		return hasToInstitution() && from_institution_id != to_institution_id;
	}

	public int recipientInstitutionId() {
		return hasToInstitution() ? to_institution_id : channel_id;
	}

	public boolean requiresRecipient() {
		return requiresInput(ACCOUNT_KEY) || requiresInput(PHONE_KEY);
	}

	public boolean allowsNote() {
		return requiresInput(NOTE_KEY);
	}

	private boolean requiresInput(String key) {
		try {
			JSONArray steps = new JSONArray(custom_steps);
			for (int s = 0; s < steps.length(); s++) {
				JSONObject step = steps.optJSONObject(s);
				if (step != null && Boolean.TRUE.equals(step.optBoolean(STEP_IS_PARAM)) && step.optString(STEP_VALUE).equals(key))
					return true;
			}
		} catch (JSONException e) {
		}
		return false;
	}

	public List<String> getRequiredParams() {
		List<String> params = new ArrayList<>();
		try {
			JSONArray steps = new JSONArray(custom_steps);
			for (int s = 0; s < steps.length(); s++) {
				JSONObject step = steps.optJSONObject(s);
				if (step != null && Boolean.TRUE.equals(step.optBoolean(STEP_IS_PARAM))) {
					params.add(step.optString(STEP_VALUE));
				}
			}
		} catch (JSONException e) {
		}
		return params;
	}
}
