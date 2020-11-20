package com.hover.stax.requests;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.amplitude.api.Amplitude;
import com.hover.stax.R;
import com.hover.stax.database.Constants;
import com.hover.stax.utils.DateUtils;
import com.hover.stax.utils.paymentLinkCryptography.Base64;
import com.hover.stax.utils.paymentLinkCryptography.Encryption;

import java.security.NoSuchAlgorithmException;

@Entity(tableName = "requests")
public class Request {

	@PrimaryKey(autoGenerate = true)
	@NonNull
	public int id;

	@NonNull
	@ColumnInfo(name = "recipient")
	public String recipient;

	@ColumnInfo(name = "amount")
	public String amount;

	@ColumnInfo(name = "note")
	public String note;

	@ColumnInfo(name = "message")
	public String message;

	@ColumnInfo(name = "matched_transaction_uuid")
	public String matched_transaction_uuid;

	@ColumnInfo(name = "receiving_channel_id")
	public int receiving_channel_id;

	@ColumnInfo(name  = "receiving_account_number")
	public String receiving_account_number;

	@NonNull
	@ColumnInfo(name = "date_sent", defaultValue = "CURRENT_TIMESTAMP")
	public Long date_sent;

	public Request() {
	}

	public Request(@NonNull String recipient, String amount, String note, int receiving_channel_id, String receiving_account_number) {
		this.recipient = recipient;
		this.amount = amount;
		this.note = note;
		this.receiving_channel_id = receiving_channel_id;
		this.receiving_account_number = receiving_account_number;
		date_sent = DateUtils.now();
	}

	public String getDescription(Context c) {
		return c.getString(R.string.descrip_request, recipient);
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
		if(channel_id == 0 || accountNumber.isEmpty()) {
			Amplitude.getInstance().logEvent(c.getString(R.string.stax_link_encryption_failure_1));
			return null;
		}
		String separator = Constants.PAYMENT_LINK_SEPERATOR;
		String fullString = amount+separator+channel_id +separator+accountNumber+separator+DateUtils.today();

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
