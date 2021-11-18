package com.hover.stax.wellness

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
import timber.log.Timber

class WellnessViewModel : ViewModel() {

    val db = Firebase.firestore
    val settings = firestoreSettings { isPersistenceEnabled = true }

    init {
        db.firestoreSettings = settings
    }

    val tips = MutableLiveData<List<WellnessTip>>()

    fun getTips() {
        db.collection("wellness_tips").get()
                .addOnSuccessListener { snapshot ->
                    val wellnessTips = snapshot.map { document ->
                        WellnessTip(document.id, document.data["title"].toString(), document.data["content"].toString(), document.data["date"].toString())
                    }

                    tips.postValue(wellnessTips)
                }
                .addOnFailureListener {
                    Timber.e("Error fetching wellness tips: ${it.localizedMessage}")
                    tips.postValue(emptyList())
                }
    }
}

data class WellnessTip(val id: String, val title: String, val content: String, val timestamp: String)
