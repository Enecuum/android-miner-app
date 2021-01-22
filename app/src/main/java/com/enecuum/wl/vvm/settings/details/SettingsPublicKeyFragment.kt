package com.enecuum.wl.vvm.settings.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.enecuum.wl.R
import com.enecuum.wl.utils.BaseFragment
import com.enecuum.lib.KeyStore
import kotlinx.android.synthetic.main.fragment_settings_public_key.*

class SettingsPublicKeyFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings_public_key, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        btnBack.setOnClickListener {
            activity!!.onBackPressed()
        }

        lblPublicKey.text = KeyStore.publicKey(context!!)

        btnCopy.setOnClickListener {
            copyToClipboard(lblPublicKey.text.toString())
            Toast.makeText(
                context!!,
                resources.getString(R.string.copied_address),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

}