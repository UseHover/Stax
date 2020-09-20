package com.hover.stax.languages;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hover.stax.ApplicationInstance;
import com.hover.stax.R;
import com.yariksoffice.lingver.Lingver;

import java.util.ArrayList;
import java.util.List;

public class LanguageViewModel extends ViewModel {
	private MutableLiveData<List<Lang>> languages;

	public LanguageViewModel() {
		languages = new MutableLiveData<>();
		languages.setValue(getLanguages());
	}

	public LiveData<List<Lang>> loadLanguages() { return languages; }

	private List<Lang> getLanguages() {
		String[] languageCodes = ApplicationInstance.getContext().getResources().getStringArray(R.array.supported_languages_code);
		ArrayList<Lang> langs = new ArrayList<>();
		langs.add(new Lang(Lingver.getInstance().getLanguage()));
		for (String code: languageCodes) {
			Lang toAdd = new Lang(code);
			if (!langs.contains(toAdd))
				langs.add(toAdd);
		}
		return langs;
	}
}
