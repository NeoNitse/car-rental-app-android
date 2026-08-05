package com.sv.elveloz.domain.usecase

import com.sv.elveloz.domain.model.CarStatus
import com.sv.elveloz.domain.repository.CarRepository

class UpdateCarStatusUseCase(private val repository: CarRepository) {
    suspend operator fun invoke(carId: Int, newStatus: CarStatus) {
        repository.updateCarStatus(carId, newStatus)
    }
}