package com.enecuum.wl.vvm.faq

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import com.enecuum.wl.R
import com.enecuum.wl.utils.BaseFragment
import kotlinx.android.synthetic.main.fragment_faq.*

class FaqFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_faq, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        lblBattery.text = resources.getText(R.string.faq_battery)
        lblNetwork.text = resources.getText(R.string.faq_network)
        lblSupport.text = HtmlCompat.fromHtml(
            resources.getString(R.string.faq_support),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )

        lblAirdrop.movementMethod = LinkMovementMethod.getInstance()
        lblSupport.movementMethod = LinkMovementMethod.getInstance()

        btnBack.setOnClickListener {
            activity?.onBackPressed()
        }
    }
}