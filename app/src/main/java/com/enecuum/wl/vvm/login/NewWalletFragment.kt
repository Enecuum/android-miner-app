package com.enecuum.wl.vvm.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import com.enecuum.wl.R
import com.enecuum.wl.extensions.showToast
import com.enecuum.wl.utils.BaseFragment
import com.enecuum.wl.utils.Constants.REFERRAL_SHOWED_KEY
import com.enecuum.lib.KeyStore
import com.enecuum.lib.SageSign
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.fragment_new_wallet.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class NewWalletFragment : BaseFragment(), CoroutineScope {

    private lateinit var parentJob: Job
    private val coroutineScope: CoroutineScope
        get() = CoroutineScope(Dispatchers.Main + parentJob)

    private var data: SageSign.GeneratedData? = null

    override val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.IO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parentJob = Job()
    }

    override fun onDestroy() {
        super.onDestroy()
        parentJob.cancel()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_new_wallet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val context = context ?: return

        KeyStore.resetAllKeys(context)

        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    KeyStore.resetAllKeys(context)
                    parentJob.cancel()
                    Navigation.findNavController(this@NewWalletFragment.requireView())
                        .popBackStack()
                }
            })

        coroutineScope.launch {
            data = SageSign.generateKeys()

            coroutineScope.launch(Dispatchers.Main) {
                lblSecretKey?.text =
                    data?.secretKey ?: getString(R.string.ellipsis_placeholder)
                lblPublicKey?.text =
                    data?.publicKey ?: getString(R.string.ellipsis_placeholder)
            }
        }

        btnBack.setOnClickListener {
            KeyStore.resetAllKeys(context)
            activity?.onBackPressed()
        }


        btnSecretKeyCopy.setOnClickListener {
            copyKey(data?.secretKey, getString(R.string.copied_private_key))
        }

        btnStartUsingWallet.setOnClickListener {
            val data = data ?: return@setOnClickListener
            KeyStore.saveKeys(context, data)

            if (KeyStore.referrerCode() == null) {
                val bundle = bundleOf("from_login" to true)
                Navigation.findNavController(it)
                    .navigate(R.id.action_newWalletFragment_to_referralFragment, bundle)
            } else {
                Prefs.putBoolean(REFERRAL_SHOWED_KEY, true)
                Navigation.findNavController(it)
                    .navigate(R.id.action_newWalletFragment_to_mainFragment)
            }
        }
    }

    private fun copyKey(key: String?, toastMessage: String) {
        val key = key ?: return
        copyToClipboard(key)
        showToast(toastMessage)
    }
}