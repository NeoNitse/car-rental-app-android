package com.sv.elveloz.data.local

import androidx.room.TypeConverter
import com.sv.elveloz.domain.model.CarStatus
import com.sv.elveloz.domain.model.RolUsuario

class Converters {

    @TypeConverter
    fun fromCarStatus(status: CarStatus): String = status.name

    @TypeConverter
    fun toCarStatus(value: String): CarStatus = CarStatus.valueOf(value)

    @TypeConverter
    fun fromRolUsuario(rol: RolUsuario): String = rol.name

    @TypeConverter
    fun toRolUsuario(value: String): RolUsuario = RolUsuario.valueOf(value)
}
