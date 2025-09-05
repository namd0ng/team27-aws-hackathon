package com.hackathon.alcolook.data.repository

import com.hackathon.alcolook.data.model.UserProfile
import com.hackathon.alcolook.data.model.Gender
import com.hackathon.alcolook.data.network.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileApiRepository @Inject constructor(
    private val userProfileApiService: UserProfileApiService
) {
    
    suspend fun getUserProfile(userId: String): Result<UserProfile> {
        return try {
            val response = userProfileApiService.getUserProfile(userId)
            if (response.isSuccessful) {
                response.body()?.let { profileResponse ->
                    val userProfile = UserProfile(
                        sex = when (profileResponse.sex) {
                            "MALE" -> Gender.MALE
                            "FEMALE" -> Gender.FEMALE
                            else -> Gender.UNSET
                        },
                        isSenior65 = profileResponse.isSenior65,
                        weeklyGoalStdDrinks = profileResponse.weeklyGoalStdDrinks
                    )
                    Result.success(userProfile)
                } ?: Result.failure(Exception("응답 본문이 비어있습니다"))
            } else {
                Result.failure(Exception("프로필 조회 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateUserProfile(userId: String, userProfile: UserProfile): Result<UserProfile> {
        return try {
            val request = UserProfileRequest(
                sex = userProfile.sex.name,
                isSenior65 = userProfile.isSenior65,
                weeklyGoalStdDrinks = userProfile.weeklyGoalStdDrinks
            )
            
            val response = userProfileApiService.updateUserProfile(userId, request)
            if (response.isSuccessful) {
                response.body()?.let { profileResponse ->
                    val updatedProfile = UserProfile(
                        sex = when (profileResponse.sex) {
                            "MALE" -> Gender.MALE
                            "FEMALE" -> Gender.FEMALE
                            else -> Gender.UNSET
                        },
                        isSenior65 = profileResponse.isSenior65,
                        weeklyGoalStdDrinks = profileResponse.weeklyGoalStdDrinks
                    )
                    Result.success(updatedProfile)
                } ?: Result.failure(Exception("응답 본문이 비어있습니다"))
            } else {
                Result.failure(Exception("프로필 업데이트 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
