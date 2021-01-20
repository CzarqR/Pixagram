package com.myniprojects.pixagram.adapters.searchadapter

import com.myniprojects.pixagram.model.Tag
import com.myniprojects.pixagram.model.User

sealed class SearchModel
{
    data class UserItem(val user: User) : SearchModel()
    data class TagItem(val tag: Tag) : SearchModel()
}
