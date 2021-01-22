package com.enecuum.wl.vvm.statistic

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.enecuum.wl.data.livedata.StatisticLiveDataRepository
import com.enecuum.lib.api.Statistics
import com.enecuum.lib.api.main.Api
import com.enecuum.lib.api.main.ApiRouter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException

class StatisticViewModel(
    private val api: Api,
    private val repo: StatisticLiveDataRepository
) : ViewModel() {

    fun observeStatistic(owner: LifecycleOwner, observer: Observer<Statistics>) {
        repo.observeStatistic(owner, observer)
        getStatistic()
    }

    private fun getStatistic() {
        CoroutineScope(Dispatchers.IO).launch {
            val request = api.getStatsAsync(ApiRouter.Route.STATS.url)
            try {
                val response = request.await()
                repo.setStatistic(response)
            } catch (e: HttpException) {
                e.printStackTrace()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

}