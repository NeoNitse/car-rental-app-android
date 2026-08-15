package com.sv.elveloz.domain.usecase

import com.sv.elveloz.data.local.entity.RentalEntity
import com.sv.elveloz.domain.repository.CarRepository

class RechazarReservaUseCase(private val repository: CarRepository) {
    suspend operator fun invoke(rental: RentalEntity, carId: Int) {
        val updatedRental = rental.copy(estado = "RECHAZADA", isActive = false)
        repository.updateRental(updatedRental)
        repository.updateCarStatus(carId, com.sv.elveloz.domain.model.CarStatus.DISPONIBLE)
    }
}