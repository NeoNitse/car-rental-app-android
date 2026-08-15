package com.sv.elveloz.data.local.entity

import com.sv.elveloz.domain.model.CarStatus
import java.util.UUID

data class CarEntity(
    var id: String = UUID.randomUUID().toString(),
    var brand: String = "",
    var model: String = "",
    var pricePerDay: Double = 0.0,
    var status: CarStatus = CarStatus.DISPONIBLE
)