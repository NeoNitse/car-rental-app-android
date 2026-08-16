package com.sv.elveloz.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rentals")
data class RentalEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val carId: Int = 0,
    val customerName: String = "",
    val pickupDateMs: Long = 0L,
    val returnDateMs: Long = 0L,
    val totalCost: Double = 0.0,
    val isActive: Boolean = true,
    val estado: String = "SOLICITADA" // SOLICITADA, APROBADA, FINALIZADA, RECHAZADA
)