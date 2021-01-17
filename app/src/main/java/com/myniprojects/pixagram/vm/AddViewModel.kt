package com.myniprojects.pixagram.vm

import android.app.Application
import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.myniprojects.pixagram.adapters.imageadapter.Image
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class AddViewModel @ViewModelInject constructor(
    application: Application
) : AndroidViewModel(application)
{
    private val _allImagesFromGallery: MutableStateFlow<List<Uri>> = MutableStateFlow(listOf())

    private val _selectedImage: MutableStateFlow<Uri?> = MutableStateFlow(null)
    private val _capturedImage: MutableStateFlow<Uri?> = MutableStateFlow(null)

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

        val cursor = getApplication<Application>().contentResolver.query(
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
}