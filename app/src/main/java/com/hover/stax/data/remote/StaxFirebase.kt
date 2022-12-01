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
package com.hover.stax.data.remote

import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

internal class StaxFirebase {
    private val settings = firestoreSettings { isPersistenceEnabled = true }
    private val db = Firebase.firestore.also { it.firestoreSettings = settings }

    fun clearPersistence() {
        db.clearPersistence()
    }

    suspend fun fetchBonuses(): QuerySnapshot {
        return db.collection("bonuses").get().await()
    }
}