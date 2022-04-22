package com.hover.stax.permissions

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.hover.sdk.permissions.PermissionHelper
import com.hover.stax.databinding.ManagePermissionsLayoutBinding
import com.hover.stax.utils.Utils

class ManagePermissionsFragment : Fragment() {
	private var _binding: ManagePermissionsLayoutBinding? = null
	private val binding get() = _binding!!

	override fun onCreateView(inflater: LayoutInflater,
	                          container: ViewGroup?,
	                          savedInstanceState: Bundle?): View {
		_binding = ManagePermissionsLayoutBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		handleSwitchListeners()
	}

	override fun onResume() {
		super.onResume()
		updateSwitches()
	}

	private fun updateSwitches() {
		val ph = PermissionHelper(requireActivity())
		binding.callsPermissionSwitch.isChecked = ph.hasPhonePerm()
		binding.smsPermissionSwitch.isChecked = ph.hasSmsPerm()
		binding.displayPermissionSwitch.isChecked = ph.hasOverlayPerm()
		binding.accessibilityPermissionSwitch.isChecked = ph.hasAccessPerm()
	}

	private fun handleSwitchListeners() {
		val ph = PermissionHelper(requireActivity())
		binding.callsPermissionSwitch.setOnClickListener { if(binding.callsPermissionSwitch.isChecked) ph.requestPhone(requireActivity(), 1) else openAppDetailSettings() }
		binding.smsPermissionSwitch.setOnClickListener { if(binding.smsPermissionSwitch.isChecked) requestSMSPerms(requireActivity()) else openAppDetailSettings() }
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

	private fun requestSMSPerms(act: Activity?) =
		ActivityCompat.requestPermissions(act!!, arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS), 2)

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}