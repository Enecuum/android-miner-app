package com.enecuum.wl.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.PowerManager
import com.enecuum.wl.BuildConfig
import com.enecuum.wl.service.ServiceNotification.NOTIFICATION_ID
import com.enecuum.wl.utils.AmountValue
import com.enecuum.wl.utils.BalanceStringFormatter
import com.enecuum.wl.utils.LocaleHelper
import com.enecuum.lib.KeyStore
import com.enecuum.lib.api.main.Api
import com.enecuum.lib.api.main.ApiRouter
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import java.util.*


class DateService : Service() {

    val service: SocketService by inject()
    var running = false

    private val api: Api by inject()
    private val binder = DatesBinder()
    private var job: Job? = null

    override fun onCreate() {
        super.onCreate()
        ServiceNotification.setUpChannel(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent?.action == STOP_ACTION) {
            stopInternal()
            stopForeground(true)
            stopSelf()
            return START_STICKY
        }

        CoroutineScope(Dispatchers.Default).launch {
            service.startService(ApiRouter.wsURL)
        }

        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        val wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "service:socket")
        wl.acquire(1000)
        running = true

        startForeground(NOTIFICATION_ID, ServiceNotification.build(this))
        startNotificationUpdateJob()

        return START_STICKY
    }

    private fun startNotificationUpdateJob() {
        job = CoroutineScope(Dispatchers.IO).launch {
            while (true) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    resources.configuration.locale =
                        Locale.forLanguageTag(LocaleHelper.getLocale()) // whatever you want here
                    resources.updateConfiguration(
                        resources.configuration,
                        null
                    ) // second arg null means don't change
                }

                val request = api.getTokensBalanceListAsync(
                    ApiRouter.Route.BALANCE_ALL.url,
                    KeyStore.publicKey(applicationContext)
                )
                try {
                    val responseList = request.await()
                    withContext(Dispatchers.Default) {
                        val miningTokenHash: String = BuildConfig.TOKEN
                        val token = responseList.find { it.token == miningTokenHash }
                        val mainBalance = token?.amount
                        val text = when {
                            mainBalance == null -> BalanceStringFormatter.noCurrencyString(resources)
                            mainBalance.isNotEmpty() -> {
                                AmountValue.cacheAvailableBalance(mainBalance)
                                BalanceStringFormatter.balanceString(
                                    resources,
                                    AmountValue.cachedAvailableBalance.toPlainString(),
                                    token.ticker
                                )
                            }
                            else -> AmountValue.UNKNOWN_VALUE
                        }

                        ServiceNotification.update(this@DateService, text)
                    }
                    delay(10 * 1000)
                } catch (e: Throwable) {
                    e.printStackTrace()
                    delay(2000)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopInternal()
    }

    private fun stopInternal() {
        if (running) {
            service.stopService()
        }
        running = false
        job?.cancel()
        ServiceNotification.clear(this)
    }

    override fun onBind(intent: Intent) = binder

    inner class DatesBinder : Binder() {
        fun getService() = this@DateService
    }

    companion object {
        const val STOP_ACTION = "ACTION_STOP_SERVICE"
    }
}
