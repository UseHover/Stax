package com.hover.stax.languages;

import android.content.Intent;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.amplitude.api.Amplitude;
import com.hover.stax.ApplicationInstance;
import com.hover.stax.R;
import com.hover.stax.SplashScreenActivity;
import com.hover.stax.home.MainActivity;
import com.hover.stax.utils.Utils;
import com.yariksoffice.lingver.Lingver;

public class SelectLanguageActivity extends AppCompatActivity {
	String selectedCode = null;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_language);
		Amplitude.getInstance().logEvent(getString(R.string.visit_screen, getString(R.string.visit_language)));

		selectedCode = Lingver.getInstance().getLanguage();
		final RadioGroup radioGrp = findViewById(R.id.languageRadioGroup);


		LanguageViewModel languageViewModel = new ViewModelProvider(this).get(LanguageViewModel.class);
		languageViewModel.loadLanguages().observe(this, languages -> {
			for (Lang language: languages) {
				RadioButton radioButton = (RadioButton) LayoutInflater.from(this).inflate(R.layout.stax_radio_button, null);
				radioButton.setText(language.name);
				radioButton.setTag(language.code);
				if (language.code.equals(selectedCode))
					radioButton.setChecked(true);
				else radioButton.setChecked(false);

				radioGrp.addView(radioButton);
			}

			radioGrp.setOnCheckedChangeListener((group, checkedId) -> {
				int checkedRadioButtonId = radioGrp.getCheckedRadioButtonId();
				RadioButton radioBtn = findViewById(checkedRadioButtonId);
				selectedCode = radioBtn.getTag().toString();
				Lingver.getInstance().setLocale(ApplicationInstance.getContext(), selectedCode);
				recreate();
			});
		});

		findViewById(R.id.continueLanguageButton).setOnClickListener(v -> {
			Lang.LogChange(selectedCode, SelectLanguageActivity.this);
			Utils.saveInt(SplashScreenActivity.LANGUAGE_CHECK, 1, ApplicationInstance.getContext());
			startActivity(new Intent(this, MainActivity.class));
			finish();
		});
	}
}
