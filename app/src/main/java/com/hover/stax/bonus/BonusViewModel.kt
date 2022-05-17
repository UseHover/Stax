package com.hover.stax.bonus

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
import com.hover.stax.database.DatabaseRepo
import com.hover.stax.utils.toHni
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

class BonusViewModel(val repo: BonusRepo, private val dbRepo: DatabaseRepo) : ViewModel() {

    val db = Firebase.firestore
    val settings = firestoreSettings { isPersistenceEnabled = true }
    val bonuses = MutableLiveData<List<Bonus>>()

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

    private fun checkIfEligible(bonusList: List<Bonus>) = viewModelScope.launch(Dispatchers.IO) {
        val simHnis = dbRepo.presentSims.map { it.osReportedHni }
        val bonusChannels = dbRepo.getChannelsByIds(bonusList.map { it.purchaseChannel })

        val hniList = mutableSetOf<String>()
        bonusChannels.forEach { channel ->
            channel.hniList.split(",").forEach {
                if (simHnis.contains(it.toHni()))
                    hniList.add(it.toHni())
            }
        }

        bonuses.postValue(if (hniList.isNotEmpty()) bonusList else emptyList())
    }
}