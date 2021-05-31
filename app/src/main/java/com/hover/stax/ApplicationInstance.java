package com.hover.stax;

import android.app.Application;

import com.appsflyer.AppsFlyerConversionListener;
import com.appsflyer.AppsFlyerLib;
import com.google.firebase.FirebaseApp;
import com.hover.stax.utils.fonts.FontReplacer;
import com.hover.stax.utils.fonts.Replacer;
import com.yariksoffice.lingver.Lingver;

import java.util.Locale;
import java.util.Map;

import timber.log.Timber;

public class ApplicationInstance extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        setFont();
        FirebaseApp.initializeApp(this);
        setLogger();
        initAppsFlyer();
    }

    private void setFont() {
        Replacer replacer = FontReplacer.Build(getApplicationContext());
        replacer.setBoldFont("Effra_Heavy.ttf");
        replacer.setMediumFont("Effra_Medium.ttf");
        replacer.setDefaultFont("Effra_Regular.ttf");
        replacer.setThinFont("Effra_Regular.ttf");
        replacer.applyFont();

        Lingver.init(this, Locale.getDefault());
    }

    private void setLogger() {
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.uprootAll();
        }
    }

    private void initAppsFlyer() {
        AppsFlyerConversionListener conversionListener = new AppsFlyerConversionListener() {
            @Override
            public void onConversionDataSuccess(Map<String, Object> conversionData) {
                for (String attrName : conversionData.keySet()) {
                    Timber.d("attribute: " + attrName + " = " + conversionData.get(attrName));
                }
            }
            @Override
            public void onConversionDataFail(String errorMessage) {
                Timber.d("error getting conversion data: %s", errorMessage);
            }
            @Override
            public void onAppOpenAttribution(Map<String, String> attributionData) {
                for (String attrName : attributionData.keySet()) {
                    Timber.d("attribute: " + attrName + " = " + attributionData.get(attrName));
                }
            }
            @Override
            public void onAttributionFailure(String errorMessage) {
                Timber.d("error onAttributionFailure : %s", errorMessage);
            }
        };
        AppsFlyerLib.getInstance().init(getString(R.string.appsflyer_key), conversionListener, this);
    }
}
