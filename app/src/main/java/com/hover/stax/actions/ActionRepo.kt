package com.hover.stax.actions

import androidx.lifecycle.LiveData
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.actions.HoverActionDao
import com.hover.sdk.database.HoverRoomDatabase
import com.hover.stax.database.AppDatabase

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

    fun getLiveActions(channelIds: IntArray, types: List<String>): LiveData<List<HoverAction>> {
        return actionDao.getLiveActions(channelIds, types)
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
}