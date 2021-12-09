package com.hover.stax.wellness

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
import timber.log.Timber
import java.util.*

data class WellnessTip(val id: String, val title: String, val content: String, val snippet: String?, val date: Date?)
class WellnessViewModel : ViewModel() {

    val db = Firebase.firestore
    val settings = firestoreSettings { isPersistenceEnabled = true }

    init {
        db.firestoreSettings = settings
        getTips()
    }

    val tips = MutableLiveData<List<WellnessTip>>()

    private fun getTips() {
        db.collection("wellness_tips").whereLessThanOrEqualTo("date",Date()).limit(15) .get()
                .addOnSuccessListener { snapshot ->
                    val wellnessTips = snapshot.map { document ->
                        WellnessTip(document.id, document.data["title"].toString(), document.data["content"].toString(), document.data["snippet"].toString(), document.getDate("date"))
                    }
                    tips.postValue(wellnessTips.filterNot { it.date == null }.sortedByDescending{ it.date!!.time })
                }
                .addOnFailureListener {
                    Timber.e("Error fetching wellness tips: ${it.localizedMessage}")
                    tips.postValue(emptyList())
                }
    }
}
