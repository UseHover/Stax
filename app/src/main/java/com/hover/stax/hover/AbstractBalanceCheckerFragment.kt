package com.hover.stax.hover

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.domain.model.Account
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.NavUtil
import com.hover.stax.utils.UIHelper

abstract class AbstractBalanceCheckerFragment: Fragment() {

	protected val checkBalance = registerForActivityResult(TransactionContract()) { data: Intent? ->
		if (data != null && data.extras != null && data.extras!!.getString("uuid") != null) {
			NavUtil.showTransactionDetailsFragment(findNavController(), data.extras!!.getString("uuid")!!)
		}
	}

	protected fun generateSessionBuilder(account: Account, action: HoverAction): HoverSession.Builder {
		return HoverSession.Builder(action, account, requireActivity())
	}

	protected fun callHover(launcher: ActivityResultLauncher<HoverSession.Builder>, b: HoverSession.Builder) {
		try {
			launcher.launch(b)
		} catch (e: Exception) {
			requireActivity().runOnUiThread { UIHelper.flashAndReportMessage(requireContext(), getString(
				R.string.error_running_action)) }
			AnalyticsUtil.logErrorAndReportToFirebase(b.action.public_id, getString(R.string.error_running_action_log), e)
		}
	}
}