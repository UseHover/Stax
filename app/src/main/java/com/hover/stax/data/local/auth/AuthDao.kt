package com.hover.stax.data.local.auth

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.hover.stax.domain.model.TokenInfo

@Dao
interface AuthDao {

    @Query("SELECT * FROM token_info LIMIT 1")
    suspend fun getTokenInfo(): TokenInfo?

    @Transaction
    suspend fun saveTokenInfo(tokenInfo: TokenInfo) {
        delete()
        insert(tokenInfo)
    }

    @Insert
    suspend fun insert(tokenInfo: TokenInfo)

    @Query("DELETE FROM token_info")
    suspend fun delete()

}