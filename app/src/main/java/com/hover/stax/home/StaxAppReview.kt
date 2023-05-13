/*
 * Copyright 2022 Stax
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hover.stax.home

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import com.google.android.play.core.review.ReviewManagerFactory
import com.hover.stax.R
import com.hover.stax.core.Utils

const val APP_RATED_NATIVELY = "app_has_been_rated_natively"

internal object StaxAppReview {

    fun launchStaxReview(activity: Activity) {
        com.hover.stax.core.AnalyticsUtil.logAnalyticsEvent(activity.getString(R.string.visited_rating_review_screen), activity)
        launchReviewDialog(activity)
    }

    private fun launchReviewDialog(activity: Activity) {
        val reviewManager = ReviewManagerFactory.create(activity)
        reviewManager.requestReviewFlow().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                reviewManager.launchReviewFlow(activity, task.result).addOnCompleteListener {
                    Utils.saveBoolean(APP_RATED_NATIVELY, true, activity)
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