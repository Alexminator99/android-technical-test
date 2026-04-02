package fr.leboncoin.core.database.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.leboncoin.core.database.AppDatabase
import fr.leboncoin.core.database.dao.AlbumDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "leboncoin.db")
            .build()

    @Provides
    fun provideAlbumDao(database: AppDatabase): AlbumDao = database.albumDao()
}
