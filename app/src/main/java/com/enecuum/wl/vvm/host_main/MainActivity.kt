package com.enecuum.wl.vvm.host_main

import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.enecuum.wl.BuildConfig
import com.enecuum.wl.R
import com.enecuum.wl.utils.Constants
import com.enecuum.wl.utils.Constants.LANGUAGE_KEY
import com.enecuum.wl.utils.Constants.PUBLIC_KEY
import com.enecuum.wl.utils.Constants.PUBLIC_REF_KEY
import com.enecuum.wl.utils.Constants.REFERRAL_PUBLIC_KEY
import com.enecuum.wl.utils.LocaleHelper
import com.enecuum.wl.utils.publicToRef
import com.enecuum.lib.api.main.ApiRouter
import com.google.firebase.analytics.FirebaseAnalytics
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_main.*
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.*


class MainActivity : AppCompatActivity() {

    var selectedMenuId = R.id.bottomMenuHome
    var toFinish = false
    private val viewModel: ActivityViewModel by viewModel()
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        setContentView(R.layout.activity_main)

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        vBottomNavigation.visibility = View.GONE

        if (Prefs.getString(Constants.URL_KEY, "").isEmpty()) {
            Prefs.putString(Constants.URL_KEY, ApiRouter.wsURL)
        }

        if (Prefs.getString(Constants.REPLACEMENT_URL_KEY, "").isNotEmpty()) {
            Prefs.putString(Constants.URL_KEY, Prefs.getString(Constants.REPLACEMENT_URL_KEY, ""))
            Prefs.putString(Constants.REPLACEMENT_URL_KEY, "")
        }

        if (Prefs.getString(PUBLIC_REF_KEY, "").isEmpty()) {
            Prefs.putString(PUBLIC_REF_KEY, publicToRef(Prefs.getString(PUBLIC_KEY, "")))
        }

        if (Prefs.getString(REFERRAL_PUBLIC_KEY, "").isEmpty()) {
            Prefs.putString(REFERRAL_PUBLIC_KEY, BuildConfig.DEFAULT_REFERRAL)
        }

        findViewById<View>(R.id.vBottomNavigation)?.visibility = View.GONE

        vBottomNavigation.setOnNavigationItemSelectedListener {
            dispatchBottomSelect(it.itemId)
        }
        checkVersion()
    }

    private fun dispatchBottomSelect(itemId: Int): Boolean {
        if (itemId == selectedMenuId) {
            return true
        }
        when (itemId) {
            R.id.bottomMenuHome -> {
                when (selectedMenuId) {
                    R.id.bottomMenuTransactions -> safeNavigate(R.id.action_transactionsFragment_to_mainFragment)
                    R.id.bottomMenuSend -> safeNavigate(R.id.action_sendReceiveFragment_to_mainFragment)
                    R.id.bottomMenuQR -> safeNavigate(R.id.action_qrFragment_to_mainFragment)
                    R.id.bottomMenuRoi -> safeNavigate(R.id.action_roiFragment_to_mainFragment)
                    else -> {
                        onBackPressed()
                    }
                }
                selectedMenuId = itemId
                return true
            }
            R.id.bottomMenuSend -> {
                when (selectedMenuId) {
                    R.id.bottomMenuHome -> safeNavigate(R.id.action_mainFragment_to_sendReceiveFragment)
                    R.id.bottomMenuTransactions -> safeNavigate(R.id.action_transactionsFragment_to_sendReceiveFragment)
                    R.id.bottomMenuQR -> safeNavigate(R.id.action_qrFragment_to_sendReceiveFragment)
                    R.id.bottomMenuRoi -> safeNavigate(R.id.action_roiFragment_to_sendReceiveFragment)
                    else -> {
                        onBackPressed()
                    }
                }
                selectedMenuId = itemId
                return true
            }
            R.id.bottomMenuTransactions -> {
                when (selectedMenuId) {
                    R.id.bottomMenuHome -> safeNavigate(R.id.action_mainFragment_to_transactionsFragment)
                    R.id.bottomMenuSend -> safeNavigate(R.id.action_sendReceiveFragment_to_transactionsFragment)
                    R.id.bottomMenuQR -> safeNavigate(R.id.action_qrFragment_to_transactionsFragment)
                    R.id.bottomMenuRoi -> safeNavigate(R.id.action_roiFragment_to_transactionsFragment)
                    else -> {
                        onBackPressed()
                    }
                }
                selectedMenuId = itemId
                return true
            }
            R.id.bottomMenuQR -> {
                when (selectedMenuId) {
                    R.id.bottomMenuHome -> safeNavigate(R.id.action_mainFragment_to_qrFragment)
                    R.id.bottomMenuSend -> safeNavigate(R.id.action_sendReceiveFragment_to_qrFragment)
                    R.id.bottomMenuTransactions -> safeNavigate(R.id.action_transactionsFragment_to_qrFragment)
                    R.id.bottomMenuRoi -> safeNavigate(R.id.action_roiFragment_to_qrFragment)
                    else -> {
                        onBackPressed()
                    }
                }
                selectedMenuId = itemId
                return false
            }
            R.id.bottomMenuRoi -> {
                when (selectedMenuId) {
                    R.id.bottomMenuHome -> safeNavigate(R.id.action_mainFragment_to_roiFragment)
                    R.id.bottomMenuTransactions -> safeNavigate(R.id.action_transactionsFragment_to_roiFragment)
                    R.id.bottomMenuSend -> safeNavigate(R.id.action_sendReceiveFragment_to_roiFragment)
                    R.id.bottomMenuQR -> safeNavigate(R.id.action_qrFragment_to_roiFragment)
                    else -> {
                        onBackPressed()
                    }
                }
                selectedMenuId = itemId
                return true
            }
            else -> {
                return false
            }
        }
    }

    //awkward workaround for double-tap crashes
    private fun safeNavigate(actionId: Int) {
        val navController = navHostFragment.findNavController()
        val action = navController.currentDestination?.getAction(actionId)
        if (action != null) navController.navigate(actionId)
    }

    private fun showBuyDialog(referrerStake: String) {
        AlertDialog.Builder(this)
            .setTitle(R.string.referral_buy_title)
            .setMessage(resources.getString(R.string.referral_buy_message, referrerStake))
            .setPositiveButton(resources.getString(R.string.buy_enq)) { dialogInterface, _ ->
                Navigation.findNavController(this.view!!)
                    .navigate(R.id.action_mainFragment_to_buyFragment)
                dialogInterface.dismiss()
            }
            .setNegativeButton(resources.getString(R.string.cancel)) { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .create()
            .show()
    }


    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(updateBaseContextLocale(base))
    }

    private fun updateBaseContextLocale(context: Context): Context {
        val languages = context.resources.getStringArray(R.array.language_code)
        val language = if (Prefs.getString(LANGUAGE_KEY, "") == "") {
            if (languages.contains(Locale.getDefault().language)) Locale.getDefault().language else "en"
        } else {
            Prefs.getString(LANGUAGE_KEY, "")
        }
        LocaleHelper.setLocale(context, language)
        val locale = Locale(language)
        Locale.setDefault(locale)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            updateResourcesLocale(context, locale)
        } else {
            val res = context.resources
            val dm = res.displayMetrics
            val conf = res.configuration
            conf.setLocale(locale)
            res.updateConfiguration(conf, dm)
            context
        }
    }

    private fun updateResourcesLocale(context: Context, locale: Locale): Context {
        val configuration = context.resources.configuration
        configuration.setLocale(locale)
        return context.createConfigurationContext(configuration)
    }

    fun checkVersion() {

    }
}
