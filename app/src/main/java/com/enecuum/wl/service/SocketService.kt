package com.enecuum.wl.service

import android.content.Context
import android.os.Bundle
import android.os.Message
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.enecuum.wl.BuildConfig.TOKEN
import com.enecuum.wl.Config.SOCKET_RECONNECT_TIMEOUT_IN_MILLIS
import com.enecuum.wl.utils.Constants
import com.enecuum.wl.utils.Constants.MESSAGE_BUNDLE_KEY
import com.enecuum.lib.KeyStore
import com.enecuum.lib.SageSign
import com.enecuum.lib.api.Hail
import com.enecuum.lib.api.HailData
import com.enecuum.lib.api.main.ApiRouter
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONObject
import java.net.URI

class SocketService(
    private val context: Context,
    private val okHttpClient: OkHttpClient
) : LooperCallback {

    private val looper by lazy { MessageLooper(context, this) }
    private val gson = Gson()

    private var mutableStatus: MutableLiveData<ServiceStatus> = MutableLiveData()
    val status: LiveData<ServiceStatus> = mutableStatus

    private lateinit var okSocket: WebSocket
    private val okSocketListener by lazy {
        object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket?, response: Response?) {
                Log.d("SocketService", "Websocket opened")

                mutableStatus.postValue(ServiceStatus.CONNECTING)
                val hailData = HailData(
                    SageSign.getSha256(URI(url).host),
                    SageSign.sign(context, KeyStore.secretKey(context).toByteArray()),
                    TOKEN,
                    KeyStore.publicKey(context)
                )
                sendMessage(gson.toJson(Hail(data = hailData)))

                mutableStatus.postValue(ServiceStatus.STARTED)

            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                val messageObject = JSONObject(text)
                if (messageObject.has(ERROR_FIELD)) {
                    val errorValue = messageObject.getString(ERROR_FIELD)
                    if (errorValue == ServiceError.ERR_WRONG_PROTOCOL_VERSION.name) {
                        stopServiceWithError(ServiceError.ERR_WRONG_PROTOCOL_VERSION)
                        return
                    }
                }

                val message = Message()
                message.data = Bundle().apply { putString(MESSAGE_BUNDLE_KEY, text) }
                looper.handler.sendMessage(message)
            }

            override fun onFailure(webSocket: WebSocket?, t: Throwable, response: Response?) {
                Log.d("SocketService", "Websocket closed with error")
                if (!Constants.refuseReconnection) {
                    reconnectWithDelay()
                } else {
                    Constants.refuseReconnection = false
                }
            }
        }
    }

    private var url = ApiRouter.wsURL

    fun startService(url: String) {
        this.url = url
        val request = Request.Builder().url(url).build()
        okSocket = okHttpClient.newWebSocket(request, okSocketListener)
        if (!looper.isAlive) looper.start()
    }

    fun stopService() {
        okSocket.close(1000, null)
        mutableStatus.postValue(ServiceStatus.STOPPED)
        Log.d("SocketService", "Websocket stopped")
    }

    private fun reconnectWithDelay() {
        mutableStatus.postValue(ServiceStatus.CONNECTING)
        CoroutineScope(Dispatchers.Default).launch {
            Log.d("SocketService", "Reconnecting in $reconnectTimeout sec")
            delay(reconnectTimeout)
            restartService(url)
        }
    }

    override fun restartService(url: String) {
        okSocket.close(1000, null)
        startService(url)
    }

    override fun sendMessage(message: String) {
        okSocket.send(message)
    }

    override fun stopServiceWithError(error: ServiceError) {
        Log.d("SocketService", "Got server error ${error.name}")

        Constants.refuseReconnection = true
        okSocket.close(1000, null)

        val status: ServiceStatus = when (error) {
            ServiceError.ERR_WRONG_PROTOCOL_VERSION -> ServiceStatus.TERMINATED_PROTO_ERROR
        }
        mutableStatus.postValue(status)
    }

    companion object {
        private const val ERROR_FIELD = "err"
        private const val reconnectTimeout = SOCKET_RECONNECT_TIMEOUT_IN_MILLIS
    }
}

enum class ServiceError { ERR_WRONG_PROTOCOL_VERSION /*, ERR_DUPLICATE_KEY*/ }