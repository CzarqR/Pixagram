package com.myniprojects.pixagram.utils

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.annotation.StringRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.children
import com.google.android.material.snackbar.BaseTransientBottomBar
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


fun CoordinatorLayout.showSnackbar(
    message: String,
    buttonText: String? = null,
    action: () -> Unit = {},
    length: Int = Snackbar.LENGTH_LONG,
    gravity: Int = Gravity.TOP
)
{
    val s = Snackbar
        .make(this, message, length)

    buttonText?.let {
        s.setAction(it) {
            action()
        }
    }

    val params = s.view.layoutParams as CoordinatorLayout.LayoutParams
    params.gravity = gravity
    s.view.layoutParams = params
    s.animationMode = BaseTransientBottomBar.ANIMATION_MODE_FADE

    s.show()
}

fun View.setViewAndChildrenEnabled(isEnabled: Boolean)
{
    this.isEnabled = isEnabled

    (this as? ViewGroup)?.let { viewGroup ->
        viewGroup.children.forEach { view ->
            view.setViewAndChildrenEnabled(isEnabled)
        }
    }
}