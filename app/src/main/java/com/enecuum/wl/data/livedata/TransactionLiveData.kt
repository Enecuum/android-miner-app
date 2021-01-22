package com.enecuum.wl.data.livedata

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.enecuum.lib.api.TransactionResponse

interface TransactionLiveDataRepository {
    fun observeTransaction(owner: LifecycleOwner, observer: Observer<TransactionResponse>)
    fun removeObserverTransaction(owner: LifecycleOwner)
    fun getTransaction(): TransactionResponse?
    fun setTransaction(response: TransactionResponse?)
}

class TransactionLiveData : MutableLiveData<TransactionResponse>(), TransactionLiveDataRepository {

    override fun observeTransaction(
        owner: LifecycleOwner,
        observer: Observer<TransactionResponse>
    ) {
        observe(owner, observer)
    }

    override fun removeObserverTransaction(owner: LifecycleOwner) {
        removeObservers(owner)
    }

    override fun setTransaction(response: TransactionResponse?) {
        postValue(response)
    }

    override fun getTransaction(): TransactionResponse? {
        return value
    }
}