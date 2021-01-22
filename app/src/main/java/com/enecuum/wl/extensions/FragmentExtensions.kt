package com.enecuum.wl.extensions

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment


fun Fragment.openUrl(url: String) {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = Uri.parse(url)
    startActivity(intent)
}

fun Fragment.copyToClipboard(text: String) {
    val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
    val clip = ClipData.newPlainText("text", text)
    clipboard?.setPrimaryClip(clip)
}

fun Fragment.hideSoftKeyboard() {
    val focusedView = activity?.currentFocus
    if (focusedView != null) {
        val windowToken = focusedView.windowToken
        if (windowToken != null) {
            val imm =
                activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(windowToken, 0)
        }
    }
}

fun Fragment.showToast(message: String) = Toast.makeText(context, message, Toast.LENGTH_LONG).show()

fun Fragment.showToast(resId: Int) = Toast.makeText(context, resId, Toast.LENGTH_LONG).show()