/*
 * Copyright 2023 Stax
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
package com.hover.stax.hover

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.api.HoverParameters
import com.hover.stax.notifications.PushNotificationTopicsInterface
import timber.log.Timber

class BountyContract :
    ActivityResultContract<HoverAction, Intent?>(),
    PushNotificationTopicsInterface {

    override fun createIntent(context: Context, a: HoverAction): Intent {
        updatePushNotifGroupStatus(context, a)
        return HoverParameters.Builder(context).request(a.public_id).setEnvironment(HoverParameters.MANUAL_ENV).buildIntent()
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Intent? {
        // We don't care about the resultCode - bounties are currently always cancelled because they rely on the timer running out to end.
        if (intent == null || intent.extras == null || intent.extras!!.getString("uuid") == null) {
            Timber.e("Bounty result got null transaction data")
        }
        return intent
    }

    private fun updatePushNotifGroupStatus(c: Context, a: HoverAction) {
        joinAllBountiesGroup(c)
        joinBountyCountryGroup(a.country_alpha2, c)
    }
}