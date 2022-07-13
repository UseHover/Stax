package com.hover.stax.financialTips

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
import com.hover.stax.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

data class FinancialTip(val id: String, val title: String, val content: String, val snippet: String, val date: Long?, val shareCopy: String?, val deepLink: String?)

data class FinancialTipsState(val tips: List<FinancialTip> = emptyList())

class FinancialTipsViewModel(val application: Application) : ViewModel() {

    val db = Firebase.firestore
    val settings = firestoreSettings { isPersistenceEnabled = true }

    private val _tips = MutableStateFlow(FinancialTipsState())
    val tipState: StateFlow<FinancialTipsState> = _tips

    init {
        db.firestoreSettings = settings
    }

    fun getTips() = viewModelScope.launch {
        val today = System.currentTimeMillis()

        db.collection(application.getString(R.string.tips_table))
            .orderBy("date", Query.Direction.DESCENDING)
            .whereLessThanOrEqualTo("date", today / 1000)
            .limit(20)
            .get()
            .addOnSuccessListener { snapshot ->
                val financialTip = snapshot.map { document ->
                    FinancialTip(
                        document.id, document.data["title"].toString(), document.data["content"].toString(),
                        document.data["snippet"].toString(), (document.data["date"].toString().toLong() * 1000), document.data["share copy"].toString(),
                        document.data["deep link"].toString()
                    )
                }

                _tips.value = tipState.value.copy(tips = financialTip.filterNot { it.date == null }.sortedByDescending { it.date })
            }
            .addOnFailureListener {
                Timber.e("Error fetching wellness tips: ${it.localizedMessage}")
                _tips.value = tipState.value.copy(tips = emptyList())
            }
    }
}
