package com.enecuum.wl.extensions

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.enecuum.wl.utils.SafeClickListener

fun View.show() {
    this.visibility = View.VISIBLE
}

fun View.hide() {
    this.visibility = View.INVISIBLE
}

fun View.copyToClipboard(text: String) {
    val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
    val clip = ClipData.newPlainText("text", text)
    clipboard?.setPrimaryClip(clip)
}

fun View.showToast(resId: Int) = Toast.makeText(context, resId, Toast.LENGTH_LONG).show()

fun View.setOnSafeClickListener(interval: Int = 1000, onSafeClick: (View) -> Unit) {
    setOnClickListener(SafeClickListener(interval) { v -> onSafeClick(v) })
}

//awkward workaround for double-tap crashes
fun View.safeNavigate(actionId: Int) {
    val navController: NavController = Navigation.findNavController(this)
    val action = navController.currentDestination?.getAction(actionId)
    if (action != null) navController.navigate(actionId)
}