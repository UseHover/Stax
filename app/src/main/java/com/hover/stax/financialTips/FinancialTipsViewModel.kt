package com.hover.stax.financialTips

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
import com.hover.stax.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

data class FinancialTip(val id: String, val title: String, val content: String, val snippet: String, val date: Date?, val shareCopy: String?, val deepLink: String?)

class FinancialTipsViewModel(val application: Application) : ViewModel() {

    val db = Firebase.firestore
    val settings = firestoreSettings { isPersistenceEnabled = true }

    private val _tips = MutableStateFlow<List<FinancialTip>>(emptyList())
    val tips: StateFlow<List<FinancialTip>> = _tips

    init {
        db.firestoreSettings = settings
    }

    fun getTips() = viewModelScope.launch {
        val timestamp = Timestamp.now()

        db.collection(application.getString(R.string.tips_table))
            .orderBy("date", Query.Direction.DESCENDING)
            .whereLessThanOrEqualTo("date", timestamp.toDate())
            .limit(20)
            .get()
            .addOnSuccessListener { snapshot ->
                val financialTip = snapshot.map { document ->
                    FinancialTip(
                        document.id, document.data["title"].toString(), document.data["content"].toString(),
                        document.data["snippet"].toString(), document.getDate("date"), document.data["share copy"].toString(),
                        document.data["deep link"].toString()
                    )
                }

                _tips.value = financialTip.filterNot { it.date == null }.sortedByDescending { it.date!!.time }
            }
            .addOnFailureListener {
                Timber.e("Error fetching wellness tips: ${it.localizedMessage}")
            }
    }
}
