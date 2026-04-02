package fr.leboncoin.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import fr.leboncoin.core.database.dao.AlbumDao
import fr.leboncoin.core.database.model.AlbumEntity

@Database(
    entities = [AlbumEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun albumDao(): AlbumDao
}
