package com.sv.elveloz.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rentals")
data class RentalEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val carId: Int,
    val customerName: String,
    val pickupDateMs: Long,
    val returnDateMs: Long,
    val totalCost: Double,
    val isActive: Boolean = true
)