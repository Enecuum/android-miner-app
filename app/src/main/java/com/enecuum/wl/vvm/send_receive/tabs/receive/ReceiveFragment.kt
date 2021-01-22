package com.enecuum.wl.vvm.send_receive.tabs.receive

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.enecuum.wl.R
import com.enecuum.wl.utils.BaseFragment
import com.enecuum.wl.utils.shareQRcode
import com.enecuum.lib.KeyStore
import kotlinx.android.synthetic.main.fragment_receive.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.glxn.qrgen.android.QRCode


class ReceiveFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_receive, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        lblAddress.text = KeyStore.publicKey(context!!)
        imgQrAddress.setImageBitmap(
            QRCode.from(KeyStore.publicKey(context!!)).withSize(500, 500).bitmap()
        )

        btnCopy.setOnClickListener {
            copyToClipboard(KeyStore.publicKey(context!!))
            Toast.makeText(
                context,
                resources.getString(R.string.copied_address),
                Toast.LENGTH_SHORT
            ).show()
        }

        btnShare.setOnClickListener {
            CoroutineScope(Dispatchers.Default).launch {
                shareQRcode(KeyStore.publicKey(context!!), requireContext())
            }
        }
    }
}