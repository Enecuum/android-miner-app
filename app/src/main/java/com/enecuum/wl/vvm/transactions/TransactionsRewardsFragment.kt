package com.enecuum.wl.vvm.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.enecuum.wl.R
import com.enecuum.wl.utils.BaseFragment
import com.enecuum.wl.vvm.host_main.MainActivity
import kotlinx.android.synthetic.main.fragment_send_receive.*

class TransactionsRewardsFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_transactions_rewards, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnMenu.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_transactionsFragment_to_settingsFragment)
            activity?.findViewById<View>(R.id.vBottomNavigation)?.visibility = View.GONE
        }

        btnBuy.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_transactionsFragment_to_buyFragment)
            activity?.findViewById<View>(R.id.vBottomNavigation)?.visibility = View.GONE
        }

        vPager.adapter = TransactionsRewardsAdapter(childFragmentManager, context!!)
        vPager.currentItem = TRANSACTIONS_TAB

        vTabs.setupWithViewPager(vPager)
    }

    override fun onResume() {
        super.onResume()
        activity?.findViewById<View>(R.id.vBottomNavigation)?.visibility = View.VISIBLE
        (activity!! as MainActivity).selectedMenuId = R.id.bottomMenuSend
    }

    companion object {
        const val TRANSACTIONS_TAB = 0
        const val REWARDS_TAB = 1
        const val SWAP_TAB = 2
    }

}