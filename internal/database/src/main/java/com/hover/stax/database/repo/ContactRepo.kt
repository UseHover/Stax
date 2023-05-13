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
package com.hover.stax.database.repo

import androidx.lifecycle.LiveData
import com.hover.stax.core.AnalyticsUtil
import com.hover.stax.database.dao.ContactDao
import com.hover.stax.database.models.StaxContact
import javax.inject.Inject

interface ContactRepository {

    val allContacts: LiveData<List<StaxContact>>

    fun getContacts(ids: Array<String>): List<StaxContact>

    fun getLiveContacts(ids: Array<String>): LiveData<List<StaxContact>>

    fun getContact(id: String?): StaxContact?

    suspend fun getContactAsync(id: String?): StaxContact?

    fun getContactByPhone(phone: String?): StaxContact?

    fun getLiveContact(id: String?): LiveData<StaxContact>

    fun save(contact: StaxContact)
}

class ContactRepo @Inject constructor(
    private val contactDao: ContactDao
) : ContactRepository {

    override val allContacts: LiveData<List<StaxContact>>
        get() = contactDao.all

    override fun getContacts(ids: Array<String>): List<StaxContact> {
        return contactDao[ids]
    }

    override fun getLiveContacts(ids: Array<String>): LiveData<List<StaxContact>> {
        return contactDao.getLive(ids)
    }

    override fun getContact(id: String?): StaxContact? {
        return contactDao[id]
    }

    override suspend fun getContactAsync(id: String?): StaxContact? {
        return contactDao.getAsync(id)
    }

    override fun getContactByPhone(phone: String?): StaxContact? {
        return contactDao.getByPhone("%$phone%")
    }

    override fun getLiveContact(id: String?): LiveData<StaxContact> {
        return contactDao.getLive(id)
    }

    override fun save(contact: StaxContact) {
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