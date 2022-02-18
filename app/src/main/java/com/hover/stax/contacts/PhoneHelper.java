package com.hover.stax.contacts;

import android.util.Log;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.hover.sdk.actions.HoverAction;
import com.hover.stax.channels.Channel;
import com.hover.stax.utils.AnalyticsUtil;

public class PhoneHelper {
    final static private String TAG = "PhoneHelper";

    public static String normalizeNumberByCountry(String number, String from_country, String to_country) {
        String phoneNumber = number;
        try {
            phoneNumber = convertToCountry(number, from_country, to_country);
            Log.e("Contact", "Normalized number: " + phoneNumber);
        } catch (NumberParseException e) {
            Log.e("Contact", "error formating number", e);
        }
        return phoneNumber;
    }

    private static String convertToCountry(String number, String from_country, String to_country) throws NumberParseException, IllegalStateException {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber phone = phoneUtil.parse(number, to_country);
//           Most cases we've seen the number format is that used for dialing without the plus
            number = phoneUtil.formatNumberForMobileDialing(phone, from_country, false).replace("+", "");
        } catch (IllegalStateException e) {
            Log.e(TAG, "Google phone number util failed.", e);
        }
        return number;
    }

    public static String getNationalSignificantNumber(String number, String country) {
        try {
            PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
            Phonenumber.PhoneNumber phone = phoneUtil.parse(number, country);
            return phoneUtil.getNationalSignificantNumber(phone);
        } catch (NumberParseException | IllegalStateException e) {
            AnalyticsUtil.logErrorAndReportToFirebase(TAG, "Failed to transform number for contact; doing it the old fashioned way.", e);
            return number.startsWith("+") ? number.substring(4) : (number.startsWith("0") ? number.substring(1) : number);
        }
    }

    public static String getInternationalNumber(String country, String phoneNumber) throws NumberParseException, IllegalStateException {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        Phonenumber.PhoneNumber phone = getPhone(country, phoneNumber);
        phone.getCountryCode();
        return phoneUtil.format(phone, PhoneNumberUtil.PhoneNumberFormat.E164);
    }


    public static String getInternationalNumberNoPlus(String accountNumber, String country) {
        try {
            return getInternationalNumber(country, accountNumber).replace("+", "");
        } catch (NumberParseException | IllegalStateException e) {
            AnalyticsUtil.logErrorAndReportToFirebase(TAG, "Failed to transform number for contact; doing it the old fashioned way.", e);
            return accountNumber.replace("+", "");
        }
    }

    private static Phonenumber.PhoneNumber getPhone(String country, String phoneNumber) throws NumberParseException, IllegalStateException {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        return phoneUtil.parse(phoneNumber, country);
    }
}
