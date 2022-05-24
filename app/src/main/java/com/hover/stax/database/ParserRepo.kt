package com.hover.stax.database

import com.hover.sdk.database.HoverRoomDatabase
import com.hover.sdk.parsers.ParserDao

class ParserRepo(sdkDb: HoverRoomDatabase) {
	private val parserDao: ParserDao = sdkDb.parserDao()

	suspend fun hasSMSParser(actionId: String) : Boolean {
		 return parserDao.getSMSActionParsers(actionId).isNotEmpty()
	}
}