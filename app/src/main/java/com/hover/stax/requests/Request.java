package com.hover.stax.requests;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.amplitude.api.Amplitude;
import com.hover.stax.R;
import com.hover.stax.database.Constants;
import com.hover.stax.contacts.StaxContact;
import com.hover.stax.utils.DateUtils;
import com.hover.stax.utils.paymentLinkCryptography.Base64;
import com.hover.stax.utils.paymentLinkCryptography.Encryption;

import java.security.NoSuchAlgorithmException;

@Entity(tableName = "requests")
public class Request {

	@PrimaryKey(autoGenerate = true)
	@NonNull
	public int id;

	@ColumnInfo(name = "description")
	public String description;

	@NonNull
	@ColumnInfo(name = "requestee_ids")
	public String requestee_ids;

	@ColumnInfo(name = "amount")
	public String amount;

	@ColumnInfo(name = "requester_institution_id")
	public int requester_institution_id;

	@ColumnInfo(name  = "requester_number")
	public String requester_number;

	@ColumnInfo(name = "note")
	public String note;

	@ColumnInfo(name = "message")
	public String message;

	@ColumnInfo(name = "matched_transaction_uuid")
	public String matched_transaction_uuid;

	@NonNull
	@ColumnInfo(name = "date_sent", defaultValue = "CURRENT_TIMESTAMP")
	public Long date_sent;

	public Request() { }

	public Request(StaxContact requestee, String a, String n, String requester_number, int institution_id, Context context) {
		requestee_ids = requestee.id;
		amount = a;
		note = n;
		requester_institution_id = institution_id;
		this.requester_number = requester_number;
		date_sent = DateUtils.now();
		description = getDescription(requestee, context);
	}

	public String getDescription(StaxContact contact, Context c) {
		return c.getString(R.string.descrip_request, contact.shortName());
	}

	public static Encryption.Builder getEncryptionSettings() {
		//PUTTING THIS HERE FOR NOW, BUT THIS SETTINGS OUGHT TO BE IN THE REPO SO SETTINGS COMES FROM ONLINE SERVER.
		return new Encryption.Builder()
					   .setKeyLength(128)
					   .setKeyAlgorithm("AES")
					   .setCharsetName("UTF8")
					   .setIterationCount(65536)
					   .setKey("ves€Z€xs€aBKgh")
					   .setDigestAlgorithm("SHA1")
					   .setSalt("A secured salt")
					   .setBase64Mode(Base64.DEFAULT)
					   .setAlgorithm("AES/CBC/PKCS5Padding")
					   .setSecureRandomAlgorithm("SHA1PRNG")
					   .setSecretKeyType("PBKDF2WithHmacSHA1")
					   .setIv(new byte[] { 29, 88, -79, -101, -108, -38, -126, 90, 52, 101, -35, 114, 12, -48, -66, -30 });
	}

	 static String generateStaxLink(String amount, int channel_id, String accountNumber, Context c) {
		if (channel_id == 0 || accountNumber.isEmpty()) {
			Amplitude.getInstance().logEvent(c.getString(R.string.stax_link_encryption_failure_1));
			return null;
		}
		String separator = Constants.PAYMENT_LINK_SEPERATOR;
		String fullString = amount+separator+channel_id +separator+accountNumber+separator+DateUtils.now();

		try {
			Encryption encryption =  Request.getEncryptionSettings().build();
			String encryptedString = encryption.encryptOrNull(fullString);
			return c.getResources().getString(R.string.payment_root_url)+encryptedString;

		} catch (NoSuchAlgorithmException e) {
			Amplitude.getInstance().logEvent(c.getString(R.string.stax_link_encryption_failure_2));
			return null;
		}
	}
}
