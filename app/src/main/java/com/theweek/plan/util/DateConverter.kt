package com.theweek.plan.util

import androidx.room.TypeConverter
import java.util.Date

/**
 * Type converters to allow Room to reference complex data types.
 */
class DateConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
