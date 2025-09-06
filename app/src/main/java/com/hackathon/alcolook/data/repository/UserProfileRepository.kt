package com.hackathon.alcolook.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.hackathon.alcolook.data.model.Gender
import com.hackathon.alcolook.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_profile")

@Singleton
class UserProfileRepository @Inject constructor(
    private val context: Context
) {
    
    private object PreferencesKeys {
        val GENDER = stringPreferencesKey("gender")
        val IS_SENIOR_65 = booleanPreferencesKey("is_senior_65")
        val WEEKLY_GOAL_STD_DRINKS = intPreferencesKey("weekly_goal_std_drinks")
    }
    
    val userProfile: Flow<UserProfile> = context.dataStore.data.map { preferences ->
        UserProfile(
            sex = try {
                Gender.valueOf(preferences[PreferencesKeys.GENDER] ?: Gender.UNSET.name)
            } catch (e: IllegalArgumentException) {
                Gender.UNSET
            },
            isSenior65 = preferences[PreferencesKeys.IS_SENIOR_65] ?: false,
            weeklyGoalStdDrinks = preferences[PreferencesKeys.WEEKLY_GOAL_STD_DRINKS]
        )
    }
    
    suspend fun updateGender(gender: Gender) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.GENDER] = gender.name
        }
    }
    
    suspend fun updateIsSenior65(isSenior: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_SENIOR_65] = isSenior
        }
    }
    
    suspend fun updateWeeklyGoal(stdDrinks: Int?) {
        context.dataStore.edit { preferences ->
            if (stdDrinks != null) {
                preferences[PreferencesKeys.WEEKLY_GOAL_STD_DRINKS] = stdDrinks
            } else {
                preferences.remove(PreferencesKeys.WEEKLY_GOAL_STD_DRINKS)
            }
        }
    }
    
    suspend fun clearAllData() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
    
    suspend fun getUserProfile(): UserProfile {
        return context.dataStore.data.map { preferences ->
            UserProfile(
                sex = try {
                    Gender.valueOf(preferences[PreferencesKeys.GENDER] ?: Gender.UNSET.name)
                } catch (e: IllegalArgumentException) {
                    Gender.UNSET
                },
                isSenior65 = preferences[PreferencesKeys.IS_SENIOR_65] ?: false,
                weeklyGoalStdDrinks = preferences[PreferencesKeys.WEEKLY_GOAL_STD_DRINKS]
            )
        }.first()
    }
    
    suspend fun saveUserProfile(userProfile: UserProfile) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.GENDER] = userProfile.sex.name
            preferences[PreferencesKeys.IS_SENIOR_65] = userProfile.isSenior65
            if (userProfile.weeklyGoalStdDrinks != null) {
                preferences[PreferencesKeys.WEEKLY_GOAL_STD_DRINKS] = userProfile.weeklyGoalStdDrinks
            } else {
                preferences.remove(PreferencesKeys.WEEKLY_GOAL_STD_DRINKS)
            }
        }
    }
}
