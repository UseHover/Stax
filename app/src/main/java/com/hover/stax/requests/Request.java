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
import com.hover.stax.utils.Utils;
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

	@ColumnInfo(name = "recipient_channel_id")
	public int requester_channel_id;

	@ColumnInfo(name = "requester_number")
	public String requester_number;

	@ColumnInfo(name = "amount")
	public String amount;

	@ColumnInfo(name = "note")
	public String note;

	@ColumnInfo(name = "message")
	public String message;

	@ColumnInfo(name = "matched_transaction_uuid")
	public String matched_transaction_uuid;

	@NonNull
	@ColumnInfo(name = "date_sent", defaultValue = "CURRENT_TIMESTAMP")
	public Long date_sent;

	public Request() {}

	public Request(String amount, String note, String requester, int receiving_channel_id, Context context) {
		this.amount = amount;
		this.note = note;
		this.requester_number = requester;
		this.requester_channel_id = receiving_channel_id;
		date_sent = DateUtils.now();
		description = getDescription(null, context);
	}

	public Request(String paymentLink) {
		String[] splitString = paymentLink.split(Constants.PAYMENT_LINK_SEPERATOR);
		amount = splitString[0].equals("0.00") ? "" : Utils.formatAmount(splitString[0]);
		requester_number = splitString[2];
		requester_channel_id = Integer.parseInt(splitString[1]);
	}

	public String getDescription(StaxContact contact, Context c) { return c.getString(R.string.descrip_request, contact.shortName()); }

	Request setRecipient(StaxContact c) { this.requestee_ids = c.id; return this; }

	public String generateSMS(Context c) {
		String amountString = amount != null ? c.getString(R.string.sms_amount_detail, Utils.formatAmount(amount)) : "";
		String noteString = note != null ? c.getString(R.string.sms_note_detail, note) : "";
		String paymentLink = generateStaxLink(c);

		if (paymentLink != null) return c.getString(R.string.sms_request_template_with_link, amountString, noteString, paymentLink);
		else return c.getString(R.string.sms_request_template_no_link, amountString, noteString);
	}

	 private String generateStaxLink(Context c) {
		 String amountNoFormat = amount != null ? amount : "0.00";
		String params = c.getString(R.string.payment_url_end, amountNoFormat, requester_channel_id, requester_number, DateUtils.now());

		try {
			Encryption encryption =  Request.getEncryptionSettings().build();
			String encryptedString = encryption.encryptOrNull(params);
			return c.getResources().getString(R.string.payment_root_url, encryptedString);
		} catch (NoSuchAlgorithmException e) {
			Amplitude.getInstance().logEvent(c.getString(R.string.stax_link_encryption_failure_2));
			return null;
		}
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
}
