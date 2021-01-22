package com.enecuum.wl.vvm.key_import

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.enecuum.wl.R
import com.enecuum.wl.utils.BaseFragment
import com.enecuum.lib.KeyStore
import kotlinx.android.synthetic.main.fragment_private_key_import.*

class KeyImportFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_private_key_import, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        btnBack.setOnClickListener {
            hideSoftKeyboard()
            activity?.onBackPressed()
        }

        btnSignIn.setOnClickListener {
            hideSoftKeyboard()

            val key = txtSecretKey.text?.toString() ?: return@setOnClickListener
            if (!KeyStore.validateSecretKey(key)) {
                Toast.makeText(
                    context,
                    resources.getString(R.string.wrong_secret_key_format),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            AlertDialog.Builder(context)
                .setTitle(resources.getString(R.string.are_you_sure))
                .setMessage(resources.getString(R.string.import_new_private_key))
                .setPositiveButton(resources.getString(R.string.yes)) { dialogInterface, _ ->
                    if (key.isNotBlank()) {
                        KeyStore.saveKeys(context!!, key)
                    } else {
                        Toast.makeText(
                            context,
                            resources.getString(R.string.empty_private_key),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    dialogInterface.dismiss()
                    activity?.onBackPressed()
                }
                .setNegativeButton(resources.getString(R.string.no)) { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }
                .create()
                .show()
        }
    }
}