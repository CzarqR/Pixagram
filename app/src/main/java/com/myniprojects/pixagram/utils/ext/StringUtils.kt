package com.myniprojects.pixagram.utils.ext

import androidx.lifecycle.MutableLiveData
import com.myniprojects.pixagram.utils.consts.Constants
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

fun MutableLiveData<String>.trim()
{
    value = value?.trim()
}

fun String.formatQuery(): String = this.lowercase(Locale.getDefault()).trim()

/**
 * return string in format that can be
 * compared with values saved in database
 */
fun String.normalize(): String = this.lowercase(Locale.ENGLISH).trim()

/**
 * Compare two String after normalization
 */
infix fun String?.isEqualTo(second: String?): Boolean = this?.normalize() == second?.normalize()

fun Long.formatWithSpaces(): String
{
    val sb = StringBuilder().append(this)

    for (i in sb.length - 3 downTo 1 step 3)
    {
        sb.insert(i, ' ')
    }

    return sb.toString()
}

fun Long.getDateTimeFormat(): String
{

    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
    {
        Instant.ofEpochMilli(this)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
            .format(
                DateTimeFormatter.ofPattern(
                    Constants.DATE_TIME_FORMAT_POST,
                    Locale.getDefault()
                )
            )
    }
    else
    {
        val formatter = SimpleDateFormat(Constants.DATE_TIME_FORMAT_POST, Locale.getDefault())
        val calendar = Calendar.getInstance().apply { timeInMillis = this@getDateTimeFormat }
        return formatter.format(calendar.time)
    }
}

private val onlyEmojiRegex = "[^\\p{L}\\p{N}\\p{P}\\p{Z}]".toRegex()

val String.isOnlyEmoji
    get() = replace(onlyEmojiRegex, "") == ""