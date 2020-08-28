package com.hover.stax.database;

import android.content.Context;
import android.os.SystemClock;
import android.security.KeyPairGeneratorSpec;
import android.util.Base64;
import android.util.Log;

import com.hover.stax.utils.Utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.UnrecoverableKeyException;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Calendar;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.security.auth.x500.X500Principal;

@SuppressWarnings("CharsetObjectCanBeUsed")
final public class KeyStoreExecutor {
	private final static String TAG = "KeyStoreHelper";

	public static String createNewKey(final String value, final Context c) {
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
				String encryptedPin = encrypt(value, c);
				if (encryptedPin == null)
					encryptedPin = encrypt(value, c); // Try again then give up
				return encryptedPin;
//			}
//		}).start();
	}

	private static String encrypt(String value, Context c) {
		try {
			KeyStore.PrivateKeyEntry privateKeyEntry = getKeyEntry(false, c);

			RSAPublicKey publicKey = (RSAPublicKey) privateKeyEntry.getCertificate().getPublicKey();

			Cipher input = Cipher.getInstance("RSA/ECB/PKCS1Padding", "AndroidOpenSSL");
			input.init(Cipher.ENCRYPT_MODE, publicKey);

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, input);
			cipherOutputStream.write(value.getBytes("UTF-8"));
			cipherOutputStream.close();

			return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
		} catch (Exception e) {
			if (c != null) {
			}
			Log.println( Log.ERROR,TAG, c.getString(com.hover.sdk.R.string.hsdk_log_pin_encrypt_err));
		}
		return null;
	}

	public static String decrypt(String pin, Context c) {
		try {
			KeyStore.PrivateKeyEntry privateKeyEntry = getKeyEntry(true, c);

			Cipher output = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			output.init(Cipher.DECRYPT_MODE, privateKeyEntry.getPrivateKey());
			CipherInputStream cipherInputStream = new CipherInputStream(new ByteArrayInputStream(Base64.decode(pin, Base64.DEFAULT)), output);
			ArrayList<Byte> values = new ArrayList<>();
			int nextByte;
			while ((nextByte = cipherInputStream.read()) != -1)
				values.add((byte) nextByte);

			byte[] bytes = new byte[values.size()];
			for (int i = 0; i < bytes.length; i++)
				bytes[i] = values.get(i);

			return new String(bytes, 0, bytes.length, "UTF-8");
		} catch (Exception e) {
			Log.println( Log.ERROR,TAG, c.getString(com.hover.sdk.R.string.hsdk_log_pin_decrypt_err));
		}
		return null;
	}

	private static KeyStore.PrivateKeyEntry getKeyEntry(boolean forDecrypt, Context c) throws Exception {
		KeyStore keystore = KeyStore.getInstance("AndroidKeyStore");
		keystore.load(null);

		KeyStore.PrivateKeyEntry privateKeyEntry = null;
		int count = 0;
		do {
			try {
				privateKeyEntry = (KeyStore.PrivateKeyEntry) keystore.getEntry(getAlias(c), null);
				if (!forDecrypt && count == 0)
					generateKeyPair(c);
				count++;
			} catch (NullPointerException | UnrecoverableKeyException ignored) { } // seems to be bug in keystore that throws chain == null
			if (privateKeyEntry == null && count < 4) {
				SystemClock.sleep(250); // No idea if or why this would help...
			}
		} while (privateKeyEntry == null && count < 4);

		if (privateKeyEntry == null) {
		}
		return privateKeyEntry;
	}

	public static void generateKeyPair(final Context c) {
		try {
			KeyStore keystore = KeyStore.getInstance("AndroidKeyStore");
			keystore.load(null);
			if (!keystore.containsAlias(getAlias(c))) {
				Calendar start = Calendar.getInstance();
				Calendar end = Calendar.getInstance();
				end.add(Calendar.YEAR, 1);
				KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(c)
						.setAlias(getAlias(c))
						.setSubject(new X500Principal("CN=" + getAlias(c) + ", O=Android Authority"))
						.setSerialNumber(BigInteger.ONE)
						.setStartDate(start.getTime())
						.setEndDate(end.getTime())
						.build();
				KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
				generator.initialize(spec);
				generator.generateKeyPair();
			}
		} catch (Exception e) {
			Log.println( Log.ERROR,TAG, c.getString(com.hover.sdk.R.string.hsdk_log_keystore_err));
		}
	}

	private static String getAlias(Context c) { return "hsdk_" + Utils.getPackage(c) + "_standard"; }
}