package com.xtremeiptv.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileStorage @Inject constructor(@ApplicationContext private val context: Context) {
    
    private val cacheDir = context.cacheDir
    
    suspend fun saveM3uFile(content: String, fileName: String): String = withContext(Dispatchers.IO) {
        val file = File(cacheDir, fileName)
        file.writeText(content)
        file.absolutePath
    }
    
    suspend fun readM3uFile(path: String): String = withContext(Dispatchers.IO) {
        File(path).readText()
    }
    
    fun deleteFile(path: String): Boolean {
        return File(path).delete()
    }
    
    fun fileExists(path: String): Boolean {
        return File(path).exists()
    }
}
