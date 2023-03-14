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
package com.hover.stax.data.local.actions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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

    fun getFirstAction(institutionId: Int, countryCode: String, type: String): HoverAction? {
        return actionDao.getFirstAction(institutionId, countryCode, type)
    }

    fun getTransferActions(institutionId: Int, countryCode: String): List<HoverAction> {
        return actionDao.getTransferActions(institutionId, countryCode)
    }

    fun getActions(institutionId: Int, countryCode: String): List<HoverAction> {
        // TODO - FIX ME
        return actionDao.getActions(institutionId, countryCode, "")
    }

    fun getActionsByType(institutionId: Int, countryCode: String, type: String?): List<HoverAction> {
        return actionDao.getActions(institutionId, countryCode, type)
    }

    fun getActionsByRecipientInstitution(
        recipientInstitutionId: Int,
        countryCode: String,
        type: String
    ): List<HoverAction> {
        return actionDao.getActionsByRecipientInstitution(recipientInstitutionId, countryCode, type)
    }

    fun getBonusActions(): LiveData<List<HoverAction>> {
        // TODO - FIX ME
        val me = MutableLiveData<List<HoverAction>>()
        return me
//        return actionDao.bonusActions
    }

    fun getBonusActionsByCountry(countries: Array<String>): List<HoverAction> {
        return actionDao.getBonusActions(countries)
    }

    fun getBonusActionsByCountryAndType(country: String, type: String): List<HoverAction> {
        return actionDao.getBonusActionsByType(country, type)
    }

    val bounties: List<HoverAction> get() = actionDao.bounties
}