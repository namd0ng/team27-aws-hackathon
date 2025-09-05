package com.hackathon.alcolook

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.rekognition.AmazonRekognitionClient

object AwsConfig {
    // 테스트 모드 - 실제 AWS 연결 없이 테스트 가능
    const val TEST_MODE = true
    
    // TODO: 실제 배포 시 아래 자격 증명을 실제 값으로 교체하고 TEST_MODE를 false로 변경
    private const val ACCESS_KEY = "YOUR_ACCESS_KEY"
    private const val SECRET_KEY = "YOUR_SECRET_KEY"
    
    fun getRekognitionClient(): AmazonRekognitionClient? {
        return if (TEST_MODE) {
            null // 테스트 모드에서는 null 반환
        } else {
            val credentials = BasicAWSCredentials(ACCESS_KEY, SECRET_KEY)
            val client = AmazonRekognitionClient(credentials)
            client.setRegion(Region.getRegion(Regions.US_EAST_1))
            client
        }
    }
}
