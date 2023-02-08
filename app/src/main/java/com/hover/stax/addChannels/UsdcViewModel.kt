package com.hover.stax.addChannels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.hover.stax.R
import com.hover.stax.data.local.accounts.AccountRepo
import com.hover.stax.domain.model.CRYPTO_TYPE
import com.hover.stax.domain.model.USDCAccount
import com.hover.stax.utils.AnalyticsUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.stellar.sdk.KeyPair
import org.stellar.sdk.Server
import org.stellar.sdk.responses.AccountResponse
import timber.log.Timber
import java.io.InputStream
import java.net.URL
import java.util.*


class UsdcViewModel(application: Application, val accountRepo: AccountRepo) : AndroidViewModel(application) {

	public fun createAccount() {
		viewModelScope.launch(Dispatchers.IO) {
			val pair: KeyPair = KeyPair.random()

			val seed = String(pair.getSecretSeed())
			Timber.e("seed $seed")

			val friendbotUrl = "https://friendbot.stellar.org/?addr=" + pair.getAccountId()
			val response: InputStream = URL(friendbotUrl).openStream()
			val body: String = Scanner(response, "UTF-8").useDelimiter("\\A").next()
			Timber.e("SUCCESS! You have a new account :)\n$body")

			balanceCheck(pair)
		}
	}

	private fun balanceCheck(pair: KeyPair) = viewModelScope.launch(Dispatchers.IO) {
		val server = Server("https://horizon-testnet.stellar.org")
		val accountNo: AccountResponse = server.accounts().account(pair.getAccountId())
		Timber.e("Balances for account " + pair.getAccountId())
		for (balance in accountNo.getBalances()) {
			val acct = USDCAccount("Stellar USDC", "Stellar USDC", "logo", pair.getAccountId(), -1, CRYPTO_TYPE, "Stellar color 1", "stellar color 2",
				"type", "code", false)
			acct.updateBalance(balance.getBalance(), null)
			Timber.e(
				"Type: %s, Code: %s, Balance: %s%n",
				balance.getAssetType(),
				balance.getAssetCode(),
				balance.getBalance()
			)

			accountRepo.insert(acct)
		}
		log()
	}

	private fun log() {
		FirebaseMessaging.getInstance().subscribeToTopic((getApplication() as Context).getString(R.string.firebase_topic_usdc))
		AnalyticsUtil.logAnalyticsEvent((getApplication() as Context).getString(R.string.added_usdc), getApplication() as Context)
	}
}