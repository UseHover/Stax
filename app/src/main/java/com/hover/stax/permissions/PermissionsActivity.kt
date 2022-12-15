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
package com.hover.stax.permissions

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.permissions.PermissionHelper
import com.hover.stax.R
import timber.log.Timber

class PermissionsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permissions)

        if (PermissionHelper(this).hasAllPerms()) {
            Timber.e("Has all perms, should wrap up now")
            setResult(RESULT_OK)
            finish()
        } else {
            Timber.e("Showing dialog again")
            showDialog()
        }
    }

    private fun showDialog() {
        val ft = supportFragmentManager.beginTransaction()
        val prev = supportFragmentManager.findFragmentByTag("dialog")
        if (prev != null) {
            ft.remove(prev)
        }
        ft.addToBackStack(null)
        val newFragment = PermissionsFragment.newInstance(
            HoverAction.getHumanFriendlyType(this, if (intent != null) intent.getStringExtra("transaction_type") else ""),
            PermissionHelper(this).hasOverlayPerm()
        )
        newFragment.show(ft, "dialog")
    }
}