package com.hover.stax.contacts;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

@Entity(tableName = "stax_contacts", indices = {@Index(value = {"lookup_key"}, unique = true)})
public class StaxContact {

	@PrimaryKey
	@NonNull
	public String id;

	@ColumnInfo(name = "lookup_key")
	public String lookupKey;

	@ColumnInfo(name = "name")
	public String name;

	@ColumnInfo(name = "aliases")
	public String aliases;

	@ColumnInfo(name = "phone_number")
	public String phoneNumber;

	@ColumnInfo(name = "thumb_uri")
	public String thumbUri;

	public StaxContact() {}

	public StaxContact(Intent data, Context c) {
		Uri contactData = data.getData();
		if (contactData != null) {
			Cursor cur = c.getContentResolver().query(contactData, null, null, null, null);
			if (cur != null && cur.getCount() > 0 && cur.moveToNext()) {
				id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
				lookupKey = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
				name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				thumbUri = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI));

				if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
					Cursor phones = c.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id, null, null);
					if (phones != null && phones.moveToNext())
						phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
					if (phones != null) phones.close();
				}
			}
			if (cur != null) cur.close();
		}
	}

	public String normalizedNumber(String country) {
		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
		String number = phoneNumber;
		try {
			Phonenumber.PhoneNumber phone = phoneUtil.parse(phoneNumber, country);
			number = phoneUtil.formatNumberForMobileDialing(phone, country, false);
			Log.e("Contact", "Normalized number: " + number);
		} catch (NumberParseException e) {
			Log.e("Contact", "error formating number", e);
		}
		return number;
	}
}
