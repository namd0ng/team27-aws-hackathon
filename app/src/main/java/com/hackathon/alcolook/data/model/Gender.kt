package com.hackathon.alcolook.data.model

enum class Gender {
    MALE, FEMALE, UNSET;
    
    fun getDisplayName(): String = when(this) {
        MALE -> "남성"
        FEMALE -> "여성" 
        UNSET -> "미설정"
    }
}