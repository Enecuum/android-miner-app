package com.enecuum.wl.vvm.send_receive.tabs.send

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.enecuum.wl.data.livedata.AmountLiveDataRepository
import com.enecuum.wl.data.livedata.TransactionLiveDataRepository
import com.enecuum.wl.utils.Constants
import com.enecuum.lib.KeyStore
import com.enecuum.lib.api.TokenBalance
import com.enecuum.lib.api.Transaction
import com.enecuum.lib.api.main.Api
import com.enecuum.lib.api.main.ApiRouter
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException

class SendViewModel(
    private val api: Api,
    private val balanceRepo: AmountLiveDataRepository,
    private val repo: TransactionLiveDataRepository
) : ViewModel() {

    fun observeTransaction(
        owner: LifecycleOwner,
        observer: Observer<com.enecuum.lib.api.TransactionResponse>
    ) {
        repo.observeTransaction(owner, observer)
    }

    fun observeBalance(owner: LifecycleOwner, observer: Observer<TokenBalance>) {
        balanceRepo.observeAmount(owner, observer)
    }

    fun removeObserver(owner: LifecycleOwner) {
        repo.removeObserverTransaction(owner)
    }

    fun removeTransactionResult() {
        repo.setTransaction(null)
    }

    fun postTransaction(body: List<Transaction.Request>) {
        CoroutineScope(Dispatchers.IO).launch {
            var url = ""
            if (Prefs.getString(Constants.URL_KEY, "").isNotEmpty()) {
                url = Prefs.getString(Constants.URL_KEY, "").split(":")[1]
            }
            val request = api.postTransactionAsync("http:$url:80/api/v1/tx", body)
            try {
                val response = request.await()
                repo.setTransaction(response)
            } catch (e: HttpException) {
                e.printStackTrace()
                repo.setTransaction(null)
            } catch (e: Throwable) {
                e.printStackTrace()
                repo.setTransaction(null)
            }
        }
    }

    fun getBalance(context: Context?) {
        CoroutineScope(Dispatchers.IO).launch {

            var url = ""
            if (Prefs.getString(Constants.URL_KEY, "").isNotEmpty()) {
                url = Prefs.getString(Constants.URL_KEY, "").split(":")[1]
            }
            val request = api.getTokensBalanceListAsync(
                ApiRouter.Route.BALANCE_ALL.url,
                KeyStore.publicKey(context!!)
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

}