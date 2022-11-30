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
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.hover.stax.R

open class StaxDialog(var ctx: Context, var mView: View) : AlertDialog(ctx) {

    var dialog: AlertDialog? = null

    @JvmField
    protected var customNegListener: View.OnClickListener? = null

    @JvmField
    protected var customPosListener: View.OnClickListener? = null

    constructor(a: Activity) : this(a, a.layoutInflater, R.layout.stax_dialog)
    constructor(a: Activity, layoutRes: Int) : this(a, a.layoutInflater, layoutRes)
    constructor(inflater: LayoutInflater) : this(inflater.context, inflater, R.layout.stax_dialog)
    private constructor(c: Context, inflater: LayoutInflater, layoutRes: Int) : this(c, inflater.inflate(layoutRes, null))

    val view get() = mView

    fun setDialogIcon(iconRes: Int): StaxDialog {
        if (iconRes > 0) {
            val title: TextView = mView.findViewById(R.id.title)
            title.setCompoundDrawablesWithIntrinsicBounds(iconRes, 0, 0, 0)
        }
        return this
    }
    fun setDialogTitle(title: Int): StaxDialog {
        if (title == 0) setDialogMessage("") else setDialogTitle(ctx.getString(title))
        return this
    }

    fun setDialogTitle(title: String?): StaxDialog {
        mView.findViewById<LinearLayout>(R.id.transaction_header)?.let { it.visibility = View.VISIBLE }
        mView.findViewById<View?>(R.id.title)?.let { (it as TextView).text = title }

        return this
    }

    fun setPath(pathStringRes: Int): StaxDialog {
        mView.findViewById<TextView>(R.id.path_text)?.let {
            it.visibility = View.VISIBLE
            it.text = ctx.getString(pathStringRes)
        }
        return this
    }

    fun setDialogMessage(message: Int): StaxDialog {
        setDialogMessage(context.getString(message))
        return this
    }

    fun setDialogMessage(message: String?): StaxDialog {
        mView.findViewById<TextView>(R.id.message)?.let {
            it.visibility = View.VISIBLE
            it.text = message
        }
        return this
    }

    fun setHelperIcon(iconRes: Int): StaxDialog {
        mView.findViewById<TextView>(R.id.perm_message)?.let {
            it.setCompoundDrawablesWithIntrinsicBounds(iconRes, 0, 0, 0)
        }
        return this
    }

    fun setPosButton(label: Int, listener: View.OnClickListener?): StaxDialog {
        (mView.findViewById<View>(R.id.pos_btn) as AppCompatButton).text = context.getString(label)
        customPosListener = listener
        mView.findViewById<View>(R.id.pos_btn).setOnClickListener(posListener)
        return this
    }

    fun setNegButton(label: Int, listener: View.OnClickListener?): StaxDialog {
        val negBtn = mView.findViewById<View>(R.id.neg_btn)
        negBtn.visibility = View.VISIBLE
        (negBtn as AppCompatButton).text = context.getString(label)
        customNegListener = listener
        negBtn.setOnClickListener(negListener)

        return this
    }

    val isDestructive: StaxDialog
        get() {
            mView.findViewById<View>(R.id.pos_btn).background.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(context, R.color.stax_state_red), PorterDuff.Mode.SRC)
            return this
        }

    fun highlightPos(): StaxDialog {
        (mView.findViewById<View>(R.id.pos_btn) as Button).setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
        mView.findViewById<View>(R.id.pos_btn).background.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(context, R.color.brightBlue), PorterDuff.Mode.SRC)
        return this
    }

    fun createIt(): AlertDialog {
        return Builder(context, R.style.StaxDialog).setView(mView).create()
    }

    fun showIt(): AlertDialog? {
        if (dialog == null) dialog = createIt()
        dialog!!.show()
        return dialog
    }

    fun makeSticky(): StaxDialog {
        if (dialog == null) dialog = createIt()
        dialog!!.setCancelable(false)
        dialog!!.setCanceledOnTouchOutside(false)
        return this
    }

    private val negListener = View.OnClickListener { view: View? ->
        customNegListener?.onClick(view)
        dialog?.dismiss()
    }
    private val posListener = View.OnClickListener { view: View? ->
        customPosListener?.onClick(view)
        dialog?.dismiss()
    }
}