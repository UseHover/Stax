package com.hover.stax.permissions

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.hover.stax.R
import com.hover.sdk.permissions.PermissionHelper
import android.app.Activity
import com.hover.stax.permissions.PermissionsFragment
import com.hover.sdk.actions.HoverAction

class PermissionsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permissions)

        if (PermissionHelper(this).hasAllPerms()) {
            setResult(RESULT_OK)
            finish()
        } else showDialog()
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