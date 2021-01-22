package com.enecuum.wl.data.livedata

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.enecuum.lib.api.Statistics

interface StatisticLiveDataRepository {
    fun observeStatistic(owner: LifecycleOwner, observer: Observer<Statistics>)
    fun getStatistic(): Statistics
    fun setStatistic(statistic: Statistics)
}

class StatisticLiveData : MutableLiveData<Statistics>(), StatisticLiveDataRepository {

    override fun observeStatistic(owner: LifecycleOwner, observer: Observer<Statistics>) {
        observe(owner, observer)
    }

    override fun setStatistic(statistic: Statistics) {
        postValue(statistic)
    }

    override fun getStatistic(): Statistics {
        return value!!
    }

}