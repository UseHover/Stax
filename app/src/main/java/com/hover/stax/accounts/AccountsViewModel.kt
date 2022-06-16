package com.hover.stax.accounts

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.actions.ActionRepo
import com.hover.stax.data.local.accounts.AccountRepo
import com.hover.stax.data.local.bonus.BonusRepo
import com.hover.stax.schedules.Schedule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class AccountsViewModel(application: Application, val repo: AccountRepo, val actionRepo: ActionRepo, private val bonusRepo: BonusRepo) : AndroidViewModel(application),
    AccountDropdown.HighlightListener {

    private val _accounts = MutableStateFlow<List<Account>>(emptyList())
    val accounts: StateFlow<List<Account>> = _accounts
    val activeAccount = MutableLiveData<Account>()

    private var type = MutableLiveData<String>()
    val channelActions = MediatorLiveData<List<HoverAction>>()

    init {
        fetchAccounts()

        channelActions.apply {
            addSource(activeAccount, this@AccountsViewModel::loadActions)
            addSource(type, this@AccountsViewModel::loadActions)
        }
    }

    private fun fetchAccounts() = viewModelScope.launch {
        repo.getAccounts().collect {
            _accounts.value = it

            setActiveAccountIfNull(it)
        }
    }

    fun setType(t: String) {
        type.value = t
    }

    private fun setActiveAccountIfNull(accounts: List<Account>) {
        if (accounts.isNotEmpty() && activeAccount.value == null)
            activeAccount.value = accounts.firstOrNull { it.isDefault }
    }

    fun getActionType(): String = type.value!!

    private fun loadActions(type: String?) {
        if (type == null || activeAccount.value == null) return

        if (accounts.value.isEmpty()) return
        loadActions(activeAccount.value!!, type)
    }

    private fun loadActions(account: Account?) {
        if (account == null || type.value.isNullOrEmpty()) return

        if (type.value == HoverAction.AIRTIME)
            checkForBonus(account)
        else
            loadActions(account, type.value!!)
    }

    private fun loadActions(account: Account, t: String) = viewModelScope.launch(Dispatchers.IO) {
        channelActions.postValue(
            if (t == HoverAction.P2P) actionRepo.getTransferActions(account.channelId)
            else actionRepo.getActions(account.channelId, t)
        )
    }

    private fun loadActions(channelId: Int, t: String = HoverAction.AIRTIME) = viewModelScope.launch(Dispatchers.IO) {
        val actions = actionRepo.getActions(channelId, t)
        channelActions.postValue(actions)
    }

    private fun checkForBonus(account: Account) = viewModelScope.launch(Dispatchers.IO) {
        val bonus = bonusRepo.getBonusByUserChannel(account.channelId)

        if (bonus != null)
            loadActions(bonus.purchaseChannel)
        else
            loadActions(account, type.value!!)
    }

    fun setActiveAccount(accountId: Int?) = accountId?.let { activeAccount.postValue(accounts.value.find { it.id == accountId }) }

    fun setActiveAccountFromChannel(userChannelId: Int) = viewModelScope.launch {
        repo.getAccounts().collect { accounts ->
            activeAccount.postValue(accounts.firstOrNull { it.channelId == userChannelId })
        }
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

    fun reset() {
        activeAccount.value = accounts.value.firstOrNull { it.isDefault }
    }

    fun setDefaultAccount(account: Account) {
        if (accounts.value.isNotEmpty()) {
            val accts = accounts.value
            //remove current default account
            val current: Account? = accts.firstOrNull { it.isDefault }
            current?.isDefault = false
            repo.update(current)

            val a = accts.first { it.id == account.id }
            a.isDefault = true
            repo.update(a)
        }
    }

    override fun highlightAccount(account: Account) {
        activeAccount.postValue(account)
    }

}