package com.myniprojects.pixagram.vm

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.myniprojects.pixagram.model.User
import com.myniprojects.pixagram.ui.fragments.SearchFragment
import com.myniprojects.pixagram.utils.DatabaseFields
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber

class SearchViewModel @ViewModelInject constructor(

) : ViewModel()
{


    private val _users: MutableStateFlow<List<User>> = MutableStateFlow(listOf())
    val users: StateFlow<List<User>> = _users

    fun submitQuery(textInput: String?, searchType: SearchFragment.SearchType)
    {
        if (textInput != null)
        {
            when (searchType)
            {
                SearchFragment.SearchType.USER -> searchUser(textInput)
                SearchFragment.SearchType.TAG -> TODO()
            }
        }
    }


    private fun searchUser(query: String)
    {
        Timber.d("SearchUser $query")

        val q = Firebase.database.reference.child(DatabaseFields.USERS_NAME)
            .orderByChild(DatabaseFields.USERS_FIELD_USERNAME)
            .startAt(query) //this API is a fucking joke...
            .endAt(query + "\uf8ff")

        q.addListenerForSingleValueEvent(
            object : ValueEventListener
            {
                override fun onDataChange(snapshot: DataSnapshot)
                {
                    val u = mutableListOf<User>()
                    snapshot.children.forEach { dataSnapshot ->
                        Timber.d("Iterating $dataSnapshot")
                        dataSnapshot.getValue(User::class.java)?.let { user ->
                            u.add(user)
                        }
                    }
                    _users.value = u
                }

                override fun onCancelled(error: DatabaseError)
                {
                }

            }
        )
    }
}