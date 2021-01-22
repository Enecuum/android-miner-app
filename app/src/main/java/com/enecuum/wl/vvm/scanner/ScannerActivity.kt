package com.enecuum.wl.vvm.scanner

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ablanco.imageprovider.ImageProvider
import com.ablanco.imageprovider.ImageSource
import com.enecuum.wl.R
import com.enecuum.wl.vvm.send_receive.tabs.send.SendFragment.Companion.SCAN_RESULT_EXTRA
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.Result
import com.google.zxing.common.HybridBinarizer
import kotlinx.android.synthetic.main.activity_scanner.*
import me.dm7.barcodescanner.zxing.ZXingScannerView

class SimpleScannerActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {

    public override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        setContentView(R.layout.activity_scanner)
    }

    public override fun onResume() {
        super.onResume()
        vScanner.setResultHandler(this)
        vScanner.startCamera()

        btnBack.setOnClickListener {
            onBackPressed()
        }

        btnGallery.setOnClickListener {
            ImageProvider(this).getImage(ImageSource.GALLERY) { bitmap ->
                if (bitmap != null) {
                    decodeFromGallery(bitmap)
                }
            }
        }

    }

    public override fun onPause() {
        super.onPause()
        vScanner.stopCamera()
    }

    override fun handleResult(rawResult: Result) {
        vScanner.resumeCameraPreview(this)
        setResult(
            Activity.RESULT_OK,
            Intent().apply { putExtra(SCAN_RESULT_EXTRA, rawResult.text.toString()) })
        finish()
    }

    private fun decodeFromGallery(generatedQRCode: Bitmap) {
        val width = generatedQRCode.width
        val height = generatedQRCode.height
        val pixels = IntArray(width * height)
        generatedQRCode.getPixels(pixels, 0, width, 0, 0, width, height)

        val source = RGBLuminanceSource(width, height, pixels)

        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

        val reader = MultiFormatReader()
        var result: Result?
        try {
            result = reader.decode(binaryBitmap)
            setResult(
                Activity.RESULT_OK,
                Intent().apply { putExtra(SCAN_RESULT_EXTRA, result!!.text) })
            finish()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, resources.getString(R.string.error), Toast.LENGTH_SHORT).show()
        }
    }
}
