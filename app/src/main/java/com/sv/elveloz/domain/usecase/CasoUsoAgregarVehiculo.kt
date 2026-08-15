package com.sv.elveloz.domain.usecase

import com.sv.elveloz.data.local.entity.CarEntity
import com.sv.elveloz.domain.repository.CarRepository

class CasoUsoAgregarVehiculo(private val repository: CarRepository) {
    suspend operator fun invoke(vehiculo: CarEntity) {
        repository.addCar(vehiculo)
    }
}