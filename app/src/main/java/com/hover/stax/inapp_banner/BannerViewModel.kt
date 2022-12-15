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
package com.hover.stax.inapp_banner

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hover.stax.transactions.TransactionRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BannerViewModel(application: Application, repo: TransactionRepo) : AndroidViewModel(application) {

    private val qualifiedBanner = MutableLiveData<Banner?>()
    private val bannerUtils = BannerUtils(application)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val hasTransactionLastMonth: Boolean = repo.hasTransactionLastMonth()
            qualifiedBanner.postValue(bannerUtils.getQualifiedBanner(hasTransactionLastMonth))
        }
    }

    fun closeCampaign(bannerId: Int) {
        bannerUtils.closeCampaign(bannerId)
        qualifiedBanner.postValue(null)
    }
}