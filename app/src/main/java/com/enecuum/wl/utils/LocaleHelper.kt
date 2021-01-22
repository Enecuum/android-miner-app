package com.enecuum.wl.utils

import android.content.Context
import com.enecuum.wl.utils.Constants.LANGUAGE_KEY
import com.pixplicity.easyprefs.library.Prefs
import java.util.*


object LocaleHelper {

    fun setLocale(context: Context, language: String): Context {
        persist(language)
        return updateResources(context, language)
    }

    private fun persist(language: String?) {
        Prefs.putString(LANGUAGE_KEY, language)
    }

    fun getLocale(): String {
        return Prefs.getString(LANGUAGE_KEY, "")
    }

    private fun updateResources(context: Context, language: String?): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val configuration = context.resources.configuration
        configuration.setLocale(locale)

        return context.createConfigurationContext(configuration)
    }

}