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
package com.hover.stax.domain.use_case.sims

import com.hover.sdk.actions.HoverAction
import com.hover.sdk.sims.SimInfo
import com.hover.stax.data.local.SimRepo
import com.hover.stax.data.local.actions.ActionRepo
import com.hover.stax.domain.model.Account
import com.hover.stax.domain.repository.AccountRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

data class SimWithAccount(
    val sim: SimInfo,
    val account: Account,
    val balanceAction: HoverAction?,
    val airtimeActions: List<HoverAction?> = emptyList(),
    val dataActions: List<HoverAction?> = emptyList()
)

class ListSimsUseCase(
    private val simRepo: SimRepo,
    private val accountRepository: AccountRepository,
    private val actionRepository: ActionRepo,
    private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(): List<SimWithAccount> = withContext(defaultDispatcher) {
        val sims = simRepo.getAll()
        val result: MutableList<SimWithAccount> = mutableListOf()
        for (sim in sims) {
            var account = accountRepository.getAccountBySim(sim.subscriptionId)
            var balanceAct: HoverAction? = null
            var airtimeActs: List<HoverAction> = emptyList()
            var dataActs: List<HoverAction> = emptyList()

            if (account == null)
                account = accountRepository.createAccount(sim)

            if (account.channelId != -1) {
                balanceAct = actionRepository.getFirstAction(account.institutionId, account.countryAlpha2, HoverAction.BALANCE)
                airtimeActs = actionRepository.getActionsByRecipientInstitution(account.institutionId, account.countryAlpha2, HoverAction.AIRTIME)
                dataActs = actionRepository.getActionsByRecipientInstitution(account.institutionId, account.countryAlpha2, HoverAction.BUY_DATA)
            }
            result.add(SimWithAccount(sim, account, balanceAct, airtimeActs, dataActs))
        }
        result
    }
}