package com.enecuum.wl.vvm.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.enecuum.wl.R
import com.enecuum.wl.utils.BaseFragment
import com.enecuum.wl.vvm.host_main.MainActivity
import kotlinx.android.synthetic.main.fragment_settings.*

class SettingsFragment : BaseFragment() {

    private var tapCount = 0
    private var startTapMillis: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        btnBack.setOnClickListener {
            activity?.onBackPressed()
        }

        vSettingUrl.visibility = View.GONE
        imgSettingUrl.visibility = View.GONE
        lblSettingUrl.visibility = View.GONE
        vSettingReferral.visibility = View.GONE
        imgSettingReferral.visibility = View.GONE
        lblSettingReferral.visibility = View.GONE

        vSettingAbout.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_settingsFragment_to_settingsAboutAppFragment)
        }

        vSettingUrl.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_settingsFragment_to_settingsUrlBottomSheetFragment)
        }

        vSettingKeys.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_settingsFragment_to_settingsKeysFragment)
        }

        vSettingAddress.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_settingsFragment_to_settingsPublicKeyFragment)
        }

        vSettingCommunity.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_settingsFragment_to_settingsCommunityFragment)
        }

        vSettingFaq.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_settingsFragment_to_faqFragment2)
        }

        vSettingLang.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_settingsFragment_to_settingsLanguageFragment)
        }

        vSettingReferral.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_settingsFragment_to_referralFragment)
        }

        title.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val time = System.currentTimeMillis()

                if (startTapMillis == 0L || (time - startTapMillis > 2500)) {
                    startTapMillis = time
                    tapCount = 1
                } else {
                    tapCount++
                }

                if (tapCount == 10) {
                    vSettingUrl.visibility = View.VISIBLE
                    imgSettingUrl.visibility = View.VISIBLE
                    lblSettingUrl.visibility = View.VISIBLE
                    vSettingReferral.visibility = View.VISIBLE
                    imgSettingReferral.visibility = View.VISIBLE
                    lblSettingReferral.visibility = View.VISIBLE
                }
                return@setOnTouchListener true
            }
            return@setOnTouchListener false
        }

    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).toFinish = false
    }

}