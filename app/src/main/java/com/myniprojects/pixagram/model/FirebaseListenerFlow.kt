package com.myniprojects.pixagram.model

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class FirebaseListenerFlow<T>(
    private val eventListener: ValueEventListener,
    private val databaseReference: DatabaseReference,
    private val _value: MutableStateFlow<T>
)
{
    fun setValue(value: T)
    {
        _value.value = value
    }

    val value = _value.asStateFlow()

    fun removeListener()
    {
        databaseReference.removeEventListener(eventListener)
    }

    fun addListener()
    {
        databaseReference.addValueEventListener(eventListener)
    }

    fun addSingleListener()
    {
        databaseReference.addListenerForSingleValueEvent(eventListener)
    }
}