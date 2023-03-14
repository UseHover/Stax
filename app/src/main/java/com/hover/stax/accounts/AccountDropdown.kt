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
package com.hover.stax.accounts

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.domain.model.USSDAccount
import com.hover.stax.utils.UIHelper
import com.hover.stax.views.StaxDropdownLayout
import kotlinx.coroutines.launch

class AccountDropdown(context: Context, attributeSet: AttributeSet) : StaxDropdownLayout(context, attributeSet) {

    private var showSelected: Boolean = true
    private var highlightListener: HighlightListener? = null

    private var highlightedAccount: USSDAccount? = null

    val target = object : CustomTarget<Drawable>() {
        override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
            autoCompleteTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(resource, null, null, null)
        }

        override fun onLoadCleared(placeholder: Drawable?) {
            autoCompleteTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_grey_circle_small, 0, 0, 0)
        }
    }

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

    private fun accountUpdate(accounts: List<USSDAccount>) {
        if (accounts.isNotEmpty()) {
            updateChoices(accounts.toMutableList())
        } else if (!hasExistingContent()) {
            setState(context.getString(R.string.accounts_error_no_accounts), NONE)
        }
    }

    private fun setDropdownValue(account: USSDAccount?) {
        if (account != null) {
            UIHelper.loadImage(context, account.logoUrl, target)
            autoCompleteTextView.setText(account.userAlias, false)
            highlightedAccount = account
        } else { UIHelper.loadImage(context, null, target) }
    }

    private fun updateChoices(accounts: List<USSDAccount>) {
        if (highlightedAccount == null) setDropdownValue(null)
        val adapter = AccountDropdownAdapter(accounts, context)
        autoCompleteTextView.apply {
            setAdapter(adapter)
            setOnItemClickListener { parent, _, position, _ -> onSelect(parent.getItemAtPosition(position) as USSDAccount) }
        }

        if (accounts.firstOrNull()?.id != 0)
            onSelect(accounts.firstOrNull { it.isDefault })
    }

    private fun onSelect(account: USSDAccount?) {
        setDropdownValue(account)
        if (account != null && account.id != 0)
            highlightListener?.highlightAccount(account)
        else
            highlightListener?.addAccount()
    }

    private fun hasExistingContent(): Boolean = autoCompleteTextView.adapter != null && autoCompleteTextView.adapter.count > 0

    fun setObservers(viewModel: AccountsViewModel, lifecycleOwner: LifecycleOwner) {
        with(viewModel) {
            lifecycleOwner.lifecycleScope.launch {
                lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    accountList.collect {
                        accountUpdate(it.accounts.plus(USSDAccount("Add account")))
                    }
                }
            }

            activeAccount.observe(lifecycleOwner) {
                if (it != null && showSelected) {
                    setDropdownValue(it)
                    setState(helperText, NONE)
                }
            }

            institutionActions.observe(lifecycleOwner) {
                setState(it, viewModel)
            }
        }
    }

    private fun setState(actions: List<HoverAction>, viewModel: AccountsViewModel) {
        if (viewModel.activeAccount.value == null && actions.isEmpty()) {
            setState(
                context.getString(
                    R.string.no_actions_fielderror,
                    HoverAction.getHumanFriendlyType(context, viewModel.getActionType())
                ),
                ERROR
            )
        } else if (actions.isNotEmpty() && actions.size == 1)
            addInfoMessage(actions.first())
        else if (viewModel.activeAccount.value != null && showSelected)
            setState(null, SUCCESS)
    }

    private fun addInfoMessage(action: HoverAction) {
        if (!action.requiresRecipient() && isSelf(action))
            setState(
                context.getString(
                    if (action.transaction_type == HoverAction.AIRTIME) R.string.self_only_airtime_warning
                    else R.string.self_only_money_warning
                ),
                INFO
            )
        else
            setState(null, SUCCESS)
    }

    private fun isSelf(action: HoverAction): Boolean {
        return action.transaction_type == HoverAction.P2P || action.transaction_type == HoverAction.AIRTIME
    }

    fun getHighlightedAccount() = highlightedAccount

    interface HighlightListener {
        fun highlightAccount(account: USSDAccount)
        fun addAccount()
    }
}