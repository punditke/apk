package com.xtremeiptv.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.xtremeiptv.data.database.dao.CachedContentDao
import com.xtremeiptv.data.database.dao.FavoriteDao
import com.xtremeiptv.data.database.dao.ProfileDao
import com.xtremeiptv.data.database.dao.ResumePositionDao
import com.xtremeiptv.data.database.entity.CachedContent
import com.xtremeiptv.data.database.entity.Favorite
import com.xtremeiptv.data.database.entity.Profile
import com.xtremeiptv.data.database.entity.ResumePosition

@Database(
    entities = [Profile::class, Favorite::class, ResumePosition::class, CachedContent::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun resumePositionDao(): ResumePositionDao
    abstract fun cachedContentDao(): CachedContentDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "xtremeiptv.db"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
