package com.enecuum.wl.vvm.referral

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.enecuum.lib.api.main.Api
import com.enecuum.lib.api.main.ApiRouter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.math.BigDecimal

class ReferralViewModel(private val api: Api) : ViewModel() {

    private val mutableValidatedKey = MutableLiveData<String>()
    val validatedKey = mutableValidatedKey

    fun validateReferral(publicKey: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val requestBalance =
                api.getDetailedBalanceAsync(ApiRouter.Route.BALANCE_ALL.url, publicKey)
            val request = api.getReferrerStakeAsync(ApiRouter.Route.REFERRER_STAKE.url)
            try {
                val responseBalance = requestBalance.await()
                val response = request.await()
                withContext(Dispatchers.Main) {
                    if (responseBalance.amount != null && BigDecimal(responseBalance.amount.toBigInteger()) >= response.referrerStake) {
                        mutableValidatedKey.value = publicKey
                    } else {
                        mutableValidatedKey.value = ""
                    }
                }
            } catch (e: HttpException) {
                withContext(Dispatchers.Main) {
                    mutableValidatedKey.value = "error"
                }
                e.printStackTrace()
            } catch (e: Throwable) {
                withContext(Dispatchers.Main) {
                    mutableValidatedKey.value = "error"
                }
                e.printStackTrace()
            }
        }
    }
}