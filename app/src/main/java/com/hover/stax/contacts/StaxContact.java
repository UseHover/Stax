package com.hover.stax.contacts;

import static com.google.i18n.phonenumbers.PhoneNumberUtil.MatchType.NO_MATCH;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.hover.sdk.actions.HoverAction;
import com.hover.sdk.transactions.TransactionContract;
import com.hover.stax.R;
import com.hover.stax.database.Converters;
import com.hover.stax.utils.DateUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import timber.log.Timber;

@Entity(tableName = "stax_contacts", indices = {@Index(value = "id", unique = true), @Index(value = "phone_number", unique = true)})
@TypeConverters({Converters.class})
public class StaxContact {
    public final static String ID_KEY = "contact_id",
            RECIPIENT_PHONE_KEY = "recipientPhone", RECIPIENT_ACCOUNT_KEY = "recipientAccount", RECIPIENT_NAME_KEY = "recipientName",
            SENDER_NAME_KEY = "senderName", SENDER_PHONE_KEY = "senderPhone", SENDER_ACCOUNT_KEY = "senderAccount";

    @PrimaryKey
    @NonNull
    public String id;

    @ColumnInfo(name = "lookup_key")
    public String lookupKey;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "aliases")
    public ArrayList<String> aliases;

    @ColumnInfo(name = "phone_number")
    public String accountNumber;

    @ColumnInfo(name = "thumb_uri")
    public String thumbUri;

    @ColumnInfo(name = "last_used_timestamp")
    public Long lastUsedTimestamp;

    public StaxContact() {
    }

    public StaxContact(String phone) {
        id = UUID.randomUUID().toString();
        lastUsedTimestamp = DateUtils.now();
        accountNumber = phone.replaceAll(" ", "");
        name = "";
    }

    public StaxContact(Intent i) {
        id = UUID.randomUUID().toString();
        lastUsedTimestamp = DateUtils.now();
        accountNumber = getAccountFromExtras(i);
        name = getNameFromExtras(i);
    }

    @SuppressLint("Range")
    public StaxContact(Uri contactData, Context c) {
        if (contactData != null) {
            Cursor cur = c.getContentResolver().query(contactData, null, null, null, null);
            if (cur != null && cur.getCount() > 0 && cur.moveToNext()) {
                id = cur.getString(cur.getColumnIndexOrThrow(ContactsContract.RawContacts._ID));
                Timber.e("pulled contact with id: %s", id);
                lookupKey = cur.getString(cur.getColumnIndexOrThrow(ContactsContract.Contacts.LOOKUP_KEY));
                name = cur.getString(cur.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
                thumbUri = cur.getString(cur.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI));

                if (Integer.parseInt(cur.getString(cur.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor phones = c.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id, null, null);
                    if (phones != null && phones.moveToNext())
                        accountNumber = phones.getString(phones.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)).replaceAll(" ", "");
                    if (phones != null) phones.close();
                }
            }
            if (cur != null) cur.close();
        }
    }

    public static String shortName(List<StaxContact> contacts, Context c) {
        if (contacts == null || contacts.size() == 0) return null;
        else if (contacts.size() == 1) return contacts.get(0).shortName();
        else return c.getString(R.string.descrip_multcontacts, contacts.size());
    }

    public static StaxContact findOrInit(Intent intent, String countryAlpha2, ContactRepo dr) {
        StaxContact sc = checkInKeys(intent, countryAlpha2, dr);
        if (sc == null) sc = checkOutKeys(intent, countryAlpha2, dr);
        if (sc == null) sc = new StaxContact(intent);

        return sc;
    }

    @SuppressWarnings("unchecked")
    private static StaxContact checkInKeys(Intent intent, String countryAlpha2, ContactRepo dr) {
        HashMap<String, String> inExtras = (HashMap<String, String>) intent.getSerializableExtra(TransactionContract.COLUMN_INPUT_EXTRAS);
        if (inExtras != null && inExtras.containsKey(StaxContact.ID_KEY))
            return dr.getContact(inExtras.get(StaxContact.ID_KEY));
        else if (inExtras != null && inExtras.containsKey(HoverAction.PHONE_KEY))
            return getContactByPhoneValue(inExtras, HoverAction.PHONE_KEY, countryAlpha2, dr);
        else if (inExtras != null && inExtras.containsKey(HoverAction.ACCOUNT_KEY))
            return dr.getContactByPhone(inExtras.get(HoverAction.ACCOUNT_KEY));
        else return null;
    }

    @SuppressWarnings("unchecked")
    private static StaxContact checkOutKeys(Intent intent, String countryAlpha2, ContactRepo dr) {
        HashMap<String, String> outExtras = (HashMap<String, String>) intent.getSerializableExtra(TransactionContract.COLUMN_PARSED_VARIABLES);
         if (outExtras != null && outExtras.containsKey(RECIPIENT_PHONE_KEY))
            return getContactByPhoneValue(outExtras, RECIPIENT_PHONE_KEY, countryAlpha2, dr);
        else if (outExtras != null && outExtras.containsKey(RECIPIENT_ACCOUNT_KEY))
            return dr.getContactByPhone(outExtras.get(RECIPIENT_ACCOUNT_KEY));
        else if (outExtras != null && outExtras.containsKey(SENDER_PHONE_KEY))
             return getContactByPhoneValue(outExtras, SENDER_PHONE_KEY, countryAlpha2, dr);
        else if (outExtras != null && outExtras.containsKey(SENDER_ACCOUNT_KEY))
             return dr.getContactByPhone(outExtras.get(SENDER_ACCOUNT_KEY));
        return null;
    }

    private static StaxContact getContactByPhoneValue(HashMap<String, String> map, String key, String countryAlpha2, ContactRepo dr) {
        StaxContact c = dr.getContactByPhone(PhoneHelper.getNationalSignificantNumber(map.get(key), countryAlpha2));
        if (c == null) c = dr.getContactByPhone(map.get(key));
        return c;
    }

    public String shortName() {
        return hasName() ? name : accountNumber;
    }

    public boolean hasName() {
        return name != null && !name.isEmpty();
    }

    @NonNull
    @Override
    public String toString() {
        return (name + " " + accountNumber).trim();
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof StaxContact)) return false;
        StaxContact otherContact = (StaxContact) other;
        return id.equals(otherContact.id) || accountNumber.equals(otherContact.accountNumber) || isSamePhone(otherContact);
    }

    private boolean isSamePhone(StaxContact other) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        return phoneUtil.isNumberMatch(accountNumber, other.accountNumber) != NO_MATCH;
    }

    public void updateNames(Intent i) {
        if (name == null || name.isEmpty())
            name = getNameFromExtras(i);
        else if (!getNameFromExtras(i).isEmpty() && !name.equals(getNameFromExtras(i))) {
            if (aliases == null || aliases.size() == 0)
                aliases = new ArrayList<>(Collections.singleton(getNameFromExtras(i)));
            else if (!aliases.contains(getNameFromExtras(i)))
                aliases.add(getNameFromExtras(i));
        }
    }

    private String getNameFromExtras(Intent i) {
        HashMap<String, String> outExtras = (HashMap<String, String>) i.getSerializableExtra(TransactionContract.COLUMN_PARSED_VARIABLES);
        if (outExtras != null && outExtras.containsKey(RECIPIENT_NAME_KEY))
            return outExtras.get(RECIPIENT_NAME_KEY);
        else if (outExtras != null && outExtras.containsKey(SENDER_NAME_KEY))
            return outExtras.get(SENDER_NAME_KEY);
        else return "";
    }

    private String getAccountFromExtras(Intent i) {
        HashMap<String, String> outExtras = (HashMap<String, String>) i.getSerializableExtra(TransactionContract.COLUMN_PARSED_VARIABLES);

        if (outExtras != null && outExtras.containsKey(SENDER_PHONE_KEY) && outExtras.get(SENDER_PHONE_KEY) != null)
            return outExtras.get(SENDER_PHONE_KEY);
        else if (outExtras != null && outExtras.containsKey(SENDER_ACCOUNT_KEY) && outExtras.get(SENDER_ACCOUNT_KEY) != null)
            return outExtras.get(SENDER_ACCOUNT_KEY);
        else if (outExtras != null && outExtras.containsKey(RECIPIENT_PHONE_KEY) && outExtras.get(RECIPIENT_PHONE_KEY) != null)
            return outExtras.get(RECIPIENT_PHONE_KEY);
        else if (outExtras != null && outExtras.containsKey(RECIPIENT_ACCOUNT_KEY) && outExtras.get(RECIPIENT_ACCOUNT_KEY) != null)
            return outExtras.get(RECIPIENT_ACCOUNT_KEY);
        return null;
    }
}
