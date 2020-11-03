package com.hover.stax.requestAccount;

import android.content.Context;

import com.amplitude.api.Amplitude;
import com.hover.stax.R;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

class SupportedCountries {
	public String  name;

	public SupportedCountries(String name) {
		this.name = name;
	}

	@NotNull
	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof SupportedCountries)) return false;
		SupportedCountries c = (SupportedCountries) other;
		return name.equals(c.name);
	}

	public static void LogChange(String name, Context c) {
		JSONObject data = new JSONObject();
		try { data.put("country", name); } catch (JSONException ignored) { }
		Amplitude.getInstance().logEvent(c.getString(R.string.selected_language), data);
	}
}
