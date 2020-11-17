package com.hover.stax;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.hover.stax.utils.fonts.FontReplacer;
import com.hover.stax.utils.fonts.Replacer;
import com.yariksoffice.lingver.Lingver;

import java.util.Locale;

public class ApplicationInstance extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		Replacer replacer = FontReplacer.Build(getApplicationContext());
		replacer.setBoldFont("Effra_Heavy.ttf");
		replacer.setMediumFont("Effra_Medium.ttf");
		replacer.setDefaultFont("Effra_Regular.ttf");
		replacer.setThinFont("Effra_Regular.ttf");
		replacer.applyFont();
		Lingver.init(this, Locale.getDefault());

		FirebaseApp.initializeApp(this);
		FirebaseFirestoreSettings firebaseSettings = new FirebaseFirestoreSettings.Builder().setPersistenceEnabled(true).build();
		FirebaseFirestore.getInstance().setFirestoreSettings(firebaseSettings);
	}
}
