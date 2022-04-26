package com.hover.stax.inapp_banner

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hover.stax.schedules.ScheduleRepo
import com.hover.stax.transactions.TransactionRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BannerViewModel(application: Application, repo: TransactionRepo) : AndroidViewModel(application) {

    val qualifiedBanner = MutableLiveData<Banner?>()
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