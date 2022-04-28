package com.hover.stax.permissions

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        handleSwitchListeners()
    }

    override fun onResume() {
        super.onResume()
        updateSwitches()
    }

    private fun updateSwitches() {
        binding.callsPermissionSwitch.isChecked = ph.hasPhonePerm()
        binding.smsPermissionSwitch.isChecked = ph.hasSmsPerm()
        binding.displayPermissionSwitch.isChecked = ph.hasOverlayPerm()
        binding.accessibilityPermissionSwitch.isChecked = ph.hasAccessPerm()
    }

    private fun handleSwitchListeners() {
        binding.callsPermissionSwitch.setOnClickListener { if (binding.callsPermissionSwitch.isChecked) ph.requestPhone(requireActivity(), 1) else openAppDetailSettings() }
        binding.smsPermissionSwitch.setOnClickListener { if (binding.smsPermissionSwitch.isChecked) requestSMSPerms() else openAppDetailSettings() }
        binding.displayPermissionSwitch.setOnClickListener { ph.requestOverlayPerm() }
        binding.accessibilityPermissionSwitch.setOnClickListener { ph.requestAccessPerm() }
    }

    private fun openAppDetailSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY)
        val uri: Uri = Uri.fromParts("package", Utils.getPackage(requireContext()), null)
        intent.data = uri
        startActivity(intent)
    }

    private fun requestSMSPerms() = requestSMSPermLauncher.launch(arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS))

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}