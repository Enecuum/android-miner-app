package com.enecuum.wl.vvm.host_main

import androidx.lifecycle.ViewModel
import com.enecuum.lib.api.Version
import com.enecuum.lib.api.main.Api
import com.enecuum.lib.api.main.ApiRouter
import retrofit2.HttpException

class ActivityViewModel(private val api: Api) : ViewModel() {
    suspend fun checkVersion(): Version? {
        val request = api.getVersionAsync(ApiRouter.Route.VER.url)
        return try {
            request.await()
        } catch (e: HttpException) {
            e.printStackTrace()
            null
        } catch (e: Throwable) {
            e.printStackTrace()
            null
        }
    }
}