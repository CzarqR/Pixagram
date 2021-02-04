package com.myniprojects.pixagram.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import jdenticon.Jdenticon
import java.io.File

fun createImage(
    context: Context,
    username: String
): Uri
{
    val avatarFile = File.createTempFile(
        "SVG_",
        ".svg",
        context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    )

    val svgPlainText = Jdenticon.toSvg(username, Constants.AVATAR_BASE_SIZE)

    avatarFile.writeText(svgPlainText)

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        avatarFile
    )
}