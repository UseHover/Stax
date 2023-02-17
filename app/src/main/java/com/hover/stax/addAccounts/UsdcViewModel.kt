package com.hover.stax.addAccounts

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.hover.stax.R
import com.hover.stax.data.local.accounts.AccountRepo
import com.hover.stax.domain.model.CRYPTO_TYPE
import com.hover.stax.domain.model.USDCAccount
import com.hover.stax.utils.AnalyticsUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.stellar.sdk.Server
import org.stellar.sdk.responses.AccountResponse
import timber.log.Timber
import java.io.InputStream
import java.net.URL
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import org.stellar.sdk.KeyPair as StellarKeyPair


class UsdcViewModel(application: Application, val accountRepo: AccountRepo) : AndroidViewModel(application) {

	var initialPin: MediatorLiveData<String> = MediatorLiveData()
	var confirmPin: MediatorLiveData<String> = MediatorLiveData()

	val account = MutableSharedFlow<USDCAccount>()

	private val _doneEvent = MutableStateFlow(false)
	val doneEvent = _doneEvent.asSharedFlow()

	fun setPin(pin: String) {
		initialPin.postValue(pin)
	}

	fun confirmPin(pin: String): Boolean {
		if (initialPin.value != pin) return false
		confirmPin.postValue(pin)
		return true
	}

	fun createAccount() {
		viewModelScope.launch(Dispatchers.IO) {
			val keyPair = createKeypair()
			generateRemoteAccount(keyPair.accountId)

			generateStaxAccountsForEachBalance(keyPair)
		}
	}

	private fun createKeypair(): StellarKeyPair {
		val pair: StellarKeyPair = StellarKeyPair.random()
		Timber.e("accountId ${pair.accountId}")
		Timber.e("seed ${pair.secretSeed}")
		return pair
	}

	private fun generateRemoteAccount(accountId: String) {
		val friendbotUrl = "https://friendbot.stellar.org/?addr=$accountId"
		val response: InputStream = URL(friendbotUrl).openStream()
		val body: String = Scanner(response, "UTF-8").useDelimiter("\\A").next()
		Timber.e("SUCCESS! You have a new account :)\n$body")
	}

	private fun generateStaxAccountsForEachBalance(pair: StellarKeyPair) = viewModelScope.launch(Dispatchers.IO) {
		val server = Server("https://horizon-testnet.stellar.org")
		val accountNo: AccountResponse = server.accounts().account(pair.accountId)

		for (balance in accountNo.balances) {
			createStaxAccount(pair, balance)
		}
		logUSDCAdded()
	}

	private suspend fun createStaxAccount(pair: StellarKeyPair, balance: AccountResponse.Balance) {
		val acct = USDCAccount("Stellar USDC", "Stellar USDC", "logo", pair.accountId, -1, CRYPTO_TYPE, "Stellar color 1", "stellar color 2",
			balance.assetType, balance.assetCode.orNull(), false)
		acct.updateBalance(balance.balance, null)
		encryptSecret(acct, confirmPin.value!!, pair.secretSeed.toString())
		Timber.e(
			"Creating account: %s, Code: %s, Balance: %s%n",
			balance.assetType,
			balance.assetCode,
			balance.balance
		)

		accountRepo.insert(acct)
		account.emit(acct)
	}

	private fun encryptSecret(account: USDCAccount, pin: String, secret: String) {
		val pinAsBytes: ByteArray = pin.toByteArray(charset("UTF-8"))
		val secretAsBytes: ByteArray = secret.toByteArray(charset("UTF-8"))


		// One option
//		val key = SecretKeySpec(pinAsBytes, "AES")
//		val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
//		cipher.init(Cipher.ENCRYPT_MODE, key)

		// More secure option
		val iterationCount = 1000
		val keyLength = 256
		val saltLength = keyLength / 8 // same size as key output

		val random = SecureRandom()
		val salt = ByteArray(saltLength)
		random.nextBytes(salt)
		val keySpec = PBEKeySpec(pin.toCharArray(), salt, iterationCount, keyLength)
		val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
		val keyBytes = keyFactory.generateSecret(keySpec).getEncoded()
		val key = SecretKeySpec(keyBytes, "AES");

		val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
		val iv2 = ByteArray(cipher.getBlockSize())
		random.nextBytes(iv2)
		val ivParams = IvParameterSpec(iv2)
		cipher.init(Cipher.ENCRYPT_MODE, key, ivParams)

		account.encryptedKey = cipher.doFinal(secretAsBytes)
		account.initializationVector = cipher.iv
		account.salt = salt
	}

	private fun logUSDCAdded() {
		FirebaseMessaging.getInstance().subscribeToTopic((getApplication() as Context).getString(R.string.firebase_topic_usdc))
		AnalyticsUtil.logAnalyticsEvent((getApplication() as Context).getString(R.string.added_usdc), getApplication() as Context)
	}

	fun copyToClipboard(text: String) {
		val clipboardManager =
			(getApplication() as Application).getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
		val clip = ClipData.newPlainText("usdc address", text)
		clipboardManager.setPrimaryClip(clip)
	}

	fun done() = viewModelScope.launch(Dispatchers.IO) {
		Timber.e("emitting done")
		_doneEvent.emit(true)
	}
}