package com.enecuum.wl.data.livedata

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.enecuum.lib.api.CoingeckoData

interface PriceLiveDataRepository {
    fun observeAmount(owner: LifecycleOwner, observer: Observer<CoingeckoData>)
    fun getAmount(): String
    fun setAmount(tokensBalance: CoingeckoData)
    fun clearObserver(owner: LifecycleOwner)
}

class PriceLiveData : MutableLiveData<CoingeckoData>(), PriceLiveDataRepository {

    override fun observeAmount(owner: LifecycleOwner, observer: Observer<CoingeckoData>) {
        observe(owner, observer)
    }

    override fun clearObserver(owner: LifecycleOwner) {
        removeObservers(owner)
    }

    override fun setAmount(price: CoingeckoData) {

        postValue(price)
        Log.d("Price change", "New price is ${price.usd}")
    }

    override fun getAmount(): String {
        return if (value != null) {
            value!!.usd ?: "..."
        } else {
            "..."
        }
    }
}