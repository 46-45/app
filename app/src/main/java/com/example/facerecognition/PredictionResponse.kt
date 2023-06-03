package com.example.facerecognition

import com.google.gson.annotations.SerializedName

data class PredictionResponse(
    @SerializedName("name") val name: String
)
