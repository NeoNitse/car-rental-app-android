package com.sv.elveloz.data.local.entity

import java.util.UUID

data class RentalEntity(
    var id: String = UUID.randomUUID().toString(),
    var carId: String = "",
    var customerName: String = "",
    var pickupDateMs: Long = 0L,
    var returnDateMs: Long = 0L,
    var totalCost: Double = 0.0,
    var isActive: Boolean = true
)