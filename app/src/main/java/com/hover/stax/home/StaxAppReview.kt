package com.hover.stax.home

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import com.google.android.play.core.review.ReviewManagerFactory
import com.hover.stax.R
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.Constants
import com.hover.stax.utils.Utils

internal object StaxAppReview {

    fun launchStaxReview(activity: Activity) {
        AnalyticsUtil.logAnalyticsEvent(activity.getString(R.string.visited_rating_review_screen), activity)

        if (Utils.getBoolean(Constants.APP_RATED_NATIVELY, activity))
            openStaxPlaystorePage(activity)
        else
            launchReviewDialog(activity)
    }

    private fun launchReviewDialog(activity: Activity) {
        val reviewManager = ReviewManagerFactory.create(activity)
        reviewManager.requestReviewFlow().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                reviewManager.launchReviewFlow(activity, task.result).addOnCompleteListener {
                    Utils.saveBoolean(Constants.APP_RATED_NATIVELY, true, activity)
                }
            }
        }
    }

    private fun openStaxPlaystorePage(activity: Activity) {
        val link = Uri.parse(activity.getString(R.string.stax_market_playstore_link))
        val intent = Intent(Intent.ACTION_VIEW, link).apply {
            addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        }

        try {
            activity.startActivity(intent)
        } catch (nf: ActivityNotFoundException) {
            activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(activity.getString(R.string.stax_url_playstore_review_link))))
        }
    }
}