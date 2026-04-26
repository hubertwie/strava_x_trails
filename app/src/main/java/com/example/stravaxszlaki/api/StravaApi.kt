package com.example.stravaxszlaki.api

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface StravaApi {
    @POST("oauth/token")
    suspend fun exchangeToken(
        @Query("client_id") clientId: String,
        @Query("client_secret") clientSecret: String,
        @Query("code") code: String,
        @Query("grant_type") grantType: String = "authorization_code"
    ): StravaTokenResponse

    @GET("api/v3/athlete/activities")
    suspend fun getActivities(
        @Header("Authorization") authHeader: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 200
    ): List<StravaActivity>

    @GET("api/v3/activities/{id}")
    suspend fun getActivityDetails(
        @Header("Authorization") authHeader: String,
        @Path("id") activityId: Long
    ): StravaDetailedActivity
}