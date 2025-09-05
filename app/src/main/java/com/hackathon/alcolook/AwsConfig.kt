package com.hackathon.alcolook

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.rekognition.AmazonRekognitionClient

object AwsConfig {
    // TODO: 실제 AWS 자격 증명으로 교체 필요
    private const val ACCESS_KEY = "YOUR_ACCESS_KEY"
    private const val SECRET_KEY = "YOUR_SECRET_KEY"
    
    fun getRekognitionClient(): AmazonRekognitionClient {
        val credentials = BasicAWSCredentials(ACCESS_KEY, SECRET_KEY)
        val client = AmazonRekognitionClient(credentials)
        client.setRegion(Region.getRegion(Regions.US_EAST_1))
        return client
    }
}
