package com.enecuum.wl.extensions

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

fun AppCompatActivity.openUrl(url: String) {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = Uri.parse(url)
    startActivity(intent)
}

fun AppCompatActivity.showToast(message: String) =
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()

fun AppCompatActivity.showToast(resId: Int) = Toast.makeText(this, resId, Toast.LENGTH_LONG).show()