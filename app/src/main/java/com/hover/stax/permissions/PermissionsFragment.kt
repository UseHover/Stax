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

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.hover.sdk.api.Hover
import com.hover.sdk.permissions.PermissionHelper
import com.hover.stax.R
import com.hover.stax.hover.PERM_ACTIVITY
import com.hover.stax.utils.AnalyticsUtil.logAnalyticsEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PermissionsFragment : DialogFragment() {

    private lateinit var helper: PermissionHelper
    private var dialog: StaxPermissionDialog? = null
    private var current = 0
    private var hasLeft = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        helper = PermissionHelper(context)
        current = if (helper.hasOverlayPerm()) ACCESS else OVERLAY

        logAnalyticsEvent(getString(if (current == OVERLAY) R.string.perms_overlay_dialog else R.string.perms_accessibility_dialog), requireContext())

        dialog = StaxPermissionDialog(requireActivity())
            .setDialogTitle(R.string.perm_dialoghead)
//            .setDialogMessage(getString(R.string.perm_dialogbody, arguments?.getString(REASON)))
            .setNegButton(R.string.btn_cancel) { cancel() }
            .setPosButton(R.string.perm_cta1) { requestOverlay() }
            .highlightPos() as StaxPermissionDialog
        maybeUpdateToNext()
        return dialog!!.createIt()
    }

    private fun requestOverlay() {
        hasLeft = true
        logAnalyticsEvent(getString(R.string.perms_overlay_requested), requireContext())
        helper.requestOverlayPerm()
    }

    private fun requestAccessibility() {
        hasLeft = true
        logAnalyticsEvent(getString(R.string.perms_accessibility_requested), requireContext())
        Hover.setPermissionActivity(PERM_ACTIVITY, context)
        helper.requestAccessPerm()
    }

    override fun onResume() {
        super.onResume()
        logReturnEvent()
        maybeUpdateToNext()
    }

    private fun logReturnEvent() {
        if (hasLeft) {
            if (current == OVERLAY) logAnalyticsEvent(
                getString(
                    if (helper.hasOverlayPerm()) R.string.perms_overlay_granted
                    else R.string.perms_overlay_notgranted
                ),
                requireContext()
            ) else if (current == ACCESS)
                logAnalyticsEvent(
                    getString(
                        if (helper.hasAccessPerm()) R.string.perms_accessibility_granted
                        else R.string.perms_accessibility_notgranted
                    ),
                    requireContext()
                )
        }
    }

    private fun maybeUpdateToNext() {
        if (arguments?.getInt(STARTWITH) == ACCESS && !helper.hasAccessPerm())
            setOnlyNeedAccess()
        else if (current == OVERLAY && helper.hasOverlayPerm() && !helper.hasAccessPerm()) {
            lifecycleScope.launch {
                delay(300)
                animateToStep2()
            }
        } else if (helper.hasAccessPerm()) {
            animateToDone()
        }
    }

    private fun setOnlyNeedAccess() {
        animateToStep2()
        dialog?.let {
            with(it) {
                (view.findViewById<View>(R.id.progress_text) as TextView).text = getString(R.string.perm_progress_no_steps)
                (view.findViewById<View>(R.id.progress_text) as TextView).textSize = 16f
                view.findViewById<View>(R.id.progress_indicator).visibility = View.GONE
//                setDialogMessage(getString(R.string.perm_accessibiltiy_dialogbody, arguments?.getString(REASON)))
                setPosButton(R.string.perm_cta1) { requestAccessibility() }
            }
        }
    }

    private fun animateToStep2() {
        current = ACCESS

        dialog?.let {
            with(it) {
                animateProgressTo(81)
                (view.findViewById<View>(R.id.progress_text) as TextView).text = getString(R.string.perm_progress2)
                (view.findViewById<View>(R.id.perm_message) as TextView).text = getString(R.string.permissions_accessibility_desc)
                setHelperIcon(R.drawable.ic_accessibility)
                setPath(R.string.permissions_accessibility_path)
                view.findViewById<View>(R.id.overlay_example).visibility = View.GONE
                view.findViewById<View>(R.id.accessibility_example).visibility = View.VISIBLE
                view.findViewById<View>(R.id.accessibility_more).visibility = View.VISIBLE
                view.findViewById<View>(R.id.accessibility_more).setOnClickListener { toggleDataInfo(view, true) }
                setPosButton(R.string.perm_cta2) { requestAccessibility() }
            }
        }
    }

    private fun toggleDataInfo(v: View, show: Boolean) {
        (v.findViewById<View>(R.id.accessibility_more) as TextView).setCompoundDrawablesWithIntrinsicBounds(getArrow(show),  0, 0, 0)
        v.findViewById<View>(R.id.accessibility_data_info)?.visibility = if (show) View.VISIBLE else View.GONE
        v.findViewById<View>(R.id.accessibility_more)?.setOnClickListener { toggleDataInfo(v, !show) }
    }

    private fun getArrow(show: Boolean): Int {
        return if (show) R.drawable.ic_chevron_down
        else R.drawable.ic_chevron_right
    }

    private fun animateToDone() {
        dialog?.animateProgressTo(100)
        requireActivity().setResult(Activity.RESULT_OK)

        lifecycleScope.launch {
            delay(if (arguments?.getInt(STARTWITH) == ACCESS) 10 else 800.toLong())
            requireActivity().finish()
        }
    }

    private fun cancel() {
        logAnalyticsEvent(getString(if (current == OVERLAY) R.string.perms_overlay_cancelled else R.string.perms_accessibility_cancelled), requireContext())
        dialog?.dismiss()
        requireActivity().apply {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        cancel()
    }

    companion object {
        const val SMS = 1
        const val OVERLAY = 2
        const val ACCESS = 3
        private const val REASON = "reason"
        private const val STARTWITH = "start_with"

        fun newInstance(reason: String?, onlyAccessibility: Boolean): PermissionsFragment = PermissionsFragment().apply {
            arguments = bundleOf(REASON to reason, STARTWITH to if (onlyAccessibility) ACCESS else OVERLAY)
        }
    }
}