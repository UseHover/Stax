/*
 * Copyright 2022 Stax
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hover.stax.data.repository

import android.content.Context
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
import com.hover.stax.R
import com.hover.stax.domain.model.FINANCIAL_TIP_ID
import com.hover.stax.domain.model.FinancialTip
import com.hover.stax.domain.repository.FinancialTipsRepository
import com.hover.stax.utils.Utils
import kotlinx.coroutines.tasks.await

class FinancialTipsRepositoryImpl(val context: Context) : FinancialTipsRepository {

    val db = Firebase.firestore
    val settings = firestoreSettings { isPersistenceEnabled = true }

    override suspend fun getTips(): List<FinancialTip> {
        val today = System.currentTimeMillis()

        return db.collection(context.getString(R.string.tips_table))
            .orderBy("date", Query.Direction.DESCENDING)
            .whereLessThanOrEqualTo("date", today / 1000)
            .limit(20)
            .get()
            .await()
            .documents
            .mapNotNull { document ->
                FinancialTip(
                    document.id, document.data!!["title"].toString(), document.data!!["content"].toString(),
                    document.data!!["snippet"].toString(), (document.data!!["date"].toString().toLong() * 1000), document.data!!["share copy"].toString(),
                    document.data!!["deep link"].toString()
                )
            }
    }

    override fun getDismissedTipId(): String? {
        return Utils.getString(FINANCIAL_TIP_ID, context)
    }

    override fun dismissTip(id: String) {
        Utils.saveString(FINANCIAL_TIP_ID, id, context)
    }
}