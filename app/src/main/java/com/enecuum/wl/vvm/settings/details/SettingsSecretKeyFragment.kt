package com.enecuum.wl.vvm.settings.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.enecuum.wl.R
import com.enecuum.wl.utils.BaseFragment
import com.enecuum.lib.KeyStore
import kotlinx.android.synthetic.main.fragment_settings_secret_key.*

class SettingsSecretKeyFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings_secret_key, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        btnBack.setOnClickListener {
            activity?.onBackPressed()
        }

        if (KeyStore.secretKey(requireContext()).isNotEmpty()) {
            lblSecretKey.setText(KeyStore.secretKey(requireContext()))
        }

        btnCopy.setOnClickListener {
            copyToClipboard(lblSecretKey.text.toString())
            Toast.makeText(
                context,
                resources.getString(R.string.copied_private_key),
                Toast.LENGTH_SHORT
            ).show()
        }

        btnImportWallet.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_settingsKeysFragment_to_keyImportFragment)
        }
    }
}