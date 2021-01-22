package com.enecuum.wl.vvm.buy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.enecuum.wl.R
import com.enecuum.wl.utils.BaseFragment
import kotlinx.android.synthetic.main.fragment_buy.*

class BuyFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_buy, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        btnBackTop.setOnClickListener { activity!!.onBackPressed() }
    }

    override fun onResume() {
        super.onResume()
        activity?.findViewById<View>(R.id.vBottomNavigation)?.visibility = View.GONE
    }

}