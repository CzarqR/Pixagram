package com.myniprojects.pixagram.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val bio: String = "",
    val email: String = "",
    val id: String = "",
    val imageUrl: String = "",
    val username: String = "",
    val fullName: String = "",
    val usernameComparator: String = ""
) : Parcelable
