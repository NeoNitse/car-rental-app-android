package com.sv.elveloz.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.sv.elveloz.data.local.entity.CarEntity
import com.sv.elveloz.data.local.entity.RentalEntity
import com.sv.elveloz.domain.model.CarStatus
import com.sv.elveloz.domain.repository.CarRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CarRepositoryFirebaseImpl : CarRepository {

    private val db = FirebaseFirestore.getInstance()
    private val carsCollection = db.collection("cars")
    private val rentalsCollection = db.collection("rentals")

    override fun getAllCars(): Flow<List<CarEntity>> = callbackFlow {
        val subscription = carsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                try {
                    val cars = snapshot.toObjects(CarEntity::class.java)
                    if (cars.isEmpty()) {
                        seedInitialCars()
                    } else {
                        trySend(cars)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("CRASH_HUNT", "Error al mapear documentos", e)
                }
            }
        }
        awaitClose { subscription.remove() }
    }

    override fun getAllRentals(): Flow<List<RentalEntity>> = callbackFlow {
        val subscription = rentalsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val rentals = snapshot.toObjects(RentalEntity::class.java)
                trySend(rentals)
            }
        }
        awaitClose { subscription.remove() }
    }

    override suspend fun getActiveRentalForCar(carId: Int): RentalEntity? {
        return try {
            val snapshot = rentalsCollection.get().await()
            val allRentals = snapshot.documents.mapNotNull { it.toObject(RentalEntity::class.java) }

            allRentals
                .filter { it.carId.toString() == carId.toString() }
                .filter { it.estado != "DEVUELTO" && it.estado != "RECHAZADA" }
                .maxByOrNull { it.pickupDateMs ?: 0L }

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun rentCar(rental: RentalEntity, carId: Int) {
        val finalId = if (rental.id == 0) (System.currentTimeMillis() % 1000000000).toInt() else rental.id
        val rentalToSave = rental.copy(id = finalId, estado = "SOLICITADA")

        val batch = db.batch()
        val rentalRef = rentalsCollection.document(finalId.toString())
        val carRef = carsCollection.document(carId.toString())

        batch.set(rentalRef, rentalToSave)
        batch.update(carRef, "status", CarStatus.PEND_APROBACION.name)

        batch.commit().await()
    }

    override suspend fun updateCarStatus(carId: Int, newStatus: CarStatus) {
        carsCollection.document(carId.toString()).update("status", newStatus.name).await()
    }

    override suspend fun completeRental(rental: RentalEntity, carId: Int) {
        val batch = db.batch()
        val rentalRef = rentalsCollection.document(rental.id.toString())
        val carRef = carsCollection.document(carId.toString())

        // Usamos SetOptions.merge() para forzar la actualización sin fallos
        batch.set(rentalRef, mapOf("isActive" to false, "estado" to "DEVUELTO"), SetOptions.merge())
        batch.update(carRef, "status", CarStatus.DISPONIBLE.name)

        batch.commit().await()
    }

    override suspend fun cancelRental(rental: RentalEntity, carId: Int) {
        val batch = db.batch()
        val rentalRef = rentalsCollection.document(rental.id.toString())
        val carRef = carsCollection.document(carId.toString())

        batch.set(rentalRef, mapOf("isActive" to false, "estado" to "RECHAZADA"), SetOptions.merge())
        batch.update(carRef, "status", CarStatus.DISPONIBLE.name)

        batch.commit().await()
    }

    override suspend fun updateRental(rental: RentalEntity) {
        // SOLUCIÓN: Esta función ya no está vacía. Ahora sí actualiza las notificaciones en Firebase.
        rentalsCollection.document(rental.id.toString()).set(rental, SetOptions.merge()).await()
    }

    override fun getPendingRentals(): Flow<List<RentalEntity>> = callbackFlow {
        val subscription = rentalsCollection
            .whereEqualTo("estado", "SOLICITADA")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val rentals = snapshot.toObjects(RentalEntity::class.java)
                    trySend(rentals)
                }
            }
        awaitClose { subscription.remove() }
    }

    private fun seedInitialCars() {
        val initialCars = listOf(
            CarEntity(id = 1, brand = "Toyota", model = "Corolla 2023", pricePerDay = 45.0, status = CarStatus.DISPONIBLE, imageResName = ""),
            CarEntity(id = 2, brand = "Nissan", model = "Sentra 2022", pricePerDay = 40.0, status = CarStatus.DISPONIBLE, imageResName = ""),
            CarEntity(id = 3, brand = "Honda", model = "CR-V 2024", pricePerDay = 65.0, status = CarStatus.DISPONIBLE, imageResName = ""),
            CarEntity(id = 4, brand = "Hyundai", model = "Tucson 2023", pricePerDay = 60.0, status = CarStatus.DISPONIBLE, imageResName = ""),
            CarEntity(id = 5, brand = "Kia", model = "Picanto 2022", pricePerDay = 30.0, status = CarStatus.DISPONIBLE, imageResName = ""),
            CarEntity(id = 6, brand = "Mazda", model = "CX-5 2023", pricePerDay = 70.0, status = CarStatus.DISPONIBLE, imageResName = ""),
            CarEntity(id = 7, brand = "Chevrolet", model = "Aveo 2021", pricePerDay = 35.0, status = CarStatus.DISPONIBLE, imageResName = ""),
            CarEntity(id = 8, brand = "Ford", model = "Ranger 2023", pricePerDay = 85.0, status = CarStatus.DISPONIBLE, imageResName = "")
        )

        val batch = db.batch()
        initialCars.forEach { car ->
            val ref = carsCollection.document(car.id.toString())
            batch.set(ref, car)
        }
        batch.commit()
    }

    override suspend fun addCar(car: CarEntity) {
        try {
            carsCollection.document(car.id.toString()).set(car).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}