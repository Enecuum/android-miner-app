package com.enecuum.wl.vvm.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.enecuum.wl.R
import com.enecuum.wl.extensions.safeNavigate
import com.enecuum.wl.utils.BaseFragment
import com.enecuum.wl.utils.CacheHelper
import com.enecuum.lib.KeyStore
import kotlinx.android.synthetic.main.fragment_start.*

class StartFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_start, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        CacheHelper.deleteCache(context!!)

        if (KeyStore.publicKey(context!!).isNotEmpty()) {
            if (KeyStore.secretKey(context!!).isNotEmpty()) {
                //Reload keys to ensure data format
                KeyStore.saveKeys(context!!, KeyStore.secretKey(context!!))
            }

            view.safeNavigate(R.id.action_startFragment_to_mainFragment)
            return
        }

        btnNew.setOnClickListener {
            safeNavigate(
                Navigation.findNavController(it),
                R.id.action_startFragment_to_newWalletFragment
            )
        }

        btnImportWallet.setOnClickListener {
            safeNavigate(
                Navigation.findNavController(it),
                R.id.action_startFragment_to_privateKeyLoginFragment
            )
        }
    }

    //awkward workaround for double-tap crashes
    private fun safeNavigate(navController: NavController, actionId: Int) {
        val action = navController.currentDestination?.getAction(actionId)
        if (action != null) navController.navigate(actionId)
    }
}