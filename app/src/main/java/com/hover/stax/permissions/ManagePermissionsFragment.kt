package com.hover.stax.permissions

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.hover.sdk.permissions.PermissionHelper
import com.hover.stax.databinding.ManagePermissionsLayoutBinding
import com.hover.stax.utils.Utils
import timber.log.Timber

class ManagePermissionsFragment : Fragment() {

    private var _binding: ManagePermissionsLayoutBinding? = null
    private val binding get() = _binding!!
    private lateinit var ph: PermissionHelper

    private val requestSMSPermLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grantResults ->
        if (grantResults.containsValue(false))
            Timber.i("Permissions denied")
        else
            Timber.i("Permissions granted")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ManagePermissionsLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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

    private val callsPermCheckedChangeListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
        if (isChecked) ph.requestPhone(requireActivity(), 1) else openAppDetailSettings()
    }

    private val smsPermCheckedChangeListener = CompoundButton.OnCheckedChangeListener { _, isChecked -> if (isChecked) requestSMSPerms() else openAppDetailSettings() }

    private val displayPermCheckedChangeListener = CompoundButton.OnCheckedChangeListener { _, _ -> ph.requestOverlayPerm() }

    private val accessPermCheckedChangeListener = CompoundButton.OnCheckedChangeListener { _, _ -> ph.requestAccessPerm() }

    private fun openAppDetailSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY
            data = Uri.fromParts("package", Utils.getPackage(requireContext()), null)
        }
        startActivity(intent)
    }

    private fun requestSMSPerms() = requestSMSPermLauncher.launch(arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS))

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}