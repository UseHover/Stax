package com.hover.stax.bonus

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
import com.hover.stax.channels.Channel
import com.hover.stax.database.DatabaseRepo
import com.hover.stax.utils.toHni
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

class BonusViewModel(val repo: BonusRepo, private val dbRepo: DatabaseRepo) : ViewModel() {

    private val bonusList = MutableLiveData<List<Bonus>>()
    private val db = Firebase.firestore
    private val settings = firestoreSettings { isPersistenceEnabled = true }

    val bonuses: LiveData<List<Bonus>> get() = bonusList

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
                bonusList.postValue(emptyList())
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

    private fun checkIfEligible(bonusItems: List<Bonus>) = viewModelScope.launch(Dispatchers.IO) {
        val simHnis = dbRepo.presentSims.map { it.osReportedHni }
        val bonusChannels = dbRepo.getChannelsByIds(bonusItems.map { it.purchaseChannel })

        val showBonuses = hasValidSim(simHnis, bonusChannels)
        bonusList.postValue(if (showBonuses) bonusItems else emptyList())
    }

    /**
     * Extract the hnis from the bonus channels and compare with current sim hnis.
     * Return true if user has a valid sim
     */
    private fun hasValidSim(simHnis: List<String>, bonusChannels: List<Channel>) : Boolean {
        val hniList = mutableSetOf<String>()
        bonusChannels.forEach { channel ->
            channel.hniList.split(",").forEach {
                if (simHnis.contains(it.toHni()))
                    hniList.add(it.toHni())
            }
        }

        return hniList.isNotEmpty()
    }
}