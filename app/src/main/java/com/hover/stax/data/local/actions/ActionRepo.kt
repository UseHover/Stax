package com.hover.stax.data.local.actions

import androidx.lifecycle.LiveData
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.actions.HoverActionDao
import com.hover.sdk.database.HoverRoomDatabase

class ActionRepo(sdkDb: HoverRoomDatabase) {

    private val actionDao: HoverActionDao = sdkDb.actionDao()

    fun getAction(public_id: String): HoverAction? {
        return actionDao.getAction(public_id)
    }

    fun getLiveAction(public_id: String?): LiveData<HoverAction> {
        return actionDao.getLiveAction(public_id)
    }

    fun getFirstAction(channelId: Int, type: String): HoverAction? {
        return actionDao.getFirstAction(channelId, type)
    }

    fun getTransferActions(institutionId: Int, countryCode: String): List<HoverAction> {
        return actionDao.getTransferActions(institutionId, countryCode)
    }

    fun getActions(institutionId: Int, countryCode: String, type: String?): List<HoverAction> {
        return actionDao.getActions(institutionId, countryCode, type)
    }

    fun getActionsByRecipientInstitution(recipientInstitutionId: Int, countryCode: String, type: String): List<HoverAction> {
        return actionDao.getActionsByRecipientInstitution(recipientInstitutionId, countryCode, type)
    }

    fun getBonusActionsByCountry(countries: Array<String>): List<HoverAction> {
        return actionDao.getBonusActions(countries)
    }

    fun getBonusActionsByCountryAndType(country: String, type: String): List<HoverAction> {
        return actionDao.getBonusActionsByType(country, type)
    }

    val bounties: List<HoverAction> get() = actionDao.bounties
}