package com.hover.stax.faq

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
import timber.log.Timber

data class FAQ(var id: String, var topic: String, var content: String)

class FaqViewModel : ViewModel() {
    val faqLiveData: LiveData<List<FAQ>> = getFAQList()

    private fun getFAQList(): LiveData<List<FAQ>> {
        val db = Firebase.firestore
        val settings = firestoreSettings { isPersistenceEnabled = true }
        db.firestoreSettings = settings

        val liveData: MutableLiveData<List<FAQ>> = MutableLiveData()

        db.collection("faqs")
            .get().addOnSuccessListener { result ->
                val faqList: MutableList<FAQ> = mutableListOf()
                for (document in result) {
                    faqList.add(FAQ(document.id, document.data["topic"].toString(), document.data["content"].toString()))
                }

                liveData.value = faqList
                return@addOnSuccessListener
            }.addOnFailureListener { exception ->
                Timber.e(exception)
                return@addOnFailureListener
            }

        return liveData
    }
}