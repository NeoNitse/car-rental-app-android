package com.sv.elveloz.data.repository

import com.sv.elveloz.data.local.dao.CarDao
import com.sv.elveloz.data.local.dao.RentalDao
import com.sv.elveloz.data.local.entity.RentalEntity
import com.sv.elveloz.domain.model.CarStatus
import com.sv.elveloz.domain.repository.CarRepository

class CarRepositoryImpl(
    private val carDao: CarDao,
    private val rentalDao: RentalDao
) : CarRepository {

    override fun getAllCars() = carDao.getAllCars()

    override fun getAllRentals() = rentalDao.getAllRentals()

    override suspend fun getActiveRentalForCar(carId: Int) =
        rentalDao.getActiveRentalForCar(carId)

    override suspend fun rentCar(rental: RentalEntity, carId: Int) {
        rentalDao.insert(rental)
        carDao.updateStatus(carId, CarStatus.EN_PROCESO)
    }

    override suspend fun updateCarStatus(carId: Int, newStatus: CarStatus) {
        carDao.updateStatus(carId, newStatus)
    }

    override suspend fun completeRental(rental: RentalEntity, carId: Int) {
        rentalDao.update(rental.copy(isActive = false))
        carDao.updateStatus(carId, CarStatus.DISPONIBLE)
    }

    override suspend fun cancelRental(rental: RentalEntity, carId: Int) {
        rentalDao.delete(rental.id)
        carDao.updateStatus(carId, CarStatus.DISPONIBLE)
    }
}