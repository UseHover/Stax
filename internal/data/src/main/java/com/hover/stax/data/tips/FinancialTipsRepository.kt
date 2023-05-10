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
package com.hover.stax.data.tips

import android.content.Context
import com.hover.stax.model.FinancialTip
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface FinancialTipsRepository {

    suspend fun getTips(): List<FinancialTip>

    fun getDismissedTipId(): String?

    fun dismissTip(id: String)
}

class FinancialTipsRepositoryImpl @Inject constructor(
    @ApplicationContext appContext: Context
) : FinancialTipsRepository {

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
                    document.id,
                    document.data!!["title"].toString(),
                    document.data!!["content"].toString(),
                    document.data!!["snippet"].toString(),
                    (document.data!!["date"].toString().toLong() * 1000),
                    document.data!!["share copy"].toString(),
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