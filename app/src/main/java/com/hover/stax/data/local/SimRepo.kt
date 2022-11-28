package com.hover.stax.data.local

import com.hover.sdk.database.HoverRoomDatabase
import com.hover.sdk.sims.SimInfo
import kotlinx.coroutines.flow.flowOf

class SimRepo(val db: HoverRoomDatabase) {

	private val dao = db.simDao()

	val flowAll = flowOf(dao.all)

	fun getAll(): List<SimInfo> = dao.all

	fun getPresentSims(): List<SimInfo> = dao.present
}