package com.hover.stax.languages;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
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
import com.hover.stax.utils.fonts.FontReplacer;
import com.yariksoffice.lingver.Lingver;

public class SelectLanguageActivity extends AppCompatActivity {
	String selectedCode = null;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.language_select_layout);
		Amplitude.getInstance().logEvent(getString(R.string.visit_screen, getString(R.string.visit_language)));

		selectedCode = Lingver.getInstance().getLanguage();
		final RadioGroup radioGrp = findViewById(R.id.languageRadioGroup);

		ColorStateList colorStateList = new ColorStateList(
				new int[][]{new int[]{android.R.attr.state_enabled}},
				new int[] {getResources().getColor(R.color.colorAccent)}
		);
		LanguageViewModel languageViewModel = new ViewModelProvider(this).get(LanguageViewModel.class);
		languageViewModel.loadLanguages().observe(this, languages -> {
			for (Lang language: languages) {
				RadioButton radioButton = new RadioButton(this);
				radioButton.setText(language.name);
				radioButton.setTextColor(Color.WHITE);
				radioButton.setHighlightColor(Color.WHITE);
				radioButton.setTextSize(16);
				radioButton.setHeight(75);
				radioButton.setPadding(16, 0, 0, 0);
				radioButton.setTag(language.code);
				Typeface font = FontReplacer.getDefaultFont();
				radioButton.setTypeface(font);

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) radioButton.setButtonTintList(colorStateList);
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
