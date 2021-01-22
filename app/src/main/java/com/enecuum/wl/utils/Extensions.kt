package com.enecuum.wl.utils

import android.util.TypedValue
import android.view.View

fun View.show() {
    this.visibility = View.VISIBLE
}

fun View.hide() {
    this.visibility = View.INVISIBLE
}

fun View.addCircleRipple() = with(TypedValue()) {
    context.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, this, true)
    setBackgroundResource(resourceId)
}

fun View.removeRipple() = setBackgroundResource(android.R.color.transparent)

infix fun String.xor(other: String): String {
    val sb = StringBuilder()
    for (i in 0 until this.length) {
        sb.append(
            (Integer.parseInt(this[i].toString(), 16) xor
                    Integer.parseInt(other[i].toString(), 16)).toString(16)
        )
    }
    return sb.toString()
}