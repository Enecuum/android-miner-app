package com.enecuum.wl.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.enecuum.wl.R


open class BaseFragment : Fragment() {

    fun showMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun networkAvailable(showDefaultMessage: Boolean): Boolean {
        val connectivityManager =
            activity!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
        return if (networkInfo != null && networkInfo.isConnected) {
            true
        } else if (showDefaultMessage) {
            showMessage(resources.getString(R.string.no_internet))
            false
        } else {
            false
        }
    }

    fun hideSoftKeyboard() {
        val focusedView = activity!!.currentFocus
        if (focusedView != null) {
            val windowToken = focusedView.windowToken
            if (windowToken != null) {
                val imm =
                    activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(windowToken, 0)
            }
        }
    }

    fun copyToClipboard(text: String) {
        val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        val clip = ClipData.newPlainText("text", text)
        clipboard!!.setPrimaryClip(clip)
    }

}