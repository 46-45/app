package com.example.facerecognition

import com.google.gson.annotations.SerializedName

data class PredictionResponse(
    @SerializedName("result") val result:String,
    @SerializedName("name") val name: String,
    @SerializedName("code") val code:Int
)
