package com.hover.stax.inapp_banner

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class BannerViewModel(application: Application) : AndroidViewModel(application) {
    private val qualifiedBannerLiveData: MutableLiveData<Banner> = MutableLiveData<Banner>()
    private val bannerUtils = BannerUtils(getApplication())

    init {
        viewModelScope.launch {
            qualifiedBannerLiveData.postValue(bannerUtils.getQualifiedBanner())
        }
    }

    fun qualifiedBanner(): LiveData<Banner> = qualifiedBannerLiveData
    fun closeCampaign(bannerId: Int) {
        bannerUtils.closeCampaign(bannerId)
        qualifiedBannerLiveData.postValue(null)
    }
}