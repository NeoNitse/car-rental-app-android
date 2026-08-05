package com.sv.elveloz.domain.usecase

import com.sv.elveloz.domain.repository.CarRepository

class GetActiveRentalUseCase(private val repository: CarRepository) {
    suspend operator fun invoke(carId: Int) = repository.getActiveRentalForCar(carId)
}