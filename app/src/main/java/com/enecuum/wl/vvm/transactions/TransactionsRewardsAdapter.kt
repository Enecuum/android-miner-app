package com.enecuum.wl.vvm.transactions

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.enecuum.wl.R
import com.enecuum.wl.vvm.send_receive.SendReceiveFragment.Companion.SWAP_TAB
import com.enecuum.wl.vvm.transactions.TransactionsRewardsFragment.Companion.REWARDS_TAB
import com.enecuum.wl.vvm.transactions.TransactionsRewardsFragment.Companion.TRANSACTIONS_TAB

class TransactionsRewardsAdapter(fm: FragmentManager, private val context: Context) :
    FragmentPagerAdapter(fm) {

    override fun getCount(): Int = 2

    override fun getItem(position: Int): Fragment {
        return when (position) {
            TRANSACTIONS_TAB -> TransactionsFragment()
            REWARDS_TAB -> RewardsFragment()
            else -> throw IndexOutOfBoundsException("Tabs!")
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            TRANSACTIONS_TAB -> context.getString(R.string.transactions)
            REWARDS_TAB -> context.getString(R.string.rewards)
            SWAP_TAB -> context.getString(R.string.swap)
            else -> throw IndexOutOfBoundsException("Tabs!")
        }
    }
}
