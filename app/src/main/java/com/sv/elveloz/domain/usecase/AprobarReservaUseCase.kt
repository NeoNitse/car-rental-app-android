package com.sv.elveloz.domain.usecase

import com.sv.elveloz.data.local.entity.RentalEntity
import com.sv.elveloz.domain.model.CarStatus
import com.sv.elveloz.domain.repository.CarRepository

class AprobarReservaUseCase(private val repository: CarRepository) {
    suspend operator fun invoke(rental: RentalEntity) {
        val updatedRental = rental.copy(estado = "APROBADA")
        repository.updateRental(updatedRental)
        repository.updateCarStatus(rental.carId, CarStatus.EN_PROCESO)
    }
}
