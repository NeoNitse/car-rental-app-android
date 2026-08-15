package com.sv.elveloz.domain.repository

import com.sv.elveloz.data.local.entity.CarEntity
import com.sv.elveloz.data.local.entity.RentalEntity
import com.sv.elveloz.domain.model.CarStatus
import kotlinx.coroutines.flow.Flow

interface CarRepository {

    fun getAllCars(): Flow<List<CarEntity>>

    fun getAllRentals(): Flow<List<RentalEntity>>

    suspend fun getActiveRentalForCar(carId: Int): RentalEntity?

    suspend fun rentCar(rental: RentalEntity, carId: Int)

    suspend fun updateCarStatus(carId: Int, newStatus: CarStatus)

    suspend fun completeRental(rental: RentalEntity, carId: Int)

    suspend fun cancelRental(rental: RentalEntity, carId: Int)

    suspend fun updateRental(rental: RentalEntity)

    fun getPendingRentals(): Flow<List<RentalEntity>>
}