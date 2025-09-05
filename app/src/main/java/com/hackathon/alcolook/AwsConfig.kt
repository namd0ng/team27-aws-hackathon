package com.hackathon.alcolook

import android.content.Context
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.rekognition.AmazonRekognitionClient
import java.util.Properties

object AwsConfig {
    const val TEST_MODE = false
    
    private fun loadCredentials(context: Context): Pair<String, String>? {
        return try {
            val properties = Properties()
            context.assets.open("aws-credentials.properties").use { inputStream ->
                properties.load(inputStream)
            }
            val accessKey = properties.getProperty("aws.access.key")
            val secretKey = properties.getProperty("aws.secret.key")
            if (accessKey != null && secretKey != null) {
                Pair(accessKey, secretKey)
            } else null
        } catch (e: Exception) {
            null
        }
    }
    
    fun getRekognitionClient(context: Context): AmazonRekognitionClient? {
        return if (TEST_MODE) {
            null
        } else {
            val credentials = loadCredentials(context)
            if (credentials != null) {
                val awsCredentials = BasicAWSCredentials(credentials.first, credentials.second)
                val client = AmazonRekognitionClient(awsCredentials)
                client.setRegion(Region.getRegion(Regions.US_EAST_1))
                client
            } else null
        }
    }
}
