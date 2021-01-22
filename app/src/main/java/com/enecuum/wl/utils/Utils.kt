package com.enecuum.wl.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.content.FileProvider
import com.enecuum.wl.BuildConfig
import com.enecuum.wl.R
import com.enecuum.wl.utils.Constants.XOR_STRING
import net.glxn.qrgen.android.QRCode
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

fun publicToRef(publicKey: String) = "ref_" + (publicKey xor XOR_STRING)

fun refToPublic(refKey: String) = refKey.substring(4) xor XOR_STRING

fun shareQRcode(source: String, context: Context, backgroundColor: Int = Color.WHITE) {
    val file = File(context.cacheDir, "qr.jpeg")
    file.createNewFile()

    val bitmap =
        QRCode.from(source).withSize(500, 500).withColor(Color.BLACK, backgroundColor).bitmap()
    val bos = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)
    val bitmapData = bos.toByteArray()

    val fos = FileOutputStream(file)
    fos.write(bitmapData)
    fos.flush()
    fos.close()

    val uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID, file)

    val intent = Intent(Intent.ACTION_SEND)
    intent.type = "image/jpeg"
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    intent.putExtra(Intent.EXTRA_STREAM, uri)
    val chooser = Intent.createChooser(intent, context.resources.getString(R.string.share_qr))

    context.startActivity(chooser)
}