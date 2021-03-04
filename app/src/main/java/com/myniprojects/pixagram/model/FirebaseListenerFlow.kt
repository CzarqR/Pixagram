package com.myniprojects.pixagram.model

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class FirebaseListenerFlow<T>(
    private val _value: MutableStateFlow<T>,
    private var _eventListener: ValueEventListener? = null,
    private var _databaseReference: DatabaseReference? = null,
)
{
    fun setValue(value: T)
    {
        _value.value = value
    }

    val value = _value.asStateFlow()

    fun removeListener()
    {
        _databaseReference!!.removeEventListener(_eventListener!!)
    }

    fun addListener(eventListener: ValueEventListener, databaseReference: DatabaseReference)
    {
        _eventListener = eventListener
        _databaseReference = databaseReference
        _databaseReference!!.addValueEventListener(_eventListener!!)
    }

    fun addListener()
    {
        _databaseReference!!.addValueEventListener(_eventListener!!)
    }

    fun addSingleListener(eventListener: ValueEventListener, databaseReference: DatabaseReference)
    {
        _eventListener = eventListener
        _databaseReference = databaseReference
        _databaseReference!!.addListenerForSingleValueEvent(_eventListener!!)
    }

    fun addSingleListener()
    {
        _databaseReference!!.addListenerForSingleValueEvent(_eventListener!!)
    }
}