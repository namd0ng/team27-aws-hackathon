package com.hackathon.alcolook.service

import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class AwsSignatureV4 {
    
    companion object {
        private const val ALGORITHM = "AWS4-HMAC-SHA256"
        private const val TERMINATOR = "aws4_request"
        
        fun sign(
            accessKey: String,
            secretKey: String,
            region: String,
            service: String,
            method: String,
            uri: String,
            queryString: String,
            headers: Map<String, String>,
            payload: String
        ): String {
            val now = Date()
            val dateStamp = SimpleDateFormat("yyyyMMdd", Locale.US).format(now)
            val amzDate = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.format(now)
            
            val credentialScope = "$dateStamp/$region/$service/$TERMINATOR"
            
            // 1. Create canonical request
            val canonicalHeaders = headers.toSortedMap().map { "${it.key.lowercase()}:${it.value.trim()}" }.joinToString("\n") + "\n"
            val signedHeaders = headers.keys.map { it.lowercase() }.sorted().joinToString(";")
            val payloadHash = sha256(payload)
            
            val canonicalRequest = listOf(
                method,
                uri,
                queryString,
                canonicalHeaders,
                signedHeaders,
                payloadHash
            ).joinToString("\n")
            
            // 2. Create string to sign
            val stringToSign = listOf(
                ALGORITHM,
                amzDate,
                credentialScope,
                sha256(canonicalRequest)
            ).joinToString("\n")
            
            // 3. Calculate signature
            val signingKey = getSignatureKey(secretKey, dateStamp, region, service)
            val signature = hmacSha256(stringToSign, signingKey).toHex()
            
            // 4. Create authorization header
            return "$ALGORITHM Credential=$accessKey/$credentialScope, SignedHeaders=$signedHeaders, Signature=$signature"
        }
        
        private fun sha256(data: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
            return digest.digest(data.toByteArray()).toHex()
        }
        
        private fun hmacSha256(data: String, key: ByteArray): ByteArray {
            val mac = Mac.getInstance("HmacSHA256")
            mac.init(SecretKeySpec(key, "HmacSHA256"))
            return mac.doFinal(data.toByteArray())
        }
        
        private fun getSignatureKey(key: String, dateStamp: String, regionName: String, serviceName: String): ByteArray {
            val kDate = hmacSha256(dateStamp, ("AWS4$key").toByteArray())
            val kRegion = hmacSha256(regionName, kDate)
            val kService = hmacSha256(serviceName, kRegion)
            return hmacSha256(TERMINATOR, kService)
        }
        
        private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
    }
}
