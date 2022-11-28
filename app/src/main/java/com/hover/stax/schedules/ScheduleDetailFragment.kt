package com.hover.stax.schedules

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.hover.stax.R
import com.hover.stax.contacts.StaxContact
import com.hover.stax.databinding.FragmentScheduleBinding

import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.DateUtils
import com.hover.stax.utils.UIHelper
import com.hover.stax.utils.Utils
import com.hover.stax.views.Stax2LineItem
import com.hover.stax.views.StaxDialog
import org.json.JSONException
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel


class ScheduleDetailFragment : Fragment() {

    private val viewModel: ScheduleDetailViewModel by viewModel()
    private val args: ScheduleDetailFragmentArgs by navArgs()

    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!

    private var dialog: StaxDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        logAnalytics()
        _binding = FragmentScheduleBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(viewModel) {
            schedule.observe(viewLifecycleOwner) {
                it?.let {
                    setUpSummary(it)
                    setUpTestBtn(it)
                }
            }

            contacts.observe(viewLifecycleOwner) { contacts ->
                if (!contacts.isNullOrEmpty()) {
                    contacts.forEach { createRecipientEntry(it) }
                }
            }

            viewModel.setSchedule(requireArguments().getInt("id"))
        }
    }

    private fun logAnalytics() {
        val data = JSONObject()
        try {
            data.put("id", args.id)
        } catch (ignored: JSONException) {
        }

        AnalyticsUtil.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_schedule)), data, requireContext())
    }

    private fun setUpSummary(schedule: Schedule) {
        binding.scheduleDetailsCard.setTitle(schedule.description)

        with(binding.summaryCard) {
            detailsAmount.text = Utils.formatAmount(schedule.amount)
            detailsDate.text = DateUtils.humanFriendlyDateTime(schedule.start_date)

            frequencyRow.visibility = if (schedule.frequency == Schedule.ONCE) View.GONE else View.VISIBLE
            detailsFrequency.text = schedule.humanFrequency(context)

            endRow.visibility = if (schedule.frequency == Schedule.ONCE || schedule.end_date == null) View.GONE else View.VISIBLE
            detailsEnd.text = if (schedule.end_date != null) DateUtils.humanFriendlyDate(schedule.end_date) else ""

            noteRow.visibility = if (schedule.note.isNullOrEmpty()) View.GONE else View.VISIBLE
            detailsReason.text = schedule.note
        }

        binding.cancelBtn.setOnClickListener { showConfirmDialog() }
    }

    private fun createRecipientEntry(c: StaxContact) {
        val item = Stax2LineItem(requireActivity(), null).apply { setContact(c) }
        binding.summaryCard.requesteeValueList.addView(item)
    }

    private fun showConfirmDialog() {
        dialog = StaxDialog(requireActivity())
                .setDialogTitle(R.string.cancelfuture_head)
                .setDialogMessage(R.string.cancelfuture_msg)
                .setNegButton(R.string.btn_back) {}
                .setPosButton(R.string.btn_canceltrans) {
                    viewModel.deleteSchedule()
                    UIHelper.flashAndReportMessage(requireActivity(), getString(R.string.toast_confirm_cancelfuture))
                    findNavController().popBackStack()
                }
                .isDestructive
        dialog!!.showIt()
    }

    private fun setUpTestBtn(schedule: Schedule) {
        binding.testBtn.apply {
            visibility = if (Utils.usingDebugVariant(requireActivity())) View.VISIBLE else View.GONE
            setOnClickListener {
                WorkManager.getInstance(requireActivity()).beginUniqueWork("TEST", ExistingWorkPolicy.REPLACE,
                        ScheduleWorker.makeWork()).enqueue()

                if (!schedule.isScheduledForToday)
                    UIHelper.flashAndReportMessage(requireActivity(), "Shouldn't show notification; not scheduled for today")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

}