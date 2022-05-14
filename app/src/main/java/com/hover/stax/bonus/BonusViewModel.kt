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
    var isEligible = MutableLiveData<Boolean>()
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

    private fun saveBonuses(bonuses: List<Bonus>) = viewModelScope.launch(Dispatchers.IO) { repo.updateBonuses(bonuses) }

    fun getBonuses() = viewModelScope.launch(Dispatchers.IO) {
        repo.bonuses.collect {
            bonuses.postValue(it)
            checkIfEligible(it)
        }
    }

    //TODO use flow to check selected accounts
    private fun checkIfEligible(bonuses: List<Bonus>) = viewModelScope.launch {
        val ids = bonuses.map { it.userChannel }

        dbRepo.getAccounts().collect { accounts ->
            val accountIds = accounts.map { it.channelId }.toSet()
            isEligible.postValue(ids.intersect(accountIds).isNotEmpty())
        }
        isEligible.postValue(dbRepo.getChannelsByIds(ids).any { it.selected })
    }
}