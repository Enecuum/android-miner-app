package com.enecuum.wl.utils

import android.content.res.Resources
import com.enecuum.wl.R

object BalanceStringFormatter {

    fun balanceString(resources: Resources, balance: String, ticker: String): String =
        resources.getString(R.string.balance_enq, balance)

    fun noCurrencyString(resources: Resources): String = resources.getString(R.string.no_enq)
}