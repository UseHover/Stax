package com.hover.stax.accounts

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import com.hover.sdk.actions.HoverAction
import com.hover.stax.R
import com.hover.stax.data.local.accounts.AccountRepo
import com.hover.stax.data.local.actions.ActionRepo
import com.hover.stax.domain.model.Account
import com.hover.stax.schedules.Schedule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class AccountsViewModel(application: Application, val repo: AccountRepo, val actionRepo: ActionRepo) : AndroidViewModel(application),
    AccountDropdown.HighlightListener {

    private val _accounts = MutableStateFlow(AccountList())
    val accountList = _accounts.asStateFlow()

    val activeAccount = MutableLiveData<Account?>()

    private var type = MutableLiveData<String>()
    val institutionActions = MediatorLiveData<List<HoverAction>>()
    val bonusActions = MediatorLiveData<List<HoverAction>>()

    private val _defaultUpdateMsg = Channel<String>()
    val defaultUpdateMsg = _defaultUpdateMsg.receiveAsFlow()

    init {
        fetchAccounts()

        institutionActions.apply {
            addSource(activeAccount, this@AccountsViewModel::loadActions)
            addSource(type, this@AccountsViewModel::loadActions)
        }
        bonusActions.addSource(activeAccount, this@AccountsViewModel::loadBonuses)
    }

    private fun fetchAccounts() = viewModelScope.launch {
        repo.getAccounts().collect { a ->
            _accounts.update { it.copy(accounts = a) }

            setActiveAccountIfNull(a)
        }
    }

    fun setType(t: String) {
        type.value = t
    }

    private fun setActiveAccountIfNull(accounts: List<Account>) {
        if (accounts.isNotEmpty() && activeAccount.value == null)
            activeAccount.value = accounts.firstOrNull { it.isDefault }
    }

    fun getActionType(): String? = type.value

    private fun loadActions(type: String?) {
        if (type == null || activeAccount.value == null) return

        if (accountList.value.accounts.isEmpty()) return

        loadActions(activeAccount.value!!, type)
    }

    private fun loadActions(account: Account?) {
        if (account == null || type.value.isNullOrEmpty()) return

        loadActions(account, type.value!!)
    }

    private fun loadActions(account: Account, type: String) = viewModelScope.launch(Dispatchers.IO) {
        institutionActions.postValue(
            if (type == HoverAction.P2P) actionRepo.getTransferActions(account.institutionId!!, account.countryAlpha2!!)
            else actionRepo.getActions(account.institutionId!!, account.countryAlpha2!!, type)
        )
    }

    private fun loadBonuses(account: Account?) {
        if (account?.countryAlpha2 == null || type.value.isNullOrEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            bonusActions.postValue(
                actionRepo.getBonusActionsByCountryAndType(account.countryAlpha2!!, type.value!!)
            )
        }
    }

    fun setActiveAccount(accountId: Int?) = accountId?.let { activeAccount.postValue(accountList.value.accounts.find { it.id == accountId }) }

    fun payWith(fromInstitutionId: Int) = accountList.value.accounts.firstOrNull { it.institutionId == fromInstitutionId }?.let { activeAccount.postValue(it) }

    fun errorCheck(): String? {
        return when {
            activeAccount.value == null -> (getApplication() as Context).getString(R.string.channels_error_noselect)
            institutionActions.value.isNullOrEmpty() -> (getApplication() as Context).getString(
                R.string.no_actions_fielderror,
                HoverAction.getHumanFriendlyType(getApplication(), type.value)
            )
            !isValidAccount() -> (getApplication() as Context).getString(R.string.channels_error_newaccount)
            else -> null
        }
    }

    fun isValidAccount(): Boolean = activeAccount.value?.latestBalance != null || activeAccount.value?.institutionType != "bank"

    fun view(s: Schedule) {
        setType(s.type)
    }

    fun reset() {
        activeAccount.value = accountList.value.accounts.firstOrNull { it.isDefault }
    }

    fun setDefaultAccount(account: Account) = viewModelScope.launch(Dispatchers.IO) {
        if (accountList.value.accounts.isNotEmpty()) {
            val accts = accountList.value.accounts
            //remove current default account
            val current: Account? = accts.firstOrNull { it.isDefault }

            if (account.id == current?.id) return@launch

            current?.isDefault = false
            repo.update(current)

            val a = accts.first { it.id == account.id }
            a.isDefault = true
            repo.update(a)

            _defaultUpdateMsg.send((getApplication() as Context).getString(R.string.def_account_update_msg, account.userAlias))
        }
    }

    override fun highlightAccount(account: Account) {
        activeAccount.postValue(account)
    }

}

data class AccountList(val accounts: List<Account> = emptyList())