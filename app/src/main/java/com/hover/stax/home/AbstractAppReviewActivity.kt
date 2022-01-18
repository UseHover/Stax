package com.hover.stax.home

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.google.android.play.core.review.ReviewManagerFactory
import com.hover.stax.R
import com.hover.stax.transactions.TransactionHistoryViewModel
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.Constants
import com.hover.stax.utils.Utils
import org.koin.androidx.viewmodel.ext.android.viewModel

abstract class AbstractAppReviewActivity : AbstractSDKCaller() {
    private val historyViewModel: TransactionHistoryViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeForAppReview()
    }

    private fun observeForAppReview() = historyViewModel.showAppReviewLiveData().observe(this, { if (it) launchReviewDialog() })

    fun launchStaxReview() {
        AnalyticsUtil.logAnalyticsEvent(getString(R.string.visited_rating_review_screen), this)

        if (Utils.getBoolean(Constants.APP_RATED_NATIVELY, this))
            openStaxPlaystorePage()
        else
            launchReviewDialog()
    }

    private fun launchReviewDialog() {
        val reviewManager = ReviewManagerFactory.create(this)
        reviewManager.requestReviewFlow().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                reviewManager.launchReviewFlow(this@AbstractAppReviewActivity, task.result).addOnCompleteListener {
                    Utils.saveBoolean(Constants.APP_RATED_NATIVELY, true, this@AbstractAppReviewActivity)
                }
            }
        }
    }

    private fun openStaxPlaystorePage() {
        val link = Uri.parse(getString(R.string.stax_market_playstore_link))
        val intent = Intent(Intent.ACTION_VIEW, link).apply {
            addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        }

        try {
            startActivity(intent)
        } catch (nf: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.stax_url_playstore_review_link))))
        }
    }
}