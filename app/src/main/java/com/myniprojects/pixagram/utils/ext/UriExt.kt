package com.myniprojects.pixagram.utils

import android.content.ContentResolver
import android.net.Uri
import android.webkit.MimeTypeMap
import java.io.File

fun Uri.getFileExt(contentResolver: ContentResolver, default: String = ""): String
{
    //Check uri format to avoid null
    return if (scheme.equals(ContentResolver.SCHEME_CONTENT))
    {
        MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(contentResolver.getType(this))
                ?: default
    }
    else
    {
        //If scheme is a File
        //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
        val p = path

        if (p != null)
        {

            MimeTypeMap.getFileExtensionFromUrl(
                Uri.fromFile(File(p))
                    .toString()
            )
        }
        else
        {
            default
        }
    }
}