package com.xtremeiptv.di

import android.content.Context
import androidx.room.Room
import com.xtremeiptv.data.database.AppDatabase
import com.xtremeiptv.data.local.EncryptedPrefs
import com.xtremeiptv.data.local.FileStorage
import com.xtremeiptv.data.network.protocol.M3uParser
import com.xtremeiptv.data.network.protocol.MacClient
import com.xtremeiptv.data.network.protocol.ProtocolDetector
import com.xtremeiptv.data.network.protocol.StalkerClient
import com.xtremeiptv.data.network.protocol.XtreamClient
import com.xtremeiptv.data.repository.ContentRepository
import com.xtremeiptv.data.repository.PlayerRepository
import com.xtremeiptv.data.repository.ProfileRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }
    
    @Provides
    @Singleton
    fun provideEncryptedPrefs(@ApplicationContext context: Context): EncryptedPrefs {
        return EncryptedPrefs(context)
    }
    
    @Provides
    @Singleton
    fun provideFileStorage(@ApplicationContext context: Context): FileStorage {
        return FileStorage(context)
    }
    
    @Provides
    @Singleton
    fun provideProtocolDetector(): ProtocolDetector {
        return ProtocolDetector()
    }
    
    @Provides
    @Singleton
    fun provideM3uParser(): M3uParser {
        return M3uParser()
    }
    
    @Provides
    @Singleton
    fun provideXtreamClient(): XtreamClient {
        return XtreamClient()
    }
    
    @Provides
    @Singleton
    fun provideStalkerClient(): StalkerClient {
        return StalkerClient()
    }
    
    @Provides
    @Singleton
    fun provideMacClient(): MacClient {
        return MacClient()
    }
    
    @Provides
    @Singleton
    fun provideProfileRepository(
        database: AppDatabase,
        encryptedPrefs: EncryptedPrefs,
        protocolDetector: ProtocolDetector
    ): ProfileRepository {
        return ProfileRepository(database, encryptedPrefs, protocolDetector)
    }
    
    @Provides
    @Singleton
    fun provideContentRepository(
        database: AppDatabase,
        m3uParser: M3uParser,
        xtreamClient: XtreamClient,
        stalkerClient: StalkerClient,
        macClient: MacClient
    ): ContentRepository {
        return ContentRepository(database, m3uParser, xtreamClient, stalkerClient, macClient)
    }
    
    @Provides
    @Singleton
    fun providePlayerRepository(): PlayerRepository {
        return PlayerRepository()
    }
}
