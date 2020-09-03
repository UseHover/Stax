package com.hover.stax;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import com.blongho.country_data.Currency;
import com.blongho.country_data.World;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.utils.fonts.FontReplacer;
import com.hover.stax.utils.fonts.Replacer;

import java.util.HashMap;
import java.util.List;

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
		setupCurrency();

	}
public void setupCurrency() {
	World.init(getApplicationContext());
	currencyMap = new HashMap<>();
	for(Currency currency: World.getAllCurrencies()) {
		currencyMap.put(currency.getCountry(), currency.getCode());
	}
}

public static String getCurrency(String alphaCountry) {
		return currencyMap.get(alphaCountry);
}

	public static Context getContext() {
	return context;
}
}
