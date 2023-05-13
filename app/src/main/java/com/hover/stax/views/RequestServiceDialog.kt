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
package com.hover.stax.views

import android.app.Activity
import android.content.Context
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.hover.stax.R
import com.hover.stax.core.AnalyticsUtil
import org.json.JSONObject

open class RequestServiceDialog(private var ctx: Context, private var mView: View) : AlertDialog(ctx) {

    var dialog: AlertDialog? = null
    private var institutionValue: String = ""
    private var shortCodeValue: String = ""

    private lateinit var institutionInput: StaxTextInput
    private lateinit var shortCodeInput: StaxTextInput
    private lateinit var activity: Activity

    constructor(a: Activity) : this(a, a.layoutInflater, R.layout.request_for_service) {
        activity = a
    }
    private constructor(c: Context, inflater: LayoutInflater, layoutRes: Int) : this(c, inflater.inflate(layoutRes, null))

    val view get() = mView

    private fun setupViews() {
        initViews()
        setTitle()
        setInputWatchers()
        setPosButton()
        setNegButton()
    }

    private fun initViews() {
        institutionInput = mView.findViewById(R.id.institutionNameInput)
        shortCodeInput = mView.findViewById(R.id.shortCodeInput)
    }
    private fun setInputWatchers() {
        institutionInput.addTextChangedListener(institutionInputWatcher)
        shortCodeInput.addTextChangedListener(shortCodeInputWatcher)
    }

    private fun setTitle() {
        mView.findViewById<LinearLayout>(R.id.transaction_header)?.let { it.visibility = View.VISIBLE }
        mView.findViewById<View?>(R.id.title)
            ?.let { (it as TextView).text = ctx.getString(R.string.inform_stax_desc) }
    }

    private fun setNegButton() {
        val negBtn = mView.findViewById<View>(R.id.neg_btn)
        negBtn.visibility = View.VISIBLE
        negBtn.setOnClickListener { dialog?.dismiss() }
    }

    private fun setPosButton() {
        mView.findViewById<View>(R.id.pos_btn).setOnClickListener {
            val data = JSONObject()
            data.put("institutionName", institutionValue)
            data.put("shortCode", shortCodeValue)

            if (validates()) {
                com.hover.stax.core.AnalyticsUtil.logAnalyticsEvent(ctx.getString(R.string.requested_new_channel), data, ctx)
                dialog?.dismiss()
                showSuccessDialog()
            }
        }
    }

    private fun validates(): Boolean {
        var status = true
        if (TextUtils.getTrimmedLength(institutionValue) < MIN_ENTRY_LENGTH) {
            institutionInput.setState(ctx.getString(R.string.enter_financial_inst_err), AbstractStatefulInput.ERROR)
            status = false
        } else institutionInput.setState(null, AbstractStatefulInput.SUCCESS)
        if (TextUtils.getTrimmedLength(shortCodeValue) < MIN_ENTRY_LENGTH) {
            shortCodeInput.setState(ctx.getString(R.string.enter_shortcode_err), AbstractStatefulInput.ERROR)
            status = false
        } else shortCodeInput.setState(null, AbstractStatefulInput.SUCCESS)
        return status
    }

    private fun showSuccessDialog() {
        StaxDialog(activity)
            .setDialogTitle(ctx.getString(R.string.thank_you))
            .setDialogMessage(ctx.getString(R.string.service_request_success_desc))
            .setPosButton(R.string.btn_ok, null)
            .showIt()
    }

    private val institutionInputWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun afterTextChanged(editable: Editable) {}
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            institutionValue = charSequence.toString()
        }
    }

    private val shortCodeInputWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun afterTextChanged(editable: Editable) {}
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            shortCodeValue = charSequence.toString()
        }
    }

    private fun createIt(): AlertDialog {
        setupViews()
        return Builder(context, R.style.StaxDialog).setView(mView).create()
    }

    fun showIt(): AlertDialog? {
        if (dialog == null) dialog = createIt()
        dialog!!.show()
        return dialog
    }
    companion object {
        const val MIN_ENTRY_LENGTH = 3
    }
}