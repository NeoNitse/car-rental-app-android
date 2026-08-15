package com.sv.elveloz.domain.usecase

import com.sv.elveloz.data.local.entity.RentalEntity
import com.sv.elveloz.domain.repository.CarRepository

class CancelRentalUseCase(private val repository: CarRepository) {
    suspend operator fun invoke(rental: RentalEntity, carId: Int) {
        repository.cancelRental(rental, carId)
    }
}