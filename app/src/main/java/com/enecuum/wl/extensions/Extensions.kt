package com.enecuum.wl.extensions

import android.animation.Animator
import android.animation.ObjectAnimator
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

infix fun String.xor(other: String): String {
    val sb = StringBuilder()
    for (i in this.indices) {
        sb.append(
            (Integer.parseInt(this[i].toString(), 16) xor
                    Integer.parseInt(other[i].toString(), 16)).toString(16)
        )
    }
    return sb.toString()
}

fun String.unifyDelimiter(): String {
    val string = replace(" ", "")
    return if (string.contains('.')) {
        string.replace(",", "")
    } else {
        string.replace(',', '.')
    }
}

fun EditText.textToNormalizedDouble(): Double = text.toString().unifyDelimiter().toDouble()

fun Boolean.toInt() = if (this) 1 else 0

fun EditText.afterTextChanged(afterTextChanged: (String?) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }
    })
}

fun ObjectAnimator.onAnimationEnd(onAnimationEnd: () -> Unit) {
    this.addListener(object : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator?) {
        }

        override fun onAnimationCancel(animation: Animator?) {
        }

        override fun onAnimationEnd(animation: Animator?) {
            onAnimationEnd.invoke()
        }

        override fun onAnimationRepeat(animation: Animator?) {
        }
    })
}