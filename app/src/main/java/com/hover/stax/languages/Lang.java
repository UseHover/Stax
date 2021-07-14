package com.hover.stax.languages;

import android.content.Context;

import com.hover.stax.R;
import com.yariksoffice.lingver.Lingver;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class Lang {
    public String code, name;

    public Lang(String code) {
        this.code = code;
        Locale l = new Locale(code);
        name = l.getDisplayLanguage(l);
        name = name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public boolean isSelected() {
        return code.equals(Lingver.getInstance().getLanguage());
    }

    @NotNull
    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Lang)) return false;
        Lang l = (Lang) other;
        return code.equals(l.code);
    }

    public static void LogChange(String code, Context c) {
        JSONObject data = new JSONObject();
        try {
            data.put("language", code);
        } catch (JSONException ignored) {
        }
        Utils.logAnalyticsEvent(c.getString(R.string.selected_language), data, c);
    }
}
