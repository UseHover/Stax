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
package com.hover.stax.requests

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hover.stax.data.accounts.AccountRepository
import com.hover.stax.data.contact.ContactRepo
import com.hover.stax.data.requests.RequestRepo
import com.hover.stax.database.models.Account
import com.hover.stax.database.models.Request
import com.hover.stax.database.models.StaxContact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class RequestDetailViewModel @Inject constructor(
    val repo: AccountRepository,
    private val requestRepo: RequestRepo,
    val contactRepo: ContactRepo
) : ViewModel() {

    val request: MutableLiveData<Request> = MutableLiveData()
    var account: LiveData<Account> = MutableLiveData()
    var recipients: LiveData<List<StaxContact>> = MutableLiveData()

    init {
        account = Transformations.switchMap(request) { r -> r?.let { loadAccount(r) } }
        recipients = Transformations.switchMap(request) { r -> r?.let { loadRecipients(r) } }
    }

    private fun loadAccount(r: Request): LiveData<Account> {
        return repo.getLiveAccount(r.requester_account_id)
    }

    fun setRequest(id: Int) = viewModelScope.launch(Dispatchers.IO) {
        request.postValue(requestRepo.getRequest(id))
    }

    private fun loadRecipients(r: Request): LiveData<List<StaxContact>> {
        return contactRepo.getLiveContacts(r.requestee_ids.split(",").toTypedArray())
    }

    fun deleteRequest() = viewModelScope.launch(Dispatchers.IO) {
        requestRepo.delete(request.value)
    }
}