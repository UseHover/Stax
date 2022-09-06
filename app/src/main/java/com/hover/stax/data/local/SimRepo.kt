package com.hover.stax.data.local

import androidx.lifecycle.LiveData
import com.hover.sdk.database.HoverRoomDatabase
import com.hover.sdk.sims.SimInfo
import kotlinx.coroutines.flow.flowOf
import java.util.concurrent.Flow

class SimRepo(val db: HoverRoomDatabase) {

	private val dao = db.simDao()

	val all = flowOf(dao.all)

	fun getAll(): List<SimInfo> = dao.all

	fun getPresentSims(): List<SimInfo> = dao.present

	fun getPresentSimsLive() : LiveData<List<SimInfo>> = dao.presentLive

}