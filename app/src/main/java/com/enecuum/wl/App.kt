package com.enecuum.wl

import android.content.ContextWrapper
import android.util.Log
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.enecuum.wl.di.appModule
import com.enecuum.wl.di.networkModule
import com.enecuum.lib.api.main.ApiRouter
import com.pixplicity.easyprefs.library.Prefs
import org.koin.android.ext.android.startKoin

class App : MultiDexApplication() {

    override fun onCreate() {
        MultiDex.install(this)
        super.onCreate()
        startKoin(this, listOf(appModule, networkModule))

        val setter = ApiRouter.getConnectionSetter(false)
        ApiRouter.setter = setter

        Log.d("DEBUG", com.enecuum.lib.BuildConfig.DEBUG.toString())
        Log.d("API", ApiRouter.apiURL)
        Log.d("WS", ApiRouter.wsURL)

        Prefs.Builder()
            .setContext(this)
            .setMode(ContextWrapper.MODE_PRIVATE)
            .setPrefsName(packageName)
            .setUseDefaultSharedPreference(true)
            .build()
    }
}