package com.enecuum.wl.vvm.statistic

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.enecuum.wl.R
import com.enecuum.wl.utils.BaseFragment
import com.enecuum.wl.vvm.host_main.MainActivity
import com.enecuum.lib.api.Statistics
import kotlinx.android.synthetic.main.fragment_statistic.*
import org.koin.android.viewmodel.ext.android.viewModel

class StatisticFragment : BaseFragment(), Observer<Statistics> {

    private val viewModel: StatisticViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_statistic, container, false)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        vProgress.visibility = View.VISIBLE
        lblMapLoading.visibility = View.VISIBLE

        wvMap.settings.javaScriptEnabled = true
        wvMap.setPadding(0, 0, 0, 0)
        wvMap.settings.loadWithOverviewMode = true
        wvMap.settings.useWideViewPort = true
        wvMap.isDrawingCacheEnabled = true
        wvMap.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(webView: WebView, urlNewString: String): Boolean {
                return true
            }

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {

            }

            override fun onPageFinished(view: WebView, url: String) {
                try {
                    vProgress.visibility = View.GONE
                    lblMapLoading.visibility = View.GONE
                    vMapHider.visibility = View.GONE
                } catch (e: Exception) {
                }
            }
        }

        wvMap.webChromeClient = WebChromeClient()
        wvMap.settings.domStorageEnabled = true
        wvMap.clearCache(true)
        wvMap.settings.setAppCacheEnabled(false)
        wvMap.settings.cacheMode = WebSettings.LOAD_NO_CACHE

        if (networkAvailable(true)) {
            wvMap.loadUrl("")
        }

        viewModel.observeStatistic(this, this)

        btnSettings.setOnClickListener {
            activity?.findViewById<View>(R.id.vBottomNavigation)?.visibility = View.GONE
            Navigation.findNavController(it)
                .navigate(R.id.action_statisticFragment2_to_settingsFragment)
        }

        vNew.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW)
            startActivity(i)
        }


        btnBuy.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_statisticFragment2_to_buyFragment)
            activity?.findViewById<View>(R.id.vBottomNavigation)?.visibility = View.GONE
        }

    }

    override fun onResume() {
        super.onResume()
        activity?.findViewById<View>(R.id.vBottomNavigation)?.visibility = View.VISIBLE
        (activity!! as MainActivity).selectedMenuId = R.id.bottomMenuHome
    }

    override fun onChanged(t: Statistics?) {
        if (t != null) {
            lblAccountsVal.text = "${t.accounts}"
            lblCurrTpsVal.text = "${t.tps}"
            lblMaxTpsVal.text = "${t.max_tps}"
            lblPoaNodesVal.text = "${t.poa_count}"
            lblPowNodesVal.text = "${t.pow_count}"
            lblPosNodesVal.text = "${t.pos_count}"
        }
    }

}