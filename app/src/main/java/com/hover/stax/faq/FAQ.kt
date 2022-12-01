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