package com.hover.stax;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import com.hover.stax.utils.fonts.FontReplacer;
import com.hover.stax.utils.fonts.Replacer;
import com.yariksoffice.lingver.Lingver;

import java.util.HashMap;
import java.util.Locale;

public class ApplicationInstance extends Application {
	@SuppressLint("StaticFieldLeak")
	private static Context context;
	private static HashMap<String, String> currencyMap;

	@Override
	public void onCreate() {
		super.onCreate();
		Replacer replacer = FontReplacer.Build(getApplicationContext());
		replacer.setBoldFont("Effra_Heavy.ttf");
		replacer.setMediumFont("Effra_Medium.ttf");
		replacer.setDefaultFont("Effra_Regular.ttf");
		replacer.setThinFont("Effra_Regular.ttf");
		replacer.applyFont();
		context = this;
		Lingver.init(this, Locale.getDefault());
	}

	public static Context getContext() {
		return context;
	}
}
