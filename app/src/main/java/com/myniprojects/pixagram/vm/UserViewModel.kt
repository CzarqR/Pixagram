package com.myniprojects.pixagram.vm

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.myniprojects.pixagram.model.User
import com.myniprojects.pixagram.utils.DatabaseFields
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber

class UserViewModel : ViewModel()
{
    val followedType = object : GenericTypeIndicator<HashMap<String, String>?>()
    {}

    private lateinit var currentUserFollowersDbRef: DatabaseReference
    private lateinit var selectedUserFollowedByDbRef: DatabaseReference
    private lateinit var selectedUserFollowingDbRef: DatabaseReference

    private val _selectedUser = MutableStateFlow(User())
    val selectedUser: StateFlow<User> = _selectedUser

    private val _selectedUserFollowers: MutableStateFlow<HashMap<String, String>?> =
            MutableStateFlow(null)
    val selectedUserFollowers: StateFlow<HashMap<String, String>?> = _selectedUserFollowers

    private val _selectedUserFollowing: MutableStateFlow<HashMap<String, String>?> =
            MutableStateFlow(null)
    val selectedUserFollowing: StateFlow<HashMap<String, String>?> = _selectedUserFollowing

    private val _loggedUserFollows: MutableStateFlow<List<String>> = MutableStateFlow(listOf())
    val loggedUserFollows: StateFlow<List<String>> = _loggedUserFollows

    fun initUser(user: User)
    {
        _selectedUser.value = user

        selectedUserFollowedByDbRef = Firebase.database
            .getReference(DatabaseFields.FOLLOWED_BY_NAME)
            .child(user.id)

        selectedUserFollowedByDbRef.addValueEventListener(
            object : ValueEventListener
            {
                override fun onDataChange(dataSnapshot: DataSnapshot)
                {
                    dataSnapshot.getValue(followedType)?.let { followers ->
                        Timber.d("Selected user followers: $followers")
                        _selectedUserFollowers.value = followers
                    }
                }

                override fun onCancelled(databaseError: DatabaseError)
                {
                    Timber.d("Loading selected user followers cancelled. ${databaseError.toException()}")
                }
            }
        )

        selectedUserFollowingDbRef = Firebase.database
            .getReference(DatabaseFields.FOLLOWING_NAME)
            .child(user.id)

        selectedUserFollowingDbRef.addValueEventListener(
            object : ValueEventListener
            {
                override fun onDataChange(dataSnapshot: DataSnapshot)
                {
                    dataSnapshot.getValue(followedType)?.let { following ->
                        Timber.d("Users that selected user follows: $following")
                        _selectedUserFollowing.value = following
                    }
                }

                override fun onCancelled(databaseError: DatabaseError)
                {
                    Timber.d("Loading followers for current user cancelled. ${databaseError.toException()}")
                }
            }
        )
    }


    init
    {
        Timber.d("Init VM")

        val loggedUser = Firebase.auth.currentUser

        if (loggedUser != null)
        {
            currentUserFollowersDbRef = Firebase.database
                .getReference(DatabaseFields.FOLLOWING_NAME)
                .child(loggedUser.uid)

            currentUserFollowersDbRef.addValueEventListener(
                object : ValueEventListener
                {
                    override fun onDataChange(dataSnapshot: DataSnapshot)
                    {
                        dataSnapshot.getValue(followedType)?.let { followers ->
                            Timber.d("New followers: $followers")
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError)
                    {
                        Timber.d("Loading followers for current user cancelled. ${databaseError.toException()}")
                    }
                }
            )
        }
        else
        {
            Timber.d("Something went wrong, user is not logged")
        }
    }

    fun follow()
    {
        val loggedUser = Firebase.auth.currentUser

        loggedUser?.let { currentUser ->

            val keyFollowers = currentUserFollowersDbRef.push().key
            val keyFollowedBy = selectedUserFollowedByDbRef.push().key

            if (keyFollowedBy != null && keyFollowers != null)
            {
                val h1 = mapOf(
                    keyFollowers to _selectedUser.value.id
                )
                currentUserFollowersDbRef.updateChildren(h1)

                val h2 = mapOf(
                    keyFollowedBy to currentUser.uid
                )

                selectedUserFollowedByDbRef.updateChildren(h2)
            }
            else
            {
                Timber.d("Error, not followed")
            }
        }
    }
}