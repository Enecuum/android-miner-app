package com.enecuum.wl.vvm.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.enecuum.wl.R
import com.enecuum.wl.utils.BaseFragment
import com.enecuum.wl.utils.Constants
import com.enecuum.lib.KeyStore
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.fragment_private_key_login.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class PrivateKeyLoginFragment : BaseFragment(), CoroutineScope {

    private lateinit var parentJob: Job

    private val coroutineScope: CoroutineScope
        get() = CoroutineScope(Dispatchers.Main + parentJob)

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
    ): View? =
        inflater.inflate(R.layout.fragment_private_key_login, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        btnBack.setOnClickListener {
            activity?.onBackPressed()
        }


        btnSignIn.setOnClickListener {
            hideSoftKeyboard()

            val key = txtSecretKey.text?.toString() ?: return@setOnClickListener

            if (!KeyStore.validateSecretKey(key)) {
                return@setOnClickListener
            }

            coroutineScope.launch {
                val context = context ?: return@launch
                KeyStore.saveKeys(context, key)

                Prefs.putBoolean(Constants.REFERRAL_SHOWED_KEY, true)
                Navigation.findNavController(view)
                    .navigate(R.id.action_privateKeyLoginFragment_to_mainFragment)
            }
        }
    }
}