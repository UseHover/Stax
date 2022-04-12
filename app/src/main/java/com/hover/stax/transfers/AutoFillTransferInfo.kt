package com.hover.stax.transfers


data class AutoFillTransferInfo (var toInstitutionId : Int= 0, var contactNumber: String? = null, var note: String? = null, var amount: String? = null) {
	fun formattedContactNumber() : String = contactNumber?.replace(" ".toRegex(), "") ?: "";
}