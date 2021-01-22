package com.enecuum.wl.vvm.qr

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.enecuum.wl.BuildConfig
import com.enecuum.wl.Config.GET_BALANCE_TIMEOUT_IN_MILLIS_INACTIVE
import com.enecuum.wl.data.livedata.AmountLiveDataRepository
import com.enecuum.wl.utils.AmountValue
import com.enecuum.lib.KeyStore
import com.enecuum.lib.api.MinStake
import com.enecuum.lib.api.Statistics
import com.enecuum.lib.api.main.Api
import com.enecuum.lib.api.main.ApiRouter
import kotlinx.coroutines.*
import retrofit2.HttpException
import java.math.BigDecimal

class QRViewModel(
    protected val context: Context,
    private val api: Api,
    private val repo: AmountLiveDataRepository
) : ViewModel() {

    private var continueGetBalance = false
    var balanceDelay = GET_BALANCE_TIMEOUT_IN_MILLIS_INACTIVE

    private val mutableStatistic = MutableLiveData<Statistics>()
    private val mutableMinStake = MutableLiveData<MinStake>()
    private val mutableReferrerStake = MutableLiveData<BigDecimal>()
    private val mutableReferrerShare = MutableLiveData<BigDecimal>()


    val statistic: LiveData<Statistics> = mutableStatistic
    val referrerStake: LiveData<BigDecimal> = mutableReferrerStake

    private fun getBalanceObservable() {
        CoroutineScope(Dispatchers.IO).launch {
            val request = api.getTokensBalanceListAsync(
                ApiRouter.Route.BALANCE_ALL.url,
                KeyStore.publicKey(context)
            )
            try {
                val response = request.await()
                repo.setAmount(response)
                getTokenInfo()
                getStatistic()
                if (continueGetBalance) {
                    delay(balanceDelay)
                    getBalanceObservable()
                }
            } catch (e: HttpException) {
                e.printStackTrace()
                repo.setAmount(emptyList())
            } catch (e: Throwable) {
                e.printStackTrace()
                repo.setAmount(emptyList())
            }
        }
    }

    fun getTokenInfo() {
        CoroutineScope(Dispatchers.IO).launch {
            val request = api.getTokenInfoAsync(ApiRouter.Route.TOKEN_INFO.url, BuildConfig.TOKEN)
            try {
                val response = request.await()
                withContext(Dispatchers.Main) {
                    mutableMinStake.value =
                        MinStake(AmountValue.convertFromApiRaw(response.first().min_stake))
                    mutableReferrerStake.value =
                        AmountValue.convertFromApiRaw(response.first().referrer_stake)
                    mutableReferrerShare.value =
                        AmountValue.convertFromApiRaw(response.first().ref_share)
                }

            } catch (e: HttpException) {
                e.printStackTrace()
            } catch (e: Throwable) {
                withContext(Dispatchers.Main) {
                    mutableMinStake.value = MinStake(BigDecimal("1000"))
                    mutableReferrerStake.value = BigDecimal("1000")
                    mutableReferrerShare.value = BigDecimal("1000")
                }

                e.printStackTrace()
            }
        }
    }

    private suspend fun getStatistic() {
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