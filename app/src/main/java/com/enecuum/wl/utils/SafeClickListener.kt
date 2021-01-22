package com.enecuum.wl.utils

import android.os.SystemClock
import android.view.View

class SafeClickListener(private val interval: Int = 1000, private val onSafeClick: (View) -> Unit) :
    View.OnClickListener {

    private var lastClickTime: Long = 0L

    override fun onClick(view: View) {
        if (SystemClock.elapsedRealtime() - lastClickTime < interval) {
            return
        }
        lastClickTime = SystemClock.elapsedRealtime()
        onSafeClick(view)
    }
}