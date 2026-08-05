package com.sv.elveloz.domain.usecase

import com.sv.elveloz.domain.repository.CarRepository

class GetCarsUseCase(private val repository: CarRepository) {
    operator fun invoke() = repository.getAllCars()
}