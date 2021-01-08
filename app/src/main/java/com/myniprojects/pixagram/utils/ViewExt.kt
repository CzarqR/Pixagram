package com.myniprojects.pixagram.utils

import android.widget.EditText

inline val EditText.input: String
    get() = text.toString()