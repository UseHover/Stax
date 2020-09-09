package com.hover.stax.transfer;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import com.hover.stax.ApplicationInstance;

public class StaxContactModel {
	private String id, name, phoneNumber;

	public StaxContactModel(Intent data) {
		Uri contactData = data.getData();
		if (contactData != null) {
			Cursor cur = ApplicationInstance.getContext().getContentResolver().query(contactData, null, null, null, null);
			if (cur != null && cur.getCount() > 0 && cur.moveToNext()) {
				id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
				name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

				if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
					Cursor phones = ApplicationInstance.getContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id, null, null);
					if (phones != null && phones.moveToNext()) {
						phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
					}
					if (phones != null) { phones.close(); }
				}
			}
			if (cur != null) { cur.close(); }
		}
	}

	public String getName() {
		return name;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}
}
