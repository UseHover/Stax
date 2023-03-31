package com.hover.stax.accounts

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.hover.stax.R
import com.hover.stax.addAccounts.UsdcViewModel
import com.hover.stax.databinding.FragmentUsdcAccountBinding
import com.hover.stax.domain.model.CRYPTO_TYPE
import com.hover.stax.hover.AbstractBalanceCheckerFragment
import com.hover.stax.presentation.accounts.UsdcAccountScreen
import com.hover.stax.presentation.home.BalancesViewModel
import com.hover.stax.utils.AnalyticsUtil
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class USDCAccountDetailFragment : AbstractBalanceCheckerFragment() {

    private val viewModel: AccountDetailViewModel by sharedViewModel()
    private val balancesViewModel: BalancesViewModel by sharedViewModel()
    private val usdcViewModel: UsdcViewModel by viewModel()

    private var _binding: FragmentUsdcAccountBinding? = null
    private val binding get() = _binding!!

    private val args: USDCAccountDetailFragmentArgs by navArgs()
    private var download: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUsdcAccountBinding.inflate(inflater, container, false)
        observe()
        return binding.root
    }

    private fun observe() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    usdcViewModel.downloadEvent.collect {
                        download = it
                        chooseFileLocation()
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AnalyticsUtil.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_channel)), requireActivity())

        binding.root.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        binding.root.setContent {
            UsdcAccountScreen(viewModel, usdcViewModel, findNavController())
        }

        viewModel.setAccount(args.accountId, CRYPTO_TYPE)
    }

    private fun chooseFileLocation() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_TITLE, "usdcSecretKey_backup.txt")
        }
        startActivityForResult(intent, 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0) {
            data?.data?.let {
                try {
                    val outputStream = requireActivity().contentResolver.openOutputStream(it) ?: return
                    Timber.e("Secret should be ${usdcViewModel.secret.value}")
                    outputStream.write(download!!.toByteArray(charset("UTF-8")))
                    outputStream.close()
                    Toast.makeText(requireContext(), R.string.key_downloaded, Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), R.string.key_download_error, Toast.LENGTH_LONG).show()
                    Timber.e("Something went wrong", e)
                }
            }
        }
    }
}