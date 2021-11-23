package com.hover.stax.wellness

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
import timber.log.Timber
import java.util.*

class WellnessViewModel : ViewModel() {

    val db = Firebase.firestore
    val settings = firestoreSettings { isPersistenceEnabled = true }

    init {
        db.firestoreSettings = settings
        getTips()
    }

    val tips = MutableLiveData<List<WellnessTip>>()

    private fun getTips() {
        db.collection("wellness_tips").get()
                .addOnSuccessListener { snapshot ->
                    val wellnessTips = snapshot.map { document ->
                        WellnessTip(document.id, document.data["title"].toString(), document.data["content"].toString(), document.getDate("date"))
                    }

                    wellnessTips.forEach {
                        Timber.e("${it.title} - ${it.content} - ${it.date}")
                    }

                    tips.postValue(wellnessTips)
                }
                .addOnFailureListener {
                    Timber.e("Error fetching wellness tips: ${it.localizedMessage}")
                    tips.postValue(emptyList())
                }
    }
}

data class WellnessTip(val id: String, val title: String, val content: String, val date: Date?)
