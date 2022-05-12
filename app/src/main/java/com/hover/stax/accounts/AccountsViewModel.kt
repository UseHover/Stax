package com.hover.stax.accounts

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.actions.ActionRepo
import com.hover.stax.schedules.Schedule
import com.hover.stax.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AccountsViewModel(application: Application, val repo: AccountRepo, val actionRepo: ActionRepo) : AndroidViewModel(application),
    AccountDropdown.HighlightListener {

    val accounts: LiveData<List<Account>> = repo.getAllLiveAccounts()
    val activeAccount: MediatorLiveData<Account> = MediatorLiveData()

    private var type = MutableLiveData<String>()
    val channelActions = MediatorLiveData<List<HoverAction>>()

    init {
        activeAccount.addSource(accounts, this@AccountsViewModel::setActiveAccountIfNull)

        channelActions.apply {
            addSource(type, this@AccountsViewModel::loadActions)
            addSource(activeAccount, this@AccountsViewModel::loadActions)
        }
    }

    fun setType(t: String) {
        type.value = t
    }

    private fun setActiveAccountIfNull(accounts: List<Account>) {
        if (!accounts.isNullOrEmpty() && activeAccount.value == null)
            activeAccount.postValue(accounts.firstOrNull { it.isDefault })
    }

    fun setActiveAccount(accountId: Int?) = accountId?.let { activeAccount.postValue(accounts.value?.find { it.id == accountId }) }

    fun getActionType(): String = type.value!!

    private fun loadActions(type: String?) {
        if (type == null || activeAccount.value == null) return

        if (accounts.value.isNullOrEmpty()) return
        loadActions(activeAccount.value!!, type)
    }

    private fun loadActions(account: Account?) {
        if (account == null || type.value.isNullOrEmpty()) return
        loadActions(account, type.value!!)
    }

    private fun loadActions(account: Account, t: String) = viewModelScope.launch(Dispatchers.IO) {
        channelActions.postValue(
            if (t == HoverAction.P2P) actionRepo.getTransferActions(account.channelId)
            else actionRepo.getActions(account.channelId, t))
    }

    fun errorCheck(): String? {
        return when {
            activeAccount.value == null -> (getApplication() as Context).getString(R.string.channels_error_noselect)
            channelActions.value.isNullOrEmpty() -> (getApplication() as Context).getString(
                R.string.no_actions_fielderror,
                HoverAction.getHumanFriendlyType(getApplication(), type.value)
            )
            !isValidAccount() -> (getApplication() as Context).getString(R.string.channels_error_newaccount)
            else -> null
        }
    }

    fun isValidAccount(): Boolean = activeAccount.value!!.name != PLACEHOLDER

    fun view(s: Schedule) {
        setType(s.type)
    }

    override fun highlightAccount(account: Account) {
        activeAccount.postValue(account)
    }

    fun reset() {
        activeAccount.value = accounts.value?.firstOrNull { it.isDefault }
    }

    fun setDefaultAccount(account: Account) {
        if (!accounts.value.isNullOrEmpty()) {
            val accts = accounts.value!!
            //remove current default account
            val current: Account? = accts.firstOrNull { it.isDefault }
            current?.isDefault = false
            repo.update(current)

            val a = accts.first { it.id == account.id }
            a.isDefault = true
            repo.update(a)
        }
    }
}