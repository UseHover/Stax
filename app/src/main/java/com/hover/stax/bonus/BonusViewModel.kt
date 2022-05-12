package com.hover.stax.bonus

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

class BonusViewModel(val repo: BonusRepo) : ViewModel() {

    val db = Firebase.firestore
    val settings = firestoreSettings { isPersistenceEnabled = true }

    init {
        db.firestoreSettings = settings
        fetchBonuses()
    }

    private val bonuses = MutableLiveData<List<Bonus>>()

    private fun fetchBonuses() {
        db.collection("bonus")
            .get()
            .addOnSuccessListener { snapshot ->
                val results = snapshot.map { document ->
                    Bonus(
                        document.data["recipient_channel"].toString().toInt(), document.data["purchase_channel"].toString().toInt(),
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
        repo.save(bonuses)
    }

    fun getBonuses() = viewModelScope.launch {
        repo.bonuses.collect { bonuses.postValue(it) }
    }
}