package com.hover.stax.permissions

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.hover.sdk.permissions.PermissionHelper
import com.hover.stax.R
import com.hover.stax.databinding.ManagePermissionsLayoutBinding
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.Utils
import com.uxcam.UXCam
import timber.log.Timber

class ManagePermissionsFragment : Fragment() {

    private var _binding: ManagePermissionsLayoutBinding? = null
    private val binding get() = _binding!!
    private lateinit var ph: PermissionHelper

    private val grantResults = arrayOf(PackageManager.PERMISSION_DENIED, PackageManager.PERMISSION_GRANTED)

    private val requestSMSPermLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grantResults ->
        if (grantResults.containsValue(false))
            Timber.i("SMS permissions denied")
        else
            Timber.i("SMS permissions granted")
    }

    private val requestPhonePermLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grantResults ->
        if (grantResults.containsValue(false))
            Timber.i("Phone permissions denied")
        else
            Timber.i("Phone permissions granted")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ManagePermissionsLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        UXCam.tagScreenName(getString(R.string.manage_permissions_screen))
        ph = PermissionHelper(requireActivity())
    }

    override fun onResume() {
        super.onResume()
        updateSwitches()
    }

    private fun updateSwitches() = with(binding) {
        //only way to handle manual toggles without impacting accessibility
        callsPermissionSwitch.apply {
            setOnCheckedChangeListener(null)
            isChecked = ph.hasPhonePerm()
            setOnCheckedChangeListener(callsPermCheckedChangeListener)
        }

        smsPermissionSwitch.apply {
            setOnCheckedChangeListener(null)
            isChecked = ph.hasSmsPerm()
            setOnCheckedChangeListener(smsPermCheckedChangeListener)
        }

        displayPermissionSwitch.apply {
            setOnCheckedChangeListener(null)
            isChecked = ph.hasOverlayPerm()
            setOnCheckedChangeListener(displayPermCheckedChangeListener)
        }

        accessibilityPermissionSwitch.apply {
            setOnCheckedChangeListener(null)
            isChecked = ph.hasAccessPerm()
            setOnCheckedChangeListener(accessPermCheckedChangeListener)
        }
    }

    private val callsPermCheckedChangeListener = CompoundButton.OnCheckedChangeListener { _, _ ->
        when {
            shouldOpenSettings(listOf(Manifest.permission.CALL_PHONE, Manifest.permission.READ_PHONE_STATE)) -> openAppDetailSettings()
            shouldShowRequestPermissionRationale(Manifest.permission.CALL_PHONE)
                    || shouldShowRequestPermissionRationale(Manifest.permission.READ_PHONE_STATE) -> showRationale { requestPhonePerms() }
            else -> requestPhonePerms()
        }
    }

    private val smsPermCheckedChangeListener = CompoundButton.OnCheckedChangeListener { _, _ ->
        when {
            shouldOpenSettings(listOf(Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS)) -> openAppDetailSettings()
            shouldShowRequestPermissionRationale(Manifest.permission.READ_SMS)
                    || shouldShowRequestPermissionRationale(Manifest.permission.RECEIVE_SMS) -> showRationale { requestSMSPerms() }
            else -> requestSMSPerms()
        }
    }

    private val displayPermCheckedChangeListener = CompoundButton.OnCheckedChangeListener { _, _ -> ph.requestOverlayPerm() }

    private val accessPermCheckedChangeListener = CompoundButton.OnCheckedChangeListener { _, _ -> ph.requestAccessPerm() }

    private fun shouldOpenSettings(permissions: List<String>): Boolean {
        return grantResults.contains(ContextCompat.checkSelfPermission(requireActivity(), permissions[0])) ||
                grantResults.contains(ContextCompat.checkSelfPermission(requireActivity(), permissions[1]))
    }

    private fun openAppDetailSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY
            data = Uri.fromParts("package", Utils.getPackage(requireContext()), null)
        }
        startActivity(intent)
    }

    private fun showRationale(permsToRequest: () -> Unit) {
        PermissionUtils.showInformativeBasicPermissionDialog(
            0,
            { permsToRequest() },
            { AnalyticsUtil.logAnalyticsEvent(getString(R.string.perms_basic_cancelled), requireActivity()) }, requireActivity()
        )
    }

    private fun requestSMSPerms() = requestSMSPermLauncher.launch(arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS))

    private fun requestPhonePerms() = requestPhonePermLauncher.launch(arrayOf(Manifest.permission.CALL_PHONE, Manifest.permission.READ_PHONE_STATE))

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}