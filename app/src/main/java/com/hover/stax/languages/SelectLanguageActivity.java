package com.hover.stax.languages;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.amplitude.api.Amplitude;
import com.hover.stax.R;
import com.hover.stax.databinding.ActivityLanguageBinding;
import com.hover.stax.home.MainActivity;
import com.hover.stax.settings.SettingsFragment;
import com.hover.stax.utils.Utils;
import com.yariksoffice.lingver.Lingver;

import java.util.List;

import static com.hover.stax.utils.Constants.LANGUAGE_CHECK;

public class SelectLanguageActivity extends AppCompatActivity {

    String selectedCode = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityLanguageBinding binding = ActivityLanguageBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());
        Amplitude.getInstance().logEvent(getString(R.string.visit_screen, getString(R.string.visit_language)));

        if (getIntent().hasExtra(SettingsFragment.LANG_CHANGE))
            binding.staxCardView.setBackButtonVisibility(View.VISIBLE);

        selectedCode = Lingver.getInstance().getLanguage();
        final RadioGroup radioGrp = findViewById(R.id.languageRadioGroup);

        LanguageViewModel languageViewModel = new ViewModelProvider(this).get(LanguageViewModel.class);
        languageViewModel.loadLanguages().observe(this, languages -> {
            createRadios(languages, radioGrp);
            radioGrp.setOnCheckedChangeListener((group, checkedId) -> onSelect(checkedId));
        });

        binding.continueLanguageButton.setOnClickListener(v -> onContinue());
    }

    private void createRadios(List<Lang> languages, RadioGroup radioGrp) {
        for (int l = 0; l < languages.size(); l++) {
            RadioButton radioButton = (RadioButton) LayoutInflater.from(this).inflate(R.layout.stax_radio_button, null);
            radioButton.setId(l);
            radioButton.setText(languages.get(l).name);
            radioButton.setTag(languages.get(l).code);
            radioButton.setChecked(languages.get(l).code.equals(selectedCode));

            radioGrp.addView(radioButton);
        }
    }

    private void onSelect(int checkedId) {
        RadioButton radioBtn = findViewById(checkedId);
        selectedCode = radioBtn.getTag().toString();
    }

    private void onContinue() {
        Lingver.getInstance().setLocale(SelectLanguageActivity.this, selectedCode);
        Lang.LogChange(selectedCode, SelectLanguageActivity.this);
        Utils.saveInt(LANGUAGE_CHECK, 1, SelectLanguageActivity.this);
        Intent i = new Intent(this, MainActivity.class);
        if (getIntent().hasExtra(SettingsFragment.LANG_CHANGE)) {
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            i.putExtra(SettingsFragment.LANG_CHANGE, true);
        }
        startActivity(i);
        finish();
    }
}
