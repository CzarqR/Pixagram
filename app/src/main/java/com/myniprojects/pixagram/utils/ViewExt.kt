package com.myniprojects.pixagram.utils

import android.view.View
import android.widget.EditText
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar

inline val EditText.input: String
    get() = text.toString()

fun View.showSnackbar(
    @StringRes messageId: Int,
    @StringRes buttonId: Int? = null,
    action: () -> Unit = {},
    length: Int = Snackbar.LENGTH_LONG
) = showSnackbar(
    message = this.context.getString(messageId),
    buttonText = if (buttonId == null) null else this.context.getString(buttonId),
    action = action,
    length = length
)

fun View.showSnackbar(
    message: String,
    buttonText: String? = null,
    action: () -> Unit = {},
    length: Int = Snackbar.LENGTH_LONG
)
{
    val s = Snackbar
        .make(this, message, length)

    buttonText?.let {
        s.setAction(it) {
            action()
        }
    }

    s.show()
}