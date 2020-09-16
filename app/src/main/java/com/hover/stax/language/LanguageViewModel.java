package com.hover.stax.language;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hover.stax.ApplicationInstance;
import com.hover.stax.R;
import com.yariksoffice.lingver.Lingver;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LanguageViewModel extends ViewModel {
	private MutableLiveData<Map<String, String>> languages;
	public LanguageViewModel() {
		languages = new MutableLiveData<>();
		languages.setValue(getLanguages());
	}

	public LiveData<Map<String, String>> loadLanguages() {return languages;}

	private Map<String, String> getLanguages() {
		String[] languageCodes = ApplicationInstance.getContext().getResources().getStringArray(R.array.supported_languages_code);
		Map<String, String> languageMap = new HashMap<>();
		String defaultLangCode = Lingver.getInstance().getLanguage();
		languageMap.put(new Locale(defaultLangCode).getDisplayLanguage(), defaultLangCode);
		for (String languageCode : languageCodes) {
			if (languageMap.get(new Locale(languageCode).getDisplayLanguage()) == null)
				languageMap.put(new Locale(languageCode).getDisplayLanguage(), languageCode);
		}
		return languageMap;
	}
}
