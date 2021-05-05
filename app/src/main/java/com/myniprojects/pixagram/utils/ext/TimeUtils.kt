package com.myniprojects.pixagram.utils.ext

import com.myniprojects.pixagram.utils.consts.Constants
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

fun Long.getDateTimeFormatFromMillis(
    format: String = Constants.DATE_TIME_FORMAT_MESSAGE
): String
{

    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
    {
        Instant.ofEpochMilli(this)
            .atZone(ZoneOffset.UTC)
            .toLocalDateTime()
            .format(
                DateTimeFormatter.ofPattern(
                    format,
                    Locale.getDefault()
                )
            )
    }
    else
    {
        val formatter = SimpleDateFormat(format, Locale.getDefault())
        val calendar = Calendar.getInstance()
            .apply { timeInMillis = this@getDateTimeFormatFromMillis }
        return formatter.format(calendar.time)
    }
}