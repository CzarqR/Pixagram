package com.myniprojects.pixagram.di

import android.content.Context
import coil.ImageLoader
import coil.decode.SvgDecoder
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.repository.RealtimeDatabaseRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule
{
    @Provides
    @Singleton
    fun provideGlide(
        @ApplicationContext context: Context
    ): RequestManager =
            Glide.with(context).setDefaultRequestOptions(
                RequestOptions()
                    .placeholder(R.drawable.ic_outline_image_24)
                    .error(R.drawable.ic_outline_broken_image_24)
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
            )

    @Provides
    @Singleton
    fun provideImageLoader(
        @ApplicationContext context: Context
    ): ImageLoader = ImageLoader.Builder(context)
        .componentRegistry {
            add(SvgDecoder(context))
        }
        .placeholder(R.drawable.ic_outline_image_24)
        .error(R.drawable.ic_outline_broken_image_24)
        .build()

    @Provides
    @Singleton
    fun provideRealtimeDatabaseRepository(): RealtimeDatabaseRepository =
            RealtimeDatabaseRepository()


}