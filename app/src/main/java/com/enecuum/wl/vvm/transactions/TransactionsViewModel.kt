package com.enecuum.wl.vvm.transactions

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.enecuum.wl.data.livedata.RewardsLiveDataRepository
import com.enecuum.wl.data.livedata.TransactionsLiveDataRepository
import com.enecuum.lib.KeyStore
import com.enecuum.lib.api.Transactions
import com.enecuum.lib.api.main.Api
import com.enecuum.lib.api.main.ApiRouter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.math.BigDecimal

interface TransactionsRequestListener {
    suspend fun onUpdate()
}

class TransactionsViewModel(
    val context: Context,
    private val api: Api,
    private val transactionsRepo: TransactionsLiveDataRepository,
    private val rewardsRepo: RewardsLiveDataRepository
) : ViewModel() {

    fun observeTransactions(owner: LifecycleOwner, observer: Observer<Transactions>) {
        transactionsRepo.observeTransactions(owner, observer)
        getTransactions()
    }

    fun observeRewards(owner: LifecycleOwner, observer: Observer<Transactions>) {
        rewardsRepo.observeTransactions(owner, observer)
        getRewards()
    }

    private fun getTransactions(transactionsListener: TransactionsRequestListener? = null) {
        CoroutineScope(Dispatchers.IO).launch {

            val url = ApiRouter.Route.ACCOUNT_TRANSACTIONS.url
            val request = api.getAccountTransactionsAsync(url, KeyStore.publicKey(context))

            try {
                val response = request.await()
                transactionsRepo.setTransactions(response)

                transactionsListener?.onUpdate()

            } catch (e: HttpException) {
                e.printStackTrace()
            } catch (e: Throwable) {
                e.printStackTrace()
                transactionsRepo.setTransactions(Transactions(BigDecimal(0.0), emptyList(), 0, ""))
            }
        }
    }

    private fun getRewards(transactionsListener: TransactionsRequestListener? = null) {
        CoroutineScope(Dispatchers.IO).launch {

            val request = api.getAccountTransactionsAsync(
                ApiRouter.Route.ACCOUNT_REWARDS.url,
                KeyStore.publicKey(context)
            )

            try {
                val response = request.await()
                rewardsRepo.setTransactions(response)

                transactionsListener?.onUpdate()

            } catch (e: HttpException) {
                e.printStackTrace()
            } catch (e: Throwable) {
                e.printStackTrace()
                rewardsRepo.setTransactions(Transactions(BigDecimal(0.0), emptyList(), 0, ""))
            }
        }
    }
}