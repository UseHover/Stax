package com.hover.stax;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.hover.stax.utils.fonts.FontReplacer;
import com.hover.stax.utils.fonts.Replacer;
import com.yariksoffice.lingver.Lingver;

import java.util.Locale;

import timber.log.Timber;

public class ApplicationInstance extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        setFont();

        FirebaseApp.initializeApp(this);

        setLogger();
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
}
