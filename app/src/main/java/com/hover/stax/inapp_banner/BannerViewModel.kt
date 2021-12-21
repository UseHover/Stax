package com.hover.stax.inapp_banner

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hover.stax.database.DatabaseRepo
import kotlinx.coroutines.launch

class BannerViewModel(application: Application, repo: DatabaseRepo) : ViewModel() {

    private val qualifiedBannerLiveData = MutableLiveData<Banner>()
    private val bannerUtils = BannerUtils(application)

    init {
        viewModelScope.launch {
            val hasTransactionLastMonth: Boolean = repo.hasTransactionLastMonth()
            qualifiedBannerLiveData.postValue(bannerUtils.getQualifiedBanner(hasTransactionLastMonth))
        }
    }

    fun qualifiedBanner(): LiveData<Banner> = qualifiedBannerLiveData

    fun closeCampaign(bannerId: Int) {
        bannerUtils.closeCampaign(bannerId)
        qualifiedBannerLiveData.postValue(null)
    }
}