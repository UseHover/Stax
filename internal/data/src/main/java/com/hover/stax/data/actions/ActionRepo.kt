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
package com.hover.stax.data.actions

import androidx.lifecycle.LiveData
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.actions.HoverActionDao
import javax.inject.Inject

interface ActionRepository {

    fun getAction(public_id: String): HoverAction?

    fun getLiveAction(public_id: String?): LiveData<HoverAction>

    fun getFirstAction(institutionId: Int, countryCode: String, type: String): HoverAction?

    fun getTransferActions(institutionId: Int, countryCode: String): List<HoverAction>

    fun getActions(institutionId: Int, countryCode: String, type: String?): List<HoverAction>

    fun getActionsByRecipientInstitution(
        recipientInstitutionId: Int,
        countryCode: String,
        type: String
    ): List<HoverAction>

    fun getBonusActionsByCountry(countries: Array<String>): List<HoverAction>

    fun getBonusActionsByCountryAndType(country: String, type: String): List<HoverAction>

    val bounties: List<HoverAction>
}

class ActionRepo @Inject constructor(
    private val actionDao: HoverActionDao
) : ActionRepository {

    override fun getAction(public_id: String): HoverAction? {
        return actionDao.getAction(public_id)
    }

    override fun getLiveAction(public_id: String?): LiveData<HoverAction> {
        return actionDao.getLiveAction(public_id)
    }

    override fun getFirstAction(
        institutionId: Int,
        countryCode: String,
        type: String
    ): HoverAction? {
        return actionDao.getFirstAction(institutionId, countryCode, type)
    }

    override fun getTransferActions(institutionId: Int, countryCode: String): List<HoverAction> {
        return actionDao.getTransferActions(institutionId, countryCode)
    }

    override fun getActions(
        institutionId: Int,
        countryCode: String,
        type: String?
    ): List<HoverAction> {
        return actionDao.getActions(institutionId, countryCode, type)
    }

    override fun getActionsByRecipientInstitution(
        recipientInstitutionId: Int,
        countryCode: String,
        type: String
    ): List<HoverAction> {
        return actionDao.getActionsByRecipientInstitution(recipientInstitutionId, countryCode, type)
    }

    override fun getBonusActionsByCountry(countries: Array<String>): List<HoverAction> {
        return actionDao.getBonusActions(countries)
    }

    override fun getBonusActionsByCountryAndType(country: String, type: String): List<HoverAction> {
        return actionDao.getBonusActionsByType(country, type)
    }

    override val bounties: List<HoverAction> get() = actionDao.bounties
}