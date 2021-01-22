package com.enecuum.wl.extensions

import android.graphics.*

fun Bitmap.invert(): Bitmap? {
    val height = this.height
    val width = this.width
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint()
    val matrixInvert = ColorMatrix()
    matrixInvert.set(
        floatArrayOf(
            -1.0f, 0.0f, 0.0f, 0.0f, 255.0f,
            0.0f, -1.0f, 0.0f, 0.0f, 255.0f,
            0.0f, 0.0f, -1.0f, 0.0f, 255.0f,
            0.0f, 0.0f, 0.0f, 1.0f, 0.0f
        )
    )
    val filter = ColorMatrixColorFilter(matrixInvert)
    paint.colorFilter = filter
    canvas.drawBitmap(this, 0f, 0f, paint)
    return bitmap
}