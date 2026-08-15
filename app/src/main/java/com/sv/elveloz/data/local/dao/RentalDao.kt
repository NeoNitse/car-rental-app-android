package com.sv.elveloz.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.sv.elveloz.data.local.entity.RentalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RentalDao {

    @Insert
    suspend fun insert(rental: RentalEntity): Long

    @Update
    suspend fun update(rental: RentalEntity)

    @Query("DELETE FROM rentals WHERE id = :rentalId")
    suspend fun delete(rentalId: Int)

    @Query("SELECT * FROM rentals WHERE carId = :carId AND isActive = 1 LIMIT 1")
    suspend fun getActiveRentalForCar(carId: Int): RentalEntity?

    @Query("SELECT * FROM rentals ORDER BY id DESC")
    fun getAllRentals(): Flow<List<RentalEntity>>

    @Query("SELECT * FROM rentals WHERE estado = 'SOLICITADA'")
    fun obtenerSolicitudesPendientes(): Flow<List<RentalEntity>>
}