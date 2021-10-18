package com.hover.stax.languages

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hover.stax.R
import com.yariksoffice.lingver.Lingver

class LanguageViewModel(val application: Application): ViewModel() {

    val languages = MutableLiveData<List<Lang>>()

    init {
        languages.value = getLanguages()
    }

    private fun getLanguages(): List<Lang> {
        val languageCodes = application.resources.getStringArray(R.array.supported_lang_codes)

        val langs = mutableListOf(Lang(Lingver.getInstance().getLanguage()))

        for(code in languageCodes){
            val toAdd = Lang(code)
            if(!langs.contains(toAdd)) langs.add(toAdd)
        }

        return langs
    }
}