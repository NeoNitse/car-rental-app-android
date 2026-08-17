package com.sv.elveloz.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.sv.elveloz.data.local.Converters
import com.sv.elveloz.domain.model.CarStatus

@Entity(tableName = "cars")
@TypeConverters(Converters::class)
data class CarEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val brand: String = "",
    val model: String = "",
    val pricePerDay: Double = 0.0,
    val status: CarStatus = CarStatus.DISPONIBLE,
    val imageResName: String = "",
    val imageUrl: String = "",
    val location: String = "",
    val rating: Double = 5.0
)