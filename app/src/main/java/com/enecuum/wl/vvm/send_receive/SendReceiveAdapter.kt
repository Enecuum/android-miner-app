package com.enecuum.wl.vvm.send_receive

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.enecuum.wl.R
import com.enecuum.wl.vvm.send_receive.SendReceiveFragment.Companion.RECEIVE_TAB
import com.enecuum.wl.vvm.send_receive.SendReceiveFragment.Companion.SEND_TAB
import com.enecuum.wl.vvm.send_receive.SendReceiveFragment.Companion.SWAP_TAB
import com.enecuum.wl.vvm.send_receive.tabs.receive.ReceiveFragment
import com.enecuum.wl.vvm.send_receive.tabs.send.SendFragment

class SendReceiveAdapter(fm: FragmentManager, private val context: Context) :
    FragmentPagerAdapter(fm) {

    override fun getCount(): Int = 2

    override fun getItem(position: Int): Fragment {
        return when (position) {
            SEND_TAB -> SendFragment()
            RECEIVE_TAB -> ReceiveFragment()
            else -> throw IndexOutOfBoundsException("Tabs!")
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            SEND_TAB -> context.getString(R.string.send)
            RECEIVE_TAB -> context.getString(R.string.receive)
            SWAP_TAB -> context.getString(R.string.swap)
            else -> throw IndexOutOfBoundsException("Tabs!")
        }
    }
}
