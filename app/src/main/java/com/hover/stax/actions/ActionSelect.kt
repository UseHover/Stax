package com.hover.stax.actions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.databinding.ActionSelectBinding
import com.hover.stax.utils.Constants.size55
import com.hover.stax.utils.UIHelper
import com.hover.stax.views.AbstractStatefulInput
import com.hover.stax.views.StaxDropdownLayout
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import timber.log.Timber


class ActionSelect(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs), RadioGroup.OnCheckedChangeListener, Target {

    private var dropdownLayout: StaxDropdownLayout? = null
    private var dropdownView: AutoCompleteTextView? = null
    private var radioHeader: TextView? = null
    private var isSelfRadio: RadioGroup? = null

    private var actions: List<HoverAction>? = null
    private var highlightedAction: HoverAction? = null
    private var highlightListener: HighlightListener? = null

    private var _binding: ActionSelectBinding? = null
    private val binding get() = _binding!!

    init {
        _binding = ActionSelectBinding.inflate(LayoutInflater.from(context), this, true)
        dropdownLayout = binding.actionDropdownInput
        dropdownView = binding.actionDropdownInput.autoCompleteTextView
        radioHeader = binding.header
        isSelfRadio = binding.isSelfRadioGroup

        visibility = GONE
    }

    fun updateActions(filteredActions: List<HoverAction>) {
        visibility = if (filteredActions.isNullOrEmpty()) GONE else VISIBLE
        if (filteredActions.isNullOrEmpty()) return

        actions = filteredActions
        highlightedAction = null

        val uniqueActions = sort(filteredActions)

        val actionDropdownAdapter = ActionDropdownAdapter(uniqueActions, context)
        dropdownView!!.apply {
            setAdapter(actionDropdownAdapter)
            dropDownHeight = UIHelper.dpToPx(300)
            setOnItemClickListener { parent, _, position, _ ->
                val action = parent.getItemAtPosition(position) as HoverAction
                selectRecipientNetwork(action)
             }
            dropdownLayout?.visibility = if (showRecipientNetwork(uniqueActions)) View.VISIBLE else View.GONE
            radioHeader!!.setText(if (actions!!.first().transaction_type == HoverAction.AIRTIME) R.string.airtime_who_header else R.string.send_who_header)
        }
    }

    fun sort(actions: List<HoverAction>): List<HoverAction> = actions.distinctBy { it.recipientInstitutionId() }.toList()

    private fun showRecipientNetwork(actions: List<HoverAction>) = actions.size > 1 || (actions.size == 1 && !actions.first().isOnNetwork)

    fun selectRecipientNetwork(action: HoverAction) {
        if (action == highlightedAction) return

        setDropdownValue(action)
        setRadioValuesIfRequired(action)
    }

    private fun setRadioValuesIfRequired(action: HoverAction) {
        dropdownLayout!!.setState(null, AbstractStatefulInput.SUCCESS)
        val options = getWhoMeOptions(action.recipientInstitutionId())

        if (options.size == 1) {
            if (!options.first().requiresRecipient())
                dropdownLayout!!.setState(context.getString(R.string.self_only_money_warning), AbstractStatefulInput.INFO)

            selectAction(action)

            isSelfRadio!!.visibility = GONE
            radioHeader!!.visibility = GONE
        } else createRadios(options)
    }

    private fun selectAction(action: HoverAction) {
        highlightedAction = action
        highlightListener?.highlightAction(action)
    }

    private fun setDropdownValue(action: HoverAction) {
        dropdownView!!.setText(action.toString(), false)
        UIHelper.loadPicasso(context.getString(R.string.root_url).plus(action.to_institution_logo), size55, this)
    }

    fun setState(message: String?, state: Int) = dropdownLayout?.setState(message, state)

    fun setListener(listener: HighlightListener) {
        highlightListener = listener
    }

    private fun getWhoMeOptions(recipientInstId: Int): List<HoverAction> {
        val options = ArrayList<HoverAction>()
        if (actions == null) return options

        for (action in actions!!) {
            if (action.recipientInstitutionId() == recipientInstId && !options.contains(action))
                options.add(action)
        }

        return options
    }

    private fun createRadios(actions: List<HoverAction>) {
        isSelfRadio!!.removeAllViews()
        isSelfRadio!!.clearCheck()

        if (!actions.isNullOrEmpty()) {
            actions.forEachIndexed { index, hoverAction ->
                val radioButton = (LayoutInflater.from(context).inflate(R.layout.stax_radio_button, null) as RadioButton).apply {
                    text = hoverAction.getPronoun(context)
                    id = index
                }
                isSelfRadio!!.addView(radioButton)
            }

            val radioVisibility = if (actions.size > 1) VISIBLE else GONE
            isSelfRadio!!.apply {
                setOnCheckedChangeListener(this@ActionSelect)
                check(if (highlightedAction != null) actions.indexOf(highlightedAction) else 0)
                visibility = radioVisibility
            }
            radioHeader?.visibility = radioVisibility
        }
    }

    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
        if (checkedId == -1 && actions != null) return
        selectAction(actions!![checkedId])
    }

    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
        val drawable = RoundedBitmapDrawableFactory.create(context.resources, bitmap).apply { isCircular = true }
        dropdownView?.setCompoundDrawablesRelativeWithIntrinsicBounds(drawable, null, null, null)
    }

    override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
        Timber.e(e?.localizedMessage)
    }

    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
        dropdownView?.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_grey_circle_small, 0, 0, 0)
    }

    interface HighlightListener {
        fun highlightAction(action: HoverAction?)
    }

}