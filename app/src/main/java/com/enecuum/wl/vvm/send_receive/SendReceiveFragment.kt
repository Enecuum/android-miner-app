package com.enecuum.wl.vvm.send_receive

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.enecuum.wl.R
import com.enecuum.wl.utils.BaseFragment
import com.enecuum.wl.vvm.host_main.MainActivity
import kotlinx.android.synthetic.main.fragment_send_receive.*

class SendReceiveFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_send_receive, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vPager.adapter = SendReceiveAdapter(childFragmentManager, context!!)
        vPager.currentItem = SEND_TAB

        vTabs.setupWithViewPager(vPager)

        btnMenu.setOnClickListener {
            activity?.findViewById<View>(R.id.vBottomNavigation)?.visibility = View.GONE
            Navigation.findNavController(it)
                .navigate(R.id.action_sendReceiveFragment_to_settingsFragment)
        }

        btnBuy.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_sendReceiveFragment_to_buyFragment)
            activity?.findViewById<View>(R.id.vBottomNavigation)?.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.findViewById<View>(R.id.vBottomNavigation)?.visibility = View.VISIBLE
        (activity!! as MainActivity).selectedMenuId = R.id.bottomMenuSend
    }

    companion object {
        const val SEND_TAB = 0
        const val RECEIVE_TAB = 1
        const val SWAP_TAB = 2
    }

}