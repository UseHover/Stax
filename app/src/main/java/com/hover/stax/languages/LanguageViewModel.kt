/*
 * Copyright 2022 Stax
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hover.stax.languages

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hover.stax.R
import com.yariksoffice.lingver.Lingver
import javax.inject.Inject

class LanguageViewModel @Inject constructor(
    val application: Application
) : ViewModel() {

    val languages = MutableLiveData<List<Lang>>()

    init {
        languages.value = getLanguages()
    }

    private fun getLanguages(): List<Lang> {
        val languageCodes = application.resources.getStringArray(R.array.supported_lang_codes)

        val langs = mutableListOf(Lang(Lingver.getInstance().getLanguage()))

        for (code in languageCodes) {
            val toAdd = Lang(code)
            if (!langs.contains(toAdd)) langs.add(toAdd)
        }

        return langs
    }
}