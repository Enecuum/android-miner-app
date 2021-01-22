package com.enecuum.wl.vvm.roi

import android.content.Context
import androidx.lifecycle.*
import com.enecuum.wl.BuildConfig
import com.enecuum.wl.data.livedata.AmountLiveDataRepository
import com.enecuum.wl.data.livedata.PriceLiveDataRepository
import com.enecuum.wl.data.livedata.RoiLiveDataRepository
import com.enecuum.lib.KeyStore
import com.enecuum.lib.api.CoingeckoData
import com.enecuum.lib.api.Roi
import com.enecuum.lib.api.Statistics
import com.enecuum.lib.api.TokenBalance
import com.enecuum.lib.api.main.Api
import com.enecuum.lib.api.main.ApiRouter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class RoiViewModel(
    protected val context: Context,
    private val api: Api,
    private val roiRepo: RoiLiveDataRepository,
    private val balanceRepo: AmountLiveDataRepository,
    private val priceRepo: PriceLiveDataRepository
) : ViewModel() {

    private val mutableStatistic = MutableLiveData<Statistics>()
    val statistic: LiveData<Statistics> = mutableStatistic

    private val mutablePrice = MutableLiveData<CoingeckoData>()
    val price: LiveData<CoingeckoData> = mutablePrice

    fun observeRoi(owner: LifecycleOwner, observer: Observer<List<Roi>>) {
        roiRepo.observeRoi(owner, observer)
        getRoi()
    }

    fun observeBalance(owner: LifecycleOwner, observer: Observer<TokenBalance>) {
        balanceRepo.observeAmount(owner, observer)
        getBalance()
    }

    fun observePrice(owner: LifecycleOwner, observer: Observer<CoingeckoData>) {
        priceRepo.observeAmount(owner, observer)
        getTokenPrice()
    }

    private fun getRoi() {
        CoroutineScope(Dispatchers.IO).launch {
            val request = api.getRoiAsync("https://pulse.enecuum.com/api/v1/roi", BuildConfig.TOKEN)
            try {
                val response = request.await()
                roiRepo.setRoi(response)
            } catch (e: HttpException) {
                e.printStackTrace()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    private fun getBalance() {
        CoroutineScope(Dispatchers.IO).launch {
            val request = api.getTokensBalanceListAsync(
                ApiRouter.Route.BALANCE_ALL.url,
                KeyStore.publicKey(context)
            )
            try {
                val response = request.await()
                balanceRepo.setAmount(response)
            } catch (e: HttpException) {
                e.printStackTrace()
                balanceRepo.setAmount(emptyList())
            } catch (e: Throwable) {
                e.printStackTrace()
                balanceRepo.setAmount(emptyList())
            }
        }
    }

    fun getStatistic() {
        CoroutineScope(Dispatchers.IO).launch {
            val request = api.getStatsAsync(ApiRouter.Route.STATS.url)
            try {
                val response = request.await()
                withContext(Dispatchers.Main) {
                    mutableStatistic.value = response
                }
            } catch (e: HttpException) {
                e.printStackTrace()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    private fun getTokenPrice() {
        CoroutineScope(Dispatchers.IO).launch {

            val request =
                api.getTokensPriceAsync(com.enecuum.lib.BuildConfig.COINGECKO, "enq-enecuum", "usd")

            try {
                val response = request.await()
                priceRepo.setAmount(response.`enq-enecuum`)
            } catch (e: HttpException) {
                e.printStackTrace()
                priceRepo.setAmount(CoingeckoData("0"))
            } catch (e: Throwable) {
                e.printStackTrace()
                priceRepo.setAmount(CoingeckoData("0"))
            }
        }
    }
}