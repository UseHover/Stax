package com.hover.stax.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "usdc_accounts")
class USDCAccount(
    institutionName: String,
    userAlias: String,
    logoUrl: String,
    accountNo: String,
    institutionId: Int,
    type: String,
    primaryColorHex: String,
    secondaryColorHex: String,

    @ColumnInfo
    var assetType: String,

    @ColumnInfo
    var assetCode: String?,

    @ColumnInfo(defaultValue = "0")
    var isDefault: Boolean = false

) : Account(
    institutionName,
    userAlias,
    logoUrl,
    accountNo,
    institutionId,
    type,
    primaryColorHex,
    secondaryColorHex
) {

    @ColumnInfo
    var encryptedKey: ByteArray? = null

    @ColumnInfo
    var initializationVector: ByteArray? = null

    @ColumnInfo
    var salt: ByteArray? = null
}