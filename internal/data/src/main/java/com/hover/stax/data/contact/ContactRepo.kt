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
package com.hover.stax.data.contact

import androidx.lifecycle.LiveData
import com.hover.stax.database.AppDatabase
import com.hover.stax.database.StaxDatabase
import com.hover.stax.database.dao.ContactDao
import com.hover.stax.database.models.StaxContact
import com.hover.stax.utils.AnalyticsUtil

class ContactRepo(db: StaxDatabase) {

    private val contactDao: ContactDao = db.contactDao()

    val allContacts: LiveData<List<StaxContact>>
        get() = contactDao.all

    fun getContacts(ids: Array<String>): List<StaxContact> {
        return contactDao[ids]
    }

    fun getLiveContacts(ids: Array<String>): LiveData<List<StaxContact>> {
        return contactDao.getLive(ids)
    }

    fun getContact(id: String?): StaxContact? {
        return contactDao[id]
    }

    suspend fun getContactAsync(id: String?): StaxContact? {
        return contactDao.getAsync(id)
    }

    fun getContactByPhone(phone: String?): StaxContact? {
        return contactDao.getByPhone("%$phone%")
    }

    fun getLiveContact(id: String?): LiveData<StaxContact> {
        return contactDao.getLive(id)
    }

    fun save(contact: StaxContact) {
        AppDatabase.databaseWriteExecutor.execute {
            if (getContact(contact.id) == null && contact.accountNumber != null) {
                try {
                    contactDao.insert(contact)
                } catch (e: Exception) {
                    AnalyticsUtil.logErrorAndReportToFirebase(
                        "ContactRepo",
                        "failed to insert contact",
                        e
                    )
                }
            } else contactDao.updateStaxContact(contact)
        }
    }
}