package com.myniprojects.pixagram.model

import com.myniprojects.pixagram.utils.consts.DatabaseFields

data class ChatMessage(
    val textContent: String? = null,
    val time: Long = 0L,
    val imageUrl: String? = null,
)
{
    val toHashMap: HashMap<String, Any?>
        get() = hashMapOf(
            DatabaseFields.MESSAGE_FIELD_TEXT_CONTENT to textContent?.trim(),
            DatabaseFields.MESSAGE_FIELD_IMAGE_URL to imageUrl,
            DatabaseFields.MESSAGE_FIELD_TIME to time,
        )
}
