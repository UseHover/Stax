package com.hover.stax.home

import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.hover.stax.R
import com.hover.stax.schedules.Schedule
import com.hover.stax.transfers.TransferViewModel
import com.hover.stax.utils.Constants
import com.hover.stax.utils.DateUtils
import com.hover.stax.utils.NavUtil
import com.hover.stax.utils.UIHelper
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

abstract class SDKLaunchers : AppCompatActivity() {
	private val transferViewModel : TransferViewModel by viewModel()
	val sdkLauncherForSingleBalance = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { intent ->
		Timber.i("Transaction details: data returned")
		intent.data?.let {
			val transactionUUID = it.getStringExtra("uuid")
			if (transactionUUID != null) {
				NavUtil.showTransactionDetailsFragment(transactionUUID, supportFragmentManager, true)
			}
		}
	}

	val sdkLauncherForFetchAccount = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { intent ->
		Timber.e("Accounts fetched")
		if (intent.resultCode == RESULT_OK) {
			intent.data?.let {
				val message: String = getString(R.string.accounts_fetched_success)
				UIHelper.flashMessage(this, findViewById(R.id.fab), message)
			}
		}
	}

	val sdkLauncherForBounty = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { intent ->
		Timber.e("Bounty data returned")
		intent.data?.let {
			val transactionUUID = it.getStringExtra("uuid")
			if (transactionUUID != null) NavUtil.showTransactionDetailsFragment(transactionUUID, supportFragmentManager, true)
		}
	}

	val sdkLauncherForPayBill = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { intent ->
		Timber.e("PayBill transaction data returned")
		intent.data?.let {
			val transactionUUID = it.getStringExtra("uuid")
			if (transactionUUID != null) NavUtil.showTransactionDetailsFragment(transactionUUID, supportFragmentManager, true)
		}
	}

	val sdkLauncherForTransfer = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { intent ->
		Timber.e("Transfer action returned")
		if (intent.resultCode == RESULT_OK) {
			onBackPressed()
			intent.data?.let {
				transferViewModel.reset()
				if(it.action == Constants.SCHEDULED) {
					val message = getString(R.string.toast_confirm_schedule, DateUtils.humanFriendlyDate(it.getLongExtra(
						Schedule.DATE_KEY, 0)))
					UIHelper.flashMessage(this, findViewById(R.id.fab), message)
				}
				else if (transferOccurred(it.extras)) {
					NavUtil.showTransactionDetailsFragment(it.extras!!.getString("uuid")!!, supportFragmentManager, false)
				}
			}
		}
		else if(intent.resultCode == RESULT_CANCELED) {
			Timber.e("Transfer cancelled")
			transferViewModel.setEditing(false)
		}
	}
	private fun transferOccurred(extras : Bundle?) = extras?.getString("uuid") != null
}