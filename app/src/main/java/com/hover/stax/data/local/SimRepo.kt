package com.hover.stax.data.local

import androidx.lifecycle.LiveData
import com.hover.sdk.database.HoverRoomDatabase
import com.hover.sdk.sims.SimInfo

class SimRepo(val db: HoverRoomDatabase) {

	private val dao = db.simDao()

	fun getPresentSims(): List<SimInfo> = dao.present

	fun getPresentSimsLive() : LiveData<List<SimInfo>> = dao.presentLive

}