package com.hover.stax.accounts

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.actions.ActionSelect
import com.hover.stax.utils.UIHelper
import com.hover.stax.views.StaxDropdownLayout
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber


class AccountDropdown(context: Context, attributeSet: AttributeSet) : StaxDropdownLayout(context, attributeSet) {

    private var showSelected: Boolean = true
    private var highlightListener: HighlightListener? = null

    private var highlightedAccount: Account? = null

    init {
        getAttrs(context, attributeSet)
    }

    override fun getAttrs(context: Context, attrs: AttributeSet) {
        super.getAttrs(context, attrs)
        val attributes = context.theme.obtainStyledAttributes(attrs, R.styleable.ChannelDropdown, 0, 0)

        try {
            showSelected = attributes.getBoolean(R.styleable.ChannelDropdown_show_selected, true)
            helperText = attributes.getString(R.styleable.ChannelDropdown_initial_helper_text)
        } finally {
            attributes.recycle()
        }
    }

    fun setListener(listener: HighlightListener) {
        highlightListener = listener
    }

    private fun accountUpdate(accounts: List<Account>) {
        if (accounts.isNotEmpty()) {
            updateChoices(accounts.toMutableList())
        } else if (!hasExistingContent()) {
            setState(context.getString(R.string.accounts_error_no_accounts), NONE)
        }
    }

    private fun setDropdownValue(account: Account?) {
        account?.let {
            autoCompleteTextView.setText(it.alias, false)

            val target = object : CustomTarget<Drawable>() {
                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                    autoCompleteTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(resource, null, null, null)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    autoCompleteTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_grey_circle_small, 0, 0, 0)
                }
            }

            UIHelper.loadImage(context, account.logoUrl, target)
            highlightedAccount = account
        }
    }

    private fun updateChoices(accounts: MutableList<Account>) {
        if (highlightedAccount == null) setDropdownValue(null)
        accounts.add(Account("Add account"))
        val adapter = AccountDropdownAdapter(accounts, context)
        autoCompleteTextView.apply {
            setAdapter(adapter)
            setOnItemClickListener { parent, _, position, _ -> onSelect(parent.getItemAtPosition(position) as Account) }
        }
        onSelect(accounts.firstOrNull { it.isDefault })
    }


    private fun onSelect(account: Account?) {
        setDropdownValue(account)
        if (account != null && account.id != 0)
            highlightListener?.highlightAccount(account)
        else
            findNavController().navigate(R.id.navigation_linkAccount)
    }

    private fun hasExistingContent(): Boolean = autoCompleteTextView.adapter != null && autoCompleteTextView.adapter.count > 0

    fun setObservers(viewModel: AccountsViewModel, lifecycleOwner: LifecycleOwner) {
        with(viewModel) {
            lifecycleOwner.lifecycleScope.launch {
                lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                    accounts.collect {
                        accountUpdate(it)
                    }
                }
            }

            activeAccount.observe(lifecycleOwner) {
                if (it != null && showSelected) {
                    setDropdownValue(it)
                    setState(helperText, NONE)
                }
            }

            channelActions.observe(lifecycleOwner) {
                setState(it, viewModel)
            }
        }
    }

    private fun setState(actions: List<HoverAction>, viewModel: AccountsViewModel) {
        when {
            viewModel.activeAccount.value != null && (actions.isEmpty()) -> setState(
                context.getString(
                    R.string.no_actions_fielderror,
                    HoverAction.getHumanFriendlyType(context, viewModel.getActionType())
                ), ERROR
            )

            actions.isNotEmpty() && actions.size == 1 && !actions.first().requiresRecipient() && viewModel.getActionType() != HoverAction.BALANCE ->
                setState(
                    context.getString(
                        if (actions.first().transaction_type == HoverAction.AIRTIME) R.string.self_only_airtime_warning
                        else R.string.self_only_money_warning
                    ), INFO
                )

            viewModel.activeAccount.value != null && showSelected -> setState(null, SUCCESS)
        }
    }

    fun getHighlightedAccount() = highlightedAccount

    interface HighlightListener {
        fun highlightAccount(account: Account)
    }
}