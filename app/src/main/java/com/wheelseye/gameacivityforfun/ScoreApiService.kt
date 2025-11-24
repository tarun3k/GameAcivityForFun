package com.wheelseye.gameacivityforfun

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

data class ScoreRequest(
    val playerId: String,
    val gameType: String,
    val result: String, // "win", "loss", "draw"
    val opponentType: String // "robot" or "human"
)

data class ScoreResponse(
    val success: Boolean,
    val message: String
)

interface ScoreApi {
    @POST("score")
    suspend fun updateScore(@Body request: ScoreRequest): ScoreResponse
}

object ScoreApiService {
    // Using a mock API endpoint - replace with your actual API URL
    private const val BASE_URL = "https://jsonplaceholder.typicode.com/" // Mock API
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val api: ScoreApi = retrofit.create(ScoreApi::class.java)
    
    // Mock implementation for testing
    suspend fun updateScoreMock(request: ScoreRequest): ScoreResponse {
        // Simulate API call delay
        kotlinx.coroutines.delay(500)
        return ScoreResponse(
            success = true,
            message = "Score updated: ${request.playerId} - ${request.result} against ${request.opponentType}"
        )
    }
}

