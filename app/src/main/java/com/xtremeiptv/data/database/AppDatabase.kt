package com.xtremeiptv.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.xtremeiptv.data.database.dao.FavoriteDao
import com.xtremeiptv.data.database.dao.ProfileDao
import com.xtremeiptv.data.database.dao.ResumePositionDao
import com.xtremeiptv.data.database.entity.Favorite
import com.xtremeiptv.data.database.entity.Profile
import com.xtremeiptv.data.database.entity.ResumePosition

@Database(
    entities = [Profile::class, Favorite::class, ResumePosition::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun resumePositionDao(): ResumePositionDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "xtremeiptv.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
