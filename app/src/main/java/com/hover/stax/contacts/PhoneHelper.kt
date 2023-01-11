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
package com.hover.stax.contacts

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber
import com.hover.stax.utils.AnalyticsUtil.logErrorAndReportToFirebase
import timber.log.Timber

object PhoneHelper {

    private const val TAG = "PhoneHelper"

    fun normalizeNumberByCountry(number: String, from_country: String, to_country: String): String {
        var phoneNumber = number
        try {
            phoneNumber = convertToCountry(number, from_country, to_country)
            Timber.e("Normalized number: $phoneNumber")
        } catch (e: NumberParseException) {
            Timber.e(e, "error formatting number")
        }

        return phoneNumber
    }

    @Throws(NumberParseException::class, IllegalStateException::class)
    private fun convertToCountry(num: String, from_country: String, to_country: String): String {
        var number = num
        val phoneUtil = PhoneNumberUtil.getInstance()
        try {
            val phone = phoneUtil.parse(number, to_country)
            //           Most cases we've seen the number format is that used for dialing without the plus
            number = phoneUtil.formatNumberForMobileDialing(phone, from_country, false).replace("+", "")
        } catch (e: IllegalStateException) {
            Timber.e(e, "Google phone number util failed.")
        }
        return number
    }

    @JvmStatic
    fun getNationalSignificantNumber(number: String, country: String?): String {
        return try {
            val phoneUtil = PhoneNumberUtil.getInstance()
            val phone = phoneUtil.parse(number, country)
            phoneUtil.getNationalSignificantNumber(phone)
        } catch (e: Exception) {
            logErrorAndReportToFirebase(TAG, "Failed to transform number for contact; doing it the old fashioned way.", e)
            if (number.startsWith("+")) number.substring(4) else if (number.startsWith("0")) number.substring(1) else number
        }
    }

    @Throws(NumberParseException::class, IllegalStateException::class)
    fun getInternationalNumber(country: String, phoneNumber: String): String {
        val phoneUtil = PhoneNumberUtil.getInstance()
        val phone = getPhone(country, phoneNumber)
        phone.countryCode
        return phoneUtil.format(phone, PhoneNumberUtil.PhoneNumberFormat.E164)
    }

    fun getInternationalNumberNoPlus(accountNumber: String, country: String): String {
        return try {
            getInternationalNumber(country, accountNumber).replace("+", "")
        } catch (e: NumberParseException) {
            logErrorAndReportToFirebase(TAG, "Failed to transform number for contact; doing it the old fashioned way.", e)
            accountNumber.replace("+", "")
        } catch (e: IllegalStateException) {
            logErrorAndReportToFirebase(TAG, "Failed to transform number for contact; doing it the old fashioned way.", e)
            accountNumber.replace("+", "")
        }
    }

    @Throws(NumberParseException::class, IllegalStateException::class)
    private fun getPhone(country: String, phoneNumber: String): PhoneNumber {
        val phoneUtil = PhoneNumberUtil.getInstance()
        return phoneUtil.parse(phoneNumber, country)
    }
}