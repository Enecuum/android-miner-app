package com.enecuum.wl.data.livedata

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.enecuum.lib.api.Ticker

interface TickersLiveDataRepository {
    fun observeTickers(owner: LifecycleOwner, observer: Observer<List<Ticker>?>)
    fun setTickers(tickers: List<Ticker>?)
    fun getTickers(): List<Ticker>
    fun clearObserver(owner: LifecycleOwner)
}

class TickersLiveData : MutableLiveData<List<Ticker>>(), TickersLiveDataRepository {
    override fun observeTickers(owner: LifecycleOwner, observer: Observer<List<Ticker>?>) {
        observe(owner, observer)
    }

    override fun setTickers(tickers: List<Ticker>?) {
        postValue(tickers)
    }

    override fun getTickers(): List<Ticker> {
        return value!!
    }

    override fun clearObserver(owner: LifecycleOwner) {
        removeObservers(owner)
    }
}