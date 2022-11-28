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