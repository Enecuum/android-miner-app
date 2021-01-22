package com.enecuum.wl.data.livedata

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.enecuum.lib.api.Transactions

interface TransactionsLiveDataRepository {
    fun observeTransactions(owner: LifecycleOwner, observer: Observer<Transactions>)
    fun removeObserverTransactions(owner: LifecycleOwner)
    fun getTransactions(): Transactions
    fun setTransactions(response: Transactions)
}

class TransactionsLiveData : MutableLiveData<Transactions>(), TransactionsLiveDataRepository {

    override fun observeTransactions(
        owner: LifecycleOwner,
        observer: Observer<Transactions>
    ) {
        observe(owner, observer)
    }

    override fun removeObserverTransactions(owner: LifecycleOwner) {
        removeObservers(owner)
    }

    override fun setTransactions(response: Transactions) {
        postValue(response)
    }

    override fun getTransactions(): Transactions {
        return value!!
    }
}