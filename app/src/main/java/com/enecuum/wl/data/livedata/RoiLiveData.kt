package com.enecuum.wl.data.livedata

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.enecuum.lib.api.Roi

interface RoiLiveDataRepository {
    fun observeRoi(owner: LifecycleOwner, observer: Observer<List<Roi>>)
    fun getRoi(): List<Roi>
    fun setRoi(roi: List<Roi>)
}

class RoiLiveData : MutableLiveData<List<Roi>>(), RoiLiveDataRepository {
    override fun observeRoi(owner: LifecycleOwner, observer: Observer<List<Roi>>) {
        observe(owner, observer)
    }

    override fun setRoi(roi: List<Roi>) {
        postValue(roi)
    }

    override fun getRoi(): List<Roi> {
        return value!!
    }
}