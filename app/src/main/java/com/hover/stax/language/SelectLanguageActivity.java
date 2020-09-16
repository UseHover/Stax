package com.hover.stax.language;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.hover.sdk.utils.Utils;
import com.hover.stax.ApplicationInstance;
import com.hover.stax.R;
import com.hover.stax.SplashScreenActivity;
import com.hover.stax.home.HomeViewModel;
import com.hover.stax.home.MainActivity;
import com.hover.stax.home.detailsPages.transaction.TransactionDetailsViewModel;
import com.hover.stax.utils.fonts.FontReplacer;
import com.hover.stax.utils.fonts.Replacer;
import com.hover.stax.utils.fonts.ReplacerImpl;
import com.yariksoffice.lingver.Lingver;

import java.util.Locale;
import java.util.Map;

public class SelectLanguageActivity extends AppCompatActivity {
	String languageCode = null;
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.language_select_layout);

		languageCode = Lingver.getInstance().getLanguage();
		final RadioGroup radioGrp = findViewById(R.id.languageRadioGroup);

		ColorStateList colorStateList = new ColorStateList(
				new int[][]{new int[]{android.R.attr.state_enabled}},
				new int[] {getResources().getColor(R.color.offWhite)}
		);

		LanguageViewModel languageViewModel = new ViewModelProvider(this).get(LanguageViewModel.class);
		languageViewModel.loadLanguages().observe(this, stringStringMap -> {
			for (String langCodes: stringStringMap.values() ) {
				RadioButton radioButton = new RadioButton(this);
				radioButton.setText(new Locale(langCodes).getDisplayLanguage());
				radioButton.setTag(langCodes);
				radioButton.setTextColor(Color.WHITE);
				radioButton.setHighlightColor(Color.WHITE);
				radioButton.setTextSize(16);
				Typeface font = FontReplacer.getDefaultFont();
				radioButton.setTypeface(font);

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) radioButton.setButtonTintList(colorStateList);
				if(langCodes.toLowerCase().equals(languageCode.toLowerCase()))
					radioButton.setChecked(true);
				else radioButton.setChecked(false);
				radioGrp.addView(radioButton);
			}


			//set listener to radio button group
			radioGrp.setOnCheckedChangeListener((group, checkedId) -> {
				int checkedRadioButtonId = radioGrp.getCheckedRadioButtonId();
				RadioButton radioBtn = findViewById(checkedRadioButtonId);
				languageCode = radioBtn.getTag().toString();
				Lingver.getInstance().setLocale(ApplicationInstance.getContext(), languageCode);
				recreate();
			});
		});
		//create radio buttons




		findViewById(R.id.continueLanguageButton).setOnClickListener(v -> {
			Utils.saveInt(SplashScreenActivity.LANGUAGE_CHECK, 1, ApplicationInstance.getContext());
			startActivity(new Intent(this, MainActivity.class));
			finish();
		});
	}
}
