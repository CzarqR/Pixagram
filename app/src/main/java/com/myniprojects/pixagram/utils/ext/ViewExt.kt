package com.myniprojects.pixagram.utils.ext

import android.app.Activity
import android.content.Context
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.Typeface
import android.text.TextPaint
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.myniprojects.pixagram.R


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

fun Activity.hideKeyboard()
{
    if (this.window != null)
    {
        val imm =
                this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(this.window.decorView.windowToken, 0)

        //remove focus from EditText
        findViewById<View>(android.R.id.content).clearFocus()
    }
}

fun Fragment.hideKeyboard()
{
    requireActivity().hideKeyboard()
}

fun AppCompatActivity.setActionBarTitle(title: String)
{
    supportActionBar?.title = title
}

fun Fragment.setActionBarTitle(title: String)
{
    (requireActivity() as? AppCompatActivity)?.setActionBarTitle(title)
}

fun Fragment.setActionBarTitle(@StringRes title: Int)
{
    (requireActivity() as? AppCompatActivity)?.setActionBarTitle(getString(title))
}


inline val Fragment.isFragmentAlive
    get() = !(this.isRemoving || this.activity == null || this.isDetached || !this.isAdded || this.view == null)


inline val ViewBinding.context: Context
    get() = root.context

fun MaterialToolbar.setFontHome(
    fontName: String,
    fontSizeInDp: Float
)
{
    for (i in 0 until childCount)
    {
        val view = getChildAt(i)
        if (view is AppCompatTextView)
        {
            view.typeface = Typeface.createFromAsset(context.assets, "fonts/$fontName")
            view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSizeInDp)

            val paint: TextPaint = view.paint
            val width = paint.measureText(context.getString(R.string.app_name))

            val textShader: Shader = LinearGradient(
                0f, 0f, width, view.textSize, intArrayOf(
                    ContextCompat.getColor(context, R.color.amber_400),
                    ContextCompat.getColor(context, R.color.amber_500),
                ), null, Shader.TileMode.CLAMP
            )
            view.paint.shader = textShader

            break
        }
    }
}

fun MaterialToolbar.setFontDefault(
    typeface: Pair<Typeface, Float>,
)
{
    for (i in 0 until childCount)
    {
        val view = getChildAt(i)
        if (view is AppCompatTextView)
        {
            view.typeface = typeface.first
            view.setTextSize(TypedValue.COMPLEX_UNIT_PX, typeface.second)
            view.paint.shader = null
            break
        }
    }
}


fun MaterialToolbar.getTypeface(): Pair<Typeface, Float>
{
    for (i in 0 until childCount)
    {
        val view = getChildAt(i)
        if (view is AppCompatTextView)
        {
            return view.typeface to view.textSize
        }
    }
    throw Exception("Any font wasn't found in Toolbar")
}