package com.enecuum.wl.vvm.settings.details

import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.enecuum.wl.R
import com.enecuum.wl.service.DateService
import com.enecuum.wl.utils.BaseFragment
import com.enecuum.wl.utils.Constants.REPLACEMENT_URL_KEY
import com.enecuum.wl.utils.Constants.URL_KEY
import com.enecuum.wl.utils.LocaleHelper
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.fragment_settings_url.*
import java.util.*

class SettingsUrlFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings_url, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        btnBack.setOnClickListener {
            hideSoftKeyboard()
            activity!!.onBackPressed()
        }

        if (Prefs.getString(URL_KEY, "").isNotEmpty()) {
            txtUrl.setText(Prefs.getString(URL_KEY, ""))
        }

        btnSaveUrl.setOnClickListener {

            val res = resources

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val conf = res.configuration
                conf.locale =
                    Locale.forLanguageTag(LocaleHelper.getLocale()) // whatever you want here
                res.updateConfiguration(conf, null) // second arg null means don't change
            }

            if (txtUrl.text.toString().isNotBlank()) {
                AlertDialog.Builder(context!!)
                    .setTitle(res.getString(R.string.are_you_sure))
                    .setMessage(res.getString(R.string.application_restart))
                    .setPositiveButton(res.getString(R.string.execute)) { dialogInterface, _ ->
                        val intent = Intent(context, DateService::class.java)
                        activity?.stopService(intent)
                        Prefs.putString(REPLACEMENT_URL_KEY, txtUrl.text.toString())
                        dialogInterface.dismiss()
                        activity?.finish()
                    }
                    .setNegativeButton(res.getString(R.string.cancel)) { dialogInterface, _ ->
                        dialogInterface.dismiss()
                    }
                    .create()
                    .show()
            }
        }
    }

}