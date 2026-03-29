package com.xtremeiptv.data.network.api

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface StalkerApi {
    @FormUrlEncoded
    @POST("stalker_portal/api/v1/auth")
    suspend fun authenticate(
        @Field("login") login: String,
        @Field("password") password: String,
        @Field("mac") mac: String
    ): String
    
    @GET("stalker_portal/api/v1/channels")
    suspend fun getChannels(@Query("token") token: String): String
    
    @GET("stalker_portal/api/v1/vod")
    suspend fun getVod(@Query("token") token: String): String
    
    @GET("stalker_portal/api/v1/series")
    suspend fun getSeries(@Query("token") token: String): String
}
