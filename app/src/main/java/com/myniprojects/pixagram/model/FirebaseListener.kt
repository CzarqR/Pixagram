package com.myniprojects.pixagram.model

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

data class FirebaseListener<T>(
    private val _eventListener: ValueEventListener,
    private val _databaseReference: DatabaseReference,
)
{
    fun removeListener()
    {
        _databaseReference.removeEventListener(_eventListener)
    }

    fun addListener()
    {
        _databaseReference.addValueEventListener(_eventListener)
    }

    fun addSingleListener()
    {
        _databaseReference.addListenerForSingleValueEvent(_eventListener)
    }
}