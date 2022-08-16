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

    fun getChannelActions(channelId: Int): LiveData<List<HoverAction>> {
        return actionDao.getLiveChannelActions(channelId)
    }

    fun getFirstLiveAction(channelId: Int, type: String): LiveData<HoverAction?> {
        return actionDao.getFirstLiveAction(channelId, type)
    }

    fun getTransferActions(channelId: Int): List<HoverAction> {
        return actionDao.getTransferActions(channelId)
    }

    fun getActions(channelId: Int, type: String?): List<HoverAction> {
        return actionDao.getActions(channelId, type)
    }

    fun getActions(channelIds: IntArray?, type: String?): List<HoverAction> {
        return actionDao.getActions(channelIds, type)
    }

    fun getActions(channelIds: IntArray?, recipientInstitutionId: Int): List<HoverAction> {
        return actionDao.getActions(channelIds, recipientInstitutionId, HoverAction.P2P)
    }

    val bountyActions: LiveData<List<HoverAction>>
        get() = actionDao.bountyActions

    val bounties: List<HoverAction>
        get() = actionDao.bounties
}