package com.hover.stax.account

import android.app.Application
import androidx.lifecycle.*
import com.hover.stax.R
import com.hover.stax.database.DatabaseRepo
import com.hover.stax.transactions.StaxTransaction
import kotlinx.coroutines.launch
import java.util.*


class AccountDetailViewModel(val application: Application, val repo: DatabaseRepo) : ViewModel() {

    private val id = MutableLiveData<Int>()
    var account: LiveData<Account> = MutableLiveData()
    var transactions: LiveData<List<StaxTransaction>> = MutableLiveData()
    var spentThisMonth: LiveData<Double> = MutableLiveData()
    var feesThisYear: LiveData<Double> = MutableLiveData()

    private var newAccountName = MutableLiveData<String>()

    private val calendar = Calendar.getInstance()

    init {
        account = Transformations.switchMap(id, repo::getLiveAccount)
        transactions = Transformations.switchMap(id, repo::getAllTransferTransactions)
        spentThisMonth = Transformations.switchMap(id, this::loadSpentThisMonth)
        feesThisYear = Transformations.switchMap(id, this::loadFeesThisYear)
    }

    fun setNewAccountName(name: String) {
        newAccountName.value = name
    }

    fun setAccount(accountId: Int) = id.postValue(accountId)

    private fun loadSpentThisMonth(id: Int): LiveData<Double>? =
            repo.getSpentAmount(id, calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR))

    private fun loadFeesThisYear(id: Int): LiveData<Double>? = repo.getFees(id, calendar.get(Calendar.YEAR))

    fun newNameError(): String? = if (newAccountName.value.isNullOrEmpty()) application.getString(R.string.account_name_error) else null

    fun updateAccountName() {
        viewModelScope.launch {
            val a = account.value!!
            a.alias = newAccountName.value!!
            repo.update(a)
        }
    }
}