package com.hover.stax.addAccounts

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.hover.stax.R
import com.hover.stax.data.local.accounts.AccountRepo
import com.hover.stax.domain.model.CRYPTO_TYPE
import com.hover.stax.domain.model.USDCAccount
import com.hover.stax.utils.AnalyticsUtil
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.stellar.sdk.*
import org.stellar.sdk.Network.TESTNET
import org.stellar.sdk.responses.AccountResponse
import org.stellar.sdk.responses.SubmitTransactionResponse
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
	val secret = MutableLiveData<String>()

	private val _downloadEvent = Channel<String?>()
	val downloadEvent = _downloadEvent.receiveAsFlow()

	private val _doneEvent = MutableStateFlow(false)
	val doneEvent = _doneEvent.asSharedFlow()

	private val _error = MutableStateFlow(-1)
	val error = _error.asSharedFlow()

	val server = Server(application.getString(R.string.stellar_url))

	fun setPin(pin: String) {
		initialPin.postValue(pin)
	}

	fun confirmPin(pin: String): Boolean {
		if (initialPin.value != pin) return false
		confirmPin.postValue(pin)
		return true
	}

	private val coroutineExceptionHandler = CoroutineExceptionHandler{ _, throwable ->
		_error.value = R.string.create_account_error
	}

	fun createAccount() {
		viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
			val keyPair = createKeypair()
			generateRemoteAccount(keyPair.accountId)
			secret.postValue(String(keyPair.secretSeed))
			generateStaxAccountsForEachBalance(keyPair)
		}
	}

	private fun createKeypair(): StellarKeyPair {
		val pair: StellarKeyPair = StellarKeyPair.random()
		Timber.e("accountId ${pair.accountId}")
		Timber.e("seed ${String(pair.secretSeed)}")
		return pair
	}

	private fun generateRemoteAccount(accountId: String) {
		val friendbotUrl = getApplication<Application>().getString(R.string.friendbot_url, accountId)
		val response: InputStream = URL(friendbotUrl).openStream()
		val body: String = Scanner(response, "UTF-8").useDelimiter("\\A").next()
		Timber.e("SUCCESS! You have a new account :)\n$body")
	}

	private fun generateStaxAccountsForEachBalance(pair: StellarKeyPair) = viewModelScope.launch(Dispatchers.IO) {
		val createAccountResponse: AccountResponse = server.accounts().account(pair.accountId)
		sponsorAccount(pair)
		createAccounts(pair)
		logUSDCAdded()
	}

	private fun sponsorAccount(userAccountPair: StellarKeyPair) {
		val hoverKeypair = getHoverKeypair()
//		val a = Asset.create("ABCD", hoverKeypair.publicKey)

		val transaction: Transaction = TransactionBuilder(getHoverAccount(hoverKeypair), TESTNET)
			.addOperation(
				BeginSponsoringFutureReservesOperation.Builder(
					userAccountPair.accountId
				).build()
			)
			.addOperation(
				ChangeTrustOperation.Builder(
					ChangeTrustAsset.create(AssetTypeNative()), "1000" // FIXME what is stellar usdc asset's canonicalForm?
				).build()
			)
			.addOperation(
				EndSponsoringFutureReservesOperation()
			)
			.setBaseFee(Transaction.MIN_BASE_FEE)
			.setTimeout(60)
//			.addPreconditions(TransactionPreconditions.TransactionPreconditionsBuilder().timeBounds(TimeBounds(0, 10000)).build())
			.build()
		Timber.e("Created transaction")

		transaction.sign(hoverKeypair)
		Timber.e("hover signed")
		transaction.sign(userAccountPair)
		Timber.e("user signed")

		val response: SubmitTransactionResponse? = server.submitTransaction(transaction)
		Timber.e("got response for sponsorship transaction: %s", response)
	}

	suspend fun createAccounts(pair: StellarKeyPair) {
		val updateBalancesResponse: AccountResponse = server.accounts().account(pair.accountId)

		for (balance in updateBalancesResponse.balances) {
			createStaxAccount(pair, balance)
		}
	}

	private suspend fun createStaxAccount(pair: StellarKeyPair, balance: AccountResponse.Balance) {
		val acct = USDCAccount("Stellar USDC", "Stellar USDC", "logo", pair.accountId, -1, CRYPTO_TYPE, "Stellar color 1", "stellar color 2",
			balance.assetType, balance.assetCode.orNull(), false)
		acct.updateBalance(balance.balance, null)
		encryptSecret(acct, confirmPin.value!!, String(pair.secretSeed))
		Timber.e(
			"Creating account: %s, Code: %s, Balance: %s%n",
			balance.assetType,
			balance.assetCode,
			balance.balance
		)

		accountRepo.insert(acct)
		account.emit(acct)
	}

	private fun getHoverKeypair(): StellarKeyPair {
//		val pair: StellarKeyPair = StellarKeyPair.random()
//		Timber.e("accountId ${pair.accountId}")
//		Timber.e("seed ${String(pair.secretSeed)}")

//		test network
//		key: GCZCGXB5MMEV5YE43VYPOHZX7X4ZNFBX64WAJYV6FCKJREIXGJCQDA6E
//		secret: SC6L42WPVK37KSUJC5DYURNK55QS4FFGMWJOBF273QPNLN2YKSAYGU5O

//		public network
//		key: GA7AA5FUJYYNE2SXZ7LJLWGZ7OS72AGK7N7ARQ65Q6UK3M6KTVIIKIGM
//		secret: SCMNZQNV2PAJIRIHEUYW3QRWK56OABA37YI6R43JYCCDHSBZLKNSFRKS

		val pair: StellarKeyPair = StellarKeyPair.fromSecretSeed("SC6L42WPVK37KSUJC5DYURNK55QS4FFGMWJOBF273QPNLN2YKSAYGU5O")
		Timber.e("Got account with address %s", pair.accountId)
//		generateRemoteAccount(pair.accountId)
		return pair
	}

	private fun getHoverAccount(hoverKeypair: StellarKeyPair): AccountResponse {
		return server.accounts().account(hoverKeypair.accountId)
	}

	private fun encryptSecret(account: USDCAccount, pin: String, secret: String) {
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
		val iv2 = ByteArray(cipher.blockSize)
		random.nextBytes(iv2)
		val ivParams = IvParameterSpec(iv2)
		cipher.init(Cipher.ENCRYPT_MODE, key, ivParams)

		account.encryptedKey = cipher.doFinal(secretAsBytes)
		account.initializationVector = cipher.iv
		account.salt = salt
	}

	fun decryptSecret(account: USDCAccount, pin: String): String? {
		val iterationCount = 1000
		val keyLength = 256

		val keySpec = PBEKeySpec(pin.toCharArray(), account.salt, iterationCount, keyLength)
		val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
		val keyBytes = keyFactory.generateSecret(keySpec).encoded
		val key = SecretKeySpec(keyBytes, "AES");

		val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
		val iv = IvParameterSpec(account.initializationVector)
		cipher.init(Cipher.DECRYPT_MODE, key, iv)

		return try {
			val plaintext = cipher.doFinal(account.encryptedKey)
			String(plaintext)
		} catch(e: Exception) {
			null
		}
	}

	fun updateBalances(account: USDCAccount) = viewModelScope.launch(Dispatchers.IO) {
		val updateBalancesResponse: AccountResponse = server.accounts().account(account.accountNo)
		val accounts = accountRepo.getUsdcAccounts()

		for (balance in updateBalancesResponse.balances) {
			accounts.find { it.accountNo == account.accountNo && it.assetCode == account.assetCode }?.also {
				it.updateBalance(balance.balance, null)
				accountRepo.update(it)
			}
		}
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

	fun downloadKey(decryptedKey: String) = viewModelScope.launch(Dispatchers.IO) {
		_downloadEvent.send(decryptedKey)
	}

	fun downloadKey() = viewModelScope.launch(Dispatchers.IO) {
		secret.value?.let { downloadKey(it) }
	}

	fun done() = viewModelScope.launch(Dispatchers.IO) {
		Timber.e("emitting done")
		_doneEvent.emit(true)
	}
}