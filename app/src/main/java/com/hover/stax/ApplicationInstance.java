package com.hover.stax;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import com.hover.stax.utils.fonts.FontReplacer;
import com.hover.stax.utils.fonts.Replacer;

import java.util.HashMap;

public class ApplicationInstance extends Application {
	@SuppressLint("StaticFieldLeak")
	private static Context context;
	private static HashMap<String, String> currencyMap;

	@Override
	public void onCreate() {
		super.onCreate();
		Replacer replacer = FontReplacer.Build(getApplicationContext());
		replacer.setBoldFont("Barlow-Bold.ttf");
		replacer.setMediumFont("Barlow-SemiBold.ttf");
		replacer.setDefaultFont("Barlow-Regular.ttf");
		replacer.setThinFont("Barlow-Thin.ttf");
		replacer.applyFont();
		context = this;
	}

	public static Context getContext() {
		return context;
	}
}
