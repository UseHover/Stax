package com.hover.stax.hover

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.hover.sdk.actions.HoverAction
import com.hover.stax.actions.ActionRepo
import com.hover.stax.contacts.ContactRepo
import com.hover.stax.contacts.StaxContact

class HoverViewModel(val application: Application, val repo: ContactRepo, val actionRepo: ActionRepo): ViewModel() {

    fun loadContact(contact_id: String): LiveData<StaxContact> {
        return repo.getLiveContact(contact_id)
    }

    fun getAction(channelId: Int, type: String): HoverAction {
        return actionRepo.getActions(channelId, type)[0]
    }
}