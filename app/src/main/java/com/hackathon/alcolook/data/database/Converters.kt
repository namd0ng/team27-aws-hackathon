package com.hackathon.alcolook.data.database

import androidx.room.TypeConverter
import com.hackathon.alcolook.data.model.DrinkType
import com.hackathon.alcolook.data.model.DrinkUnit
import com.hackathon.alcolook.data.model.Gender
import java.time.LocalDate

class Converters {
    
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.toString()
    }
    
    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? {
        return dateString?.let { LocalDate.parse(it) }
    }
    
    @TypeConverter
    fun fromDrinkType(type: DrinkType): String {
        return type.name
    }
    
    @TypeConverter
    fun toDrinkType(typeName: String): DrinkType {
        return DrinkType.valueOf(typeName)
    }
    
    @TypeConverter
    fun fromDrinkUnit(unit: DrinkUnit): String {
        return unit.name
    }
    
    @TypeConverter
    fun toDrinkUnit(unitName: String): DrinkUnit {
        return DrinkUnit.valueOf(unitName)
    }
    
    @TypeConverter
    fun fromGender(gender: Gender): String {
        return gender.name
    }
    
    @TypeConverter
    fun toGender(genderName: String): Gender {
        return Gender.valueOf(genderName)
    }
}