package com.myniprojects.pixagram.vm

import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.myniprojects.pixagram.adapters.searchadapter.SearchModel
import com.myniprojects.pixagram.model.Tag
import com.myniprojects.pixagram.model.User
import com.myniprojects.pixagram.ui.fragments.SearchFragment
import com.myniprojects.pixagram.utils.DatabaseFields
import com.myniprojects.pixagram.utils.formatQuery
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(): ViewModel()
{
    private val _searchResult: MutableStateFlow<List<SearchModel>> = MutableStateFlow(listOf())
    val searchResult: StateFlow<List<SearchModel>> = _searchResult

    fun submitQuery(query: String?, searchType: SearchFragment.SearchType)
    {
        if (query != null)
        {
            when (searchType)
            {
                SearchFragment.SearchType.USER -> searchUser(query)
                SearchFragment.SearchType.TAG -> searchTag(query)
            }
        }
    }

    private fun searchTag(query: String)
    {
        Timber.d("SearchTag $query")

        val qf = query.formatQuery()

        val q = Firebase.database.reference.child(DatabaseFields.HASHTAGS_NAME)
            .orderByKey()
            .startAt(qf)
            .endAt(qf + "\uf8ff")

        q.addListenerForSingleValueEvent(
            object : ValueEventListener
            {
                override fun onDataChange(snapshot: DataSnapshot)
                {
                    val u = mutableListOf<SearchModel>()

                    snapshot.children.forEach { dataSnapshot ->
                        Timber.d("Iterating $dataSnapshot")

                        dataSnapshot.key?.let { key ->
                            u.add(SearchModel.TagItem(Tag(key, dataSnapshot.childrenCount)))
                        }
                    }

                    _searchResult.value = u
                }

                override fun onCancelled(error: DatabaseError)
                {
                    Timber.d("SearchUser $query was canceled")
                }

            }
        )
    }


    private fun searchUser(query: String)
    {
        Timber.d("SearchUser $query")

        val qf = query.formatQuery()

        val q = Firebase.database.reference.child(DatabaseFields.USERS_NAME)
            .orderByChild(DatabaseFields.USERS_FIELD_USERNAME)
            .startAt(qf) //this API is a fucking joke...
            .endAt(qf + "\uf8ff")

        q.addListenerForSingleValueEvent(
            object : ValueEventListener
            {
                override fun onDataChange(snapshot: DataSnapshot)
                {
                    val u = mutableListOf<SearchModel>()

                    snapshot.children.forEach { dataSnapshot ->
                        Timber.d("Iterating $dataSnapshot")
                        dataSnapshot.getValue(User::class.java)?.let { user ->
                            u.add(SearchModel.UserItem(user))
                        }
                    }

                    _searchResult.value = u
                }

                override fun onCancelled(error: DatabaseError)
                {
                    Timber.d("SearchUser $query was canceled")
                }

            }
        )
    }


}