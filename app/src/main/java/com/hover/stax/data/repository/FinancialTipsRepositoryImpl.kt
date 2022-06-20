package com.hover.stax.data.repository

import android.content.Context
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
import com.hover.stax.R
import com.hover.stax.domain.model.FinancialTip
import com.hover.stax.domain.repository.FinancialTipsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

@OptIn(ExperimentalCoroutinesApi::class)
class FinancialTipsRepositoryImpl(val context: Context, private val coroutineDispatcher: CoroutineDispatcher) : FinancialTipsRepository {

    val db = Firebase.firestore
    val settings = firestoreSettings { isPersistenceEnabled = true }

    override suspend fun fetchTips(): Flow<List<FinancialTip>> = channelFlow {
        withContext(coroutineDispatcher) {
            val timestamp = Timestamp.now()

            db.collection(context.getString(R.string.tips_table))
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

                    launch {
                        send(financialTip.filterNot { it.date == null }.sortedByDescending { it.date!!.time })
                    }
                }
                .addOnFailureListener {
                    Timber.e("Error fetching wellness tips: ${it.localizedMessage}")
                    launch {
                        send(emptyList())
                    }
                }
        }
    }
}