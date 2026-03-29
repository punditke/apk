package com.xtremeiptv.data.network.api

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface MacApi {
    @GET("c/")
    suspend fun authenticate(
        @Header("Authorization") auth: String,
        @Query("type") type: String = "stb"
    ): String
    
    @GET("c/get_channels")
    suspend fun getChannels(@Header("Authorization") token: String): String
    
    @GET("c/get_vod")
    suspend fun getVod(@Header("Authorization") token: String): String
}
