package com.hover.stax.bonus

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
import com.hover.stax.accounts.Account
import com.hover.stax.database.DatabaseRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

class BonusViewModel(val repo: BonusRepo, private val dbRepo: DatabaseRepo) : ViewModel() {

    val db = Firebase.firestore
    val settings = firestoreSettings { isPersistenceEnabled = true }
    val bonus = MutableLiveData<Bonus?>()
    val bonuses = MutableLiveData<List<Bonus>>()
    val accounts = MutableLiveData<List<Account>>()

    init {
        db.firestoreSettings = settings
        fetchBonuses()
    }

    private fun fetchBonuses() {
        db.collection("bonuses")
            .get()
            .addOnSuccessListener { snapshot ->
                val results = snapshot.map { document ->
                    Bonus(
                        document.data["user_channel"].toString().toInt(), document.data["purchase_channel"].toString().toInt(),
                        document.data["bonus_percent"].toString().toDouble(), document.data["message"].toString()
                    )
                }

                saveBonuses(results)
            }
            .addOnFailureListener {
                Timber.e("Error fetching bonuses: ${it.localizedMessage}")
            }
    }

    private fun saveBonuses(bonuses: List<Bonus>) = viewModelScope.launch(Dispatchers.IO) {
        repo.updateBonuses(bonuses.filter { dbRepo.getChannel(it.purchaseChannel) != null })
    }

    fun getBonuses() = viewModelScope.launch(Dispatchers.IO) {
        repo.bonuses.collect {
            checkIfEligible(it)
        }
    }

    private fun checkIfEligible(bonusList: List<Bonus>) = viewModelScope.launch {
        dbRepo.getAccounts().collect { if (it.isEmpty()) bonuses.postValue(bonusList) else setActiveBonus(bonusList, it) }
    }

    private fun setActiveBonus(bonusList: List<Bonus>, accounts: List<Account>) = viewModelScope.launch(Dispatchers.IO) {
        val bonusChannelIds = bonusList.map { it.userChannel }.toSet()
        val accountChannelIds = accounts.map { it.channelId }

        val userChannelIds = accountChannelIds.intersect(bonusChannelIds)
        if (userChannelIds.isNotEmpty()) {
            val result = repo.getBonuses(bonusList.map { it.purchaseChannel }, userChannelIds.toList())

            bonuses.postValue(result)
            bonus.postValue(result.firstOrNull())
        } else
            bonus.postValue(null)
    }
}