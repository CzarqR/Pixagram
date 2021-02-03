package com.myniprojects.pixagram.vm

import android.app.Application
import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.adapters.imageadapter.Image
import com.myniprojects.pixagram.utils.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*

@HiltViewModel
class AddViewModel(
    application: Application
) : AndroidViewModel(application)
{
    private val _allImagesFromGallery: MutableStateFlow<List<Uri>> = MutableStateFlow(listOf())

    private val _selectedImage: MutableStateFlow<Uri?> = MutableStateFlow(null)
    private val _capturedImage: MutableStateFlow<Uri?> = MutableStateFlow(null)

    private val _isUploading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading

    private val _uploadingMsg: MutableStateFlow<Event<String>?> = MutableStateFlow(null)
    val uploadingMsg: StateFlow<Event<String>?> = _uploadingMsg

    val previewImage: Flow<Uri?> = _selectedImage.combine(
        _capturedImage
    ) { gallery, captured ->
        Timber.d("Combined ${gallery ?: captured}")
        gallery ?: captured
    }

    val allImagesFromGallery = _allImagesFromGallery.combine(
        _selectedImage
    ) { all, selected ->
        if (selected != null)
        {
            all.map {
                Image(it, it == selected)
            }
        }
        else
        {
            all.map {
                Image(it, false)
            }
        }
    }

    fun selectImageFromGallery(uri: Uri)
    {
        _capturedImage.value = null
        _selectedImage.value = uri
    }

    fun captureImage(uri: Uri)
    {
        _capturedImage.value = uri
        _selectedImage.value = null
    }

    fun unSelectImage()
    {
        _selectedImage.value = null
        _capturedImage.value = null
    }

    private fun getAllImages(): List<Uri>
    {
        val allImages = mutableListOf<Uri>()

        val imageProjection = arrayOf(
            MediaStore.Images.Media._ID
        )

        val imageSortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        val cursor = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            imageProjection,
            null,
            null,
            imageSortOrder
        )

        cursor.use {

            if (cursor != null)
            {
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                while (cursor.moveToNext())
                {
                    allImages.add(
                        ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            cursor.getLong(idColumn)
                        )
                    )
                }
            }
            else
            {
                Timber.d("Cursor is null!")
            }
        }
        return allImages
    }

    fun loadAllImages()
    {
        viewModelScope.launch {
            _allImagesFromGallery.value = withContext(Dispatchers.IO) {
                getAllImages()
            }
        }
    }

    fun postImage(
        uri: Uri,
        desc: String,
        hashtags: List<String>,
        mentions: List<String>,
    )
    {
        _isUploading.value = true

        val user = Firebase.auth.currentUser
        if (user != null)
        {
            val currentTime = System.currentTimeMillis()
            val storagePostsRef = Firebase.storage.getReference(StorageFields.LOCATION_POST)
                .child("${user.uid}_${currentTime}.${uri.getFileExt(context.contentResolver)}")

            val storageTask = storagePostsRef.putFile(uri)

            storageTask.continueWithTask {
                if (it.isSuccessful)
                {
                    storagePostsRef.downloadUrl
                }
                else
                {
                    _isUploading.value = false
                    throw Exception(it.exception)
                }
            }.addOnSuccessListener {
                Timber.d("Success upload $it")

                val dbRefPosts = Firebase.database.getReference(DatabaseFields.POSTS_NAME)
                val postId = dbRefPosts.push().key ?: "${user.uid}_${currentTime}"

                val post = hashMapOf(
                    DatabaseFields.POSTS_FIELD_DESC to desc,
                    DatabaseFields.POSTS_FIELD_OWNER to user.uid,
                    DatabaseFields.POSTS_FIELD_IMAGE_URL to it.toString(),
                    DatabaseFields.POSTS_FIELD_TIME to System.currentTimeMillis()
                )

                dbRefPosts.child(postId).setValue(post)
                    .addOnSuccessListener {
                        Timber.d("Success saved in db")

                        if (hashtags.isNotEmpty())
                        {
                            Timber.d("Saving hashtags")
                            val hashTagDbRef = Firebase.database.reference.child(DatabaseFields.HASHTAGS_NAME)

                            hashtags.forEach { tag ->
                                val tagRef = hashTagDbRef.child(tag.toLowerCase(Locale.getDefault()))

                                val key = tagRef.push().key

                                key?.let {
                                    val h = mapOf(
                                        key to postId
                                    )
                                    tagRef.updateChildren(h)
                                }
                            }
                        }
                        else
                        {
                            Timber.d("No hashtags")
                        }

                        if (mentions.isNotEmpty())
                        {
                            Timber.d("Saving mentions")
                            val hashTagDbRef = Firebase.database.reference.child(DatabaseFields.MENTIONS_NAME)

                            mentions.forEach { mention ->
                                val mentionRef = hashTagDbRef.child(mention.toLowerCase(Locale.getDefault()))

                                val key = mentionRef.push().key

                                key?.let {
                                    val h = mapOf(
                                        key to postId
                                    )
                                    mentionRef.updateChildren(h)
                                }
                            }
                        }
                        else
                        {
                            Timber.d("No mentions")
                        }

                        _uploadingMsg.value = Event(context.getString(R.string.post_was_uploaded))
                    }
                    .addOnFailureListener { exception ->
                        Timber.d("Failed to save in db")
                        _uploadingMsg.value = Event(
                            context.getString(
                                R.string.post_was_not_uploaded,
                                exception.localizedMessage
                            )
                        )
                    }
                    .addOnCompleteListener {
                        _isUploading.value = false
                    }


            }.addOnFailureListener { exception ->
                Timber.d("Failed upload ${exception.message}")
                _uploadingMsg.value = Event(
                    context.getString(
                        R.string.post_was_not_uploaded,
                        exception.localizedMessage
                    )
                )
            }
        }
        else
        {
            Timber.d("User was null. Cannot upload image")
            _uploadingMsg.value = Event(
                context.getString(
                    R.string.post_was_not_uploaded_no_user
                )
            )
        }
    }


}