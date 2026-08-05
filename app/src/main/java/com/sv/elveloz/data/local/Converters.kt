package com.sv.elveloz.data.local

import androidx.room.TypeConverter
import com.sv.elveloz.domain.model.CarStatus

class Converters {

    @TypeConverter
    fun fromCarStatus(status: CarStatus): String {
        return status.name
    }

    @TypeConverter
    fun toCarStatus(value: String): CarStatus {
        return CarStatus.valueOf(value)
    }
}