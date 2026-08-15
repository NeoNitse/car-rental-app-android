package com.sv.elveloz.data.repository

import com.google.firebase.firestore.FirebaseFirestore
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
                val cars = snapshot.toObjects(CarEntity::class.java)

                // Si la base de datos está vacía, subimos los 8 autos iniciales
                if (cars.isEmpty()) {
                    seedInitialCars()
                } else {
                    trySend(cars)
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
            // 1. Descargamos las reservas de Firebase sin filtros problemáticos
            val snapshot = rentalsCollection.get().await()

            // 2. Convertimos los documentos a nuestra clase RentalEntity
            val allRentals = snapshot.documents.mapNotNull { it.toObject(RentalEntity::class.java) }

            // 3. Filtramos los que coincidan con el ID del auto (comparándolos como texto para evitar fallos)
            // Y de esos, extraemos la reserva más reciente
            allRentals
                .filter { it.carId.toString() == carId.toString() }
                .maxByOrNull { it.pickupDateMs ?: 0L }

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun rentCar(rental: RentalEntity, carId: Int) {
        val batch = db.batch()
        // Convertimos los IDs (que son Int) a String para las rutas de Firestore
        val rentalRef = rentalsCollection.document(rental.id.toString())
        val carRef = carsCollection.document(carId.toString())

        batch.set(rentalRef, rental)
        batch.update(carRef, "status", CarStatus.EN_PROCESO.name)
        batch.commit().await()
    }

    override suspend fun updateCarStatus(carId: Int, newStatus: CarStatus) {
        carsCollection.document(carId.toString()).update("status", newStatus.name).await()
    }

    override suspend fun completeRental(rental: RentalEntity, carId: Int) {
        val batch = db.batch()
        val rentalRef = rentalsCollection.document(rental.id.toString())
        val carRef = carsCollection.document(carId.toString())

        batch.update(rentalRef, "isActive", false) // Marcamos como historial
        batch.update(carRef, "status", CarStatus.DISPONIBLE.name) // Auto libre
        batch.commit().await()
    }

    override suspend fun cancelRental(rental: RentalEntity, carId: Int) {
        val batch = db.batch()
        val rentalRef = rentalsCollection.document(rental.id.toString())
        val carRef = carsCollection.document(carId.toString())

        batch.delete(rentalRef) // Borramos la reserva no concretada
        batch.update(carRef, "status", CarStatus.DISPONIBLE.name) // Auto libre
        batch.commit().await()
    }

    private fun seedInitialCars() {
        val initialCars = listOf(
            CarEntity(id = "1", brand = "Toyota", model = "Corolla 2023", pricePerDay = 45.0, status = CarStatus.DISPONIBLE),
            CarEntity(id = "2", brand = "Nissan", model = "Sentra 2022", pricePerDay = 40.0, status = CarStatus.DISPONIBLE),
            CarEntity(id = "3", brand = "Honda", model = "CR-V 2024", pricePerDay = 65.0, status = CarStatus.DISPONIBLE),
            CarEntity(id = "4", brand = "Hyundai", model = "Tucson 2023", pricePerDay = 60.0, status = CarStatus.DISPONIBLE),
            CarEntity(id = "5", brand = "Kia", model = "Picanto 2022", pricePerDay = 30.0, status = CarStatus.DISPONIBLE),
            CarEntity(id = "6", brand = "Mazda", model = "CX-5 2023", pricePerDay = 70.0, status = CarStatus.DISPONIBLE),
            CarEntity(id = "7", brand = "Chevrolet", model = "Aveo 2021", pricePerDay = 35.0, status = CarStatus.DISPONIBLE),
            CarEntity(id = "8", brand = "Ford", model = "Ranger 2023", pricePerDay = 85.0, status = CarStatus.DISPONIBLE)
        )

        val batch = db.batch()
        initialCars.forEach { car ->
            val ref = carsCollection.document(car.id)
            batch.set(ref, car)
        }
        batch.commit()
    }
}