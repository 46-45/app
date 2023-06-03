package com.example.facerecognition

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ImagePredictionAPI {
    @Multipart
    @POST("predict")
    fun uploadImage(
        @Part image: MultipartBody.Part
    ): Call<PredictionResponse>
}