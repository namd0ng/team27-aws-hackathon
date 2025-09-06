package com.hackathon.alcolook.data.model

data class FaceBox(
    val left: Float,
    val top: Float,
    val width: Float,
    val height: Float,
    val drunkPercentage: Int,
    val personId: String
)

data class DrunkDetectionResult(
    val drunkPercentage: Float,
    val message: String,
    val faceBoxes: List<FaceBox>
)
