package com.enecuum.wl.vvm.settings.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.enecuum.wl.R
import com.enecuum.wl.utils.BaseFragment
import kotlinx.android.synthetic.main.fragment_settings_community.*

class SettingsCommunityFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings_community, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        btnBack.setOnClickListener {
            Navigation.findNavController(it).navigateUp()
        }
    }

}