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
package com.hover.stax.domain.model

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.hover.stax.utils.DateUtils.now
import timber.log.Timber

const val ACCOUNT_NAME: String = "accountName"
const val ACCOUNT_ID: String = "account_id"

const val USSD_TYPE: String = "ussd"
const val CRYPTO_TYPE: String = "crypto"

@Entity(tableName = "accounts")
open class Account(
	@ColumnInfo(name = "name")
	val institutionName: String,

	@ColumnInfo(name = "alias")
	var userAlias: String,

	@ColumnInfo(name = "logo_url")
	val logoUrl: String,

	@ColumnInfo(name = "account_no")
	var accountNo: String?,

	@ColumnInfo
	var institutionId: Int,

	@NonNull
	@ColumnInfo(name = "account_type", defaultValue = USSD_TYPE)
	var type: String,

	@ColumnInfo(name = "primary_color_hex")
	val primaryColorHex: String,

	@ColumnInfo(name = "secondary_color_hex")
	val secondaryColorHex: String,

) : Comparable<Account> {

	constructor(name: String) : this(name, name)
	constructor(name: String, alias: String) : this(
		institutionName = name, userAlias = alias, logoUrl = "", accountNo = "", institutionId = -1, type = "", primaryColorHex = "#292E35", secondaryColorHex = "#1E232A"
	)

	@PrimaryKey(autoGenerate = true)
	var id: Int = 0

	var institutionAccountName: String? = null

	var latestBalance: String? = null

	@ColumnInfo(defaultValue = "CURRENT_TIMESTAMP")
	var latestBalanceTimestamp: Long = System.currentTimeMillis()


	fun updateBalance(balance: String, timestamp: Long?) {
		latestBalance = balance
		latestBalanceTimestamp = timestamp ?: now()
		Timber.e("Balance is $latestBalance")
	}

	override fun toString() = buildString {
		append(userAlias)

		if (!accountNo.isNullOrEmpty()) {
			append(" - ")
			append(accountNo)
		}
	}

	override fun equals(other: Any?): Boolean {
		if (other !is Account) return false
		return id == other.id
	}

	override fun compareTo(other: Account): Int = toString().compareTo(other.toString())
}