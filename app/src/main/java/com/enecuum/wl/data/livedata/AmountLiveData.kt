package com.enecuum.wl.data.livedata

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.enecuum.wl.BuildConfig
import com.enecuum.lib.api.TokenBalance

interface AmountLiveDataRepository {
    fun observeAmount(owner: LifecycleOwner, observer: Observer<TokenBalance>)
    fun getAmount(): String
    fun setAmount(tokensBalance: List<TokenBalance>)
    fun clearObserver(owner: LifecycleOwner)
}

class AmountLiveData : MutableLiveData<TokenBalance>(), AmountLiveDataRepository {

    override fun observeAmount(owner: LifecycleOwner, observer: Observer<TokenBalance>) {
        observe(owner, observer)
    }

    override fun clearObserver(owner: LifecycleOwner) {
        removeObservers(owner)
    }

    override fun setAmount(tokensBalance: List<TokenBalance>) {

        val tokenBalance = tokensBalance.find {
            it.token == BuildConfig.TOKEN
        }

        postValue(tokenBalance)
        Log.d("Balance change", "New amount is ${tokenBalance?.amount}")
    }

    override fun getAmount(): String {
        return if (value != null) {
            value!!.amount.toString() ?: "..."
        } else {
            "..."
        }
    }
}