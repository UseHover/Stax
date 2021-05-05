package com.hover.stax.languages;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.hover.stax.R;
import com.yariksoffice.lingver.Lingver;

import java.util.ArrayList;
import java.util.List;

public class LanguageViewModel extends AndroidViewModel {
    private MutableLiveData<List<Lang>> languages;

    public LanguageViewModel(Application application) {
        super(application);
        languages = new MutableLiveData<>();
        languages.setValue(getLanguages());
    }

    public LiveData<List<Lang>> loadLanguages() {
        return languages;
    }

    private List<Lang> getLanguages() {
        String[] languageCodes = getApplication().getResources().getStringArray(R.array.supported_lang_codes);
        ArrayList<Lang> langs = new ArrayList<>();
        langs.add(new Lang(Lingver.getInstance().getLanguage()));
        for (String code : languageCodes) {
            Lang toAdd = new Lang(code);
            if (!langs.contains(toAdd))
                langs.add(toAdd);
        }
        return langs;
    }
}
