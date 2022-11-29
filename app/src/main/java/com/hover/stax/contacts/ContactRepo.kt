package com.hover.stax.contacts

import androidx.lifecycle.LiveData
import com.hover.sdk.database.HoverRoomDatabase
import com.hover.stax.database.AppDatabase
import com.hover.stax.utils.AnalyticsUtil

class ContactRepo(db: AppDatabase) {

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

    fun getContactByPhone(phone: String): StaxContact? {
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
                    AnalyticsUtil.logErrorAndReportToFirebase("ContactRepo", "failed to insert contact", e)
                }
            } else contactDao.updateStaxContact(contact)
        }
    }
}