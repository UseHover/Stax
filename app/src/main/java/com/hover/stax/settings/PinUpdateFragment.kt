package com.hover.stax.settings

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.textfield.TextInputEditText
import com.hover.stax.R
import com.hover.stax.channels.Channel
import com.hover.stax.databinding.FragmentPinUpdateBinding
import com.hover.stax.utils.UIHelper
import com.hover.stax.utils.Utils.logAnalyticsEvent
import com.hover.stax.views.StaxDialog
import com.squareup.picasso.Picasso
import com.squareup.picasso.Picasso.LoadedFrom
import com.squareup.picasso.Target
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class PinUpdateFragment : Fragment(), Target {

    private var input: TextInputEditText? = null
    private val pinViewModel: PinsViewModel by viewModel()
    private var _binding: FragmentPinUpdateBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_change_pin)), requireContext())

        _binding = FragmentPinUpdateBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pinViewModel.getSelectedChannels().observe(viewLifecycleOwner, { Timber.e("Observer ensures events fire.") })

        arguments?.getInt("channel_id", 0)?.let {
            pinViewModel.loadChannel(it)
        }

        pinViewModel.channel.observe(viewLifecycleOwner, { initView(it) })
        input = binding.pinInput
        binding.editBtn.setOnClickListener { showChoiceCard(false) }
        binding.cancelBtn.setOnClickListener { showChoiceCard(true) }
    }

    private fun initView(c: Channel?) {
        if (c == null) {
            return
        }

        binding.choiceCard.setTitle(c.name)
        binding.editCard.setTitle(c.name)

//        c.logoUrl?.let { Picasso.get().load(c.logoUrl).into(this) }

        if (c.pin != null && c.pin.isNotEmpty()) input?.setText(KeyStoreExecutor.decrypt(c.pin, context))
        setupSavePin(c)
        setUpRemoveAccount(c)
    }

    private fun setupSavePin(channel: Channel) {
        binding.saveBtn.setOnClickListener {
            if (input!!.text != null) {
                channel.pin = input!!.text.toString()
                pinViewModel.savePin(channel, requireActivity())
            }
            UIHelper.flashMessage(requireActivity(), resources.getString(R.string.toast_confirm_pinupdate))
            showChoiceCard(true)
        }
    }

    private fun setUpRemoveAccount(channel: Channel) {
        binding.removeAcct.setOnClickListener {
            StaxDialog(requireActivity())
                .setDialogTitle(getString(R.string.removepin_dialoghead, channel.name))
                .setDialogMessage(R.string.removepins_dialogmes)
                .setPosButton(R.string.btn_removeaccount) { btn: View? -> removeAccount(channel) }
                .setNegButton(R.string.btn_cancel, null)
                .isDestructive
                .showIt()
        }
    }

    private fun removeAccount(channel: Channel) {
        pinViewModel.removeChannel(channel)
        NavHostFragment.findNavController(this).popBackStack()
        UIHelper.flashMessage(requireActivity(), resources.getString(R.string.toast_confirm_acctremoved))
    }

    private fun showChoiceCard(show: Boolean) {
        binding.choiceCard.visibility = if (show) View.VISIBLE else View.GONE
        binding.editCard.visibility = if (show) View.GONE else View.VISIBLE
        if (!show) input!!.requestFocus()
    }

    override fun onBitmapLoaded(bitmap: Bitmap, from: LoadedFrom) {
        val b = Bitmap.createScaledBitmap(bitmap, UIHelper.dpToPx(34), UIHelper.dpToPx(34), true)
        //wait for binding to happen when fragment is resumed before setting the image
        val d = RoundedBitmapDrawableFactory.create(binding.root.context.resources, b)
        d.isCircular = true
        input?.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null)
    }

    override fun onBitmapFailed(e: Exception, errorDrawable: Drawable) {}
    override fun onPrepareLoad(placeHolderDrawable: Drawable) {}

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}