package com.enecuum.wl.vvm.settings.details.language

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.enecuum.wl.R
import com.enecuum.wl.utils.BaseFragment
import com.enecuum.wl.utils.Constants.LANGUAGE_KEY
import com.enecuum.wl.utils.LocaleHelper
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.fragment_settings_language.*


class SettingsLanguageFragment : BaseFragment(), SettingsLanguageAdapter.OnItemChooseListener {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings_language, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        btnBack.setOnClickListener {
            Navigation.findNavController(it).navigateUp()
        }

        rvLanguages.layoutManager = LinearLayoutManager(context)
        rvLanguages.adapter = SettingsLanguageAdapter(this)

    }

    override fun onItemClick(languageCode: String) {
        Prefs.putString(LANGUAGE_KEY, languageCode)
        LocaleHelper.setLocale(context!!, languageCode)

        activity?.recreate()
    }

}