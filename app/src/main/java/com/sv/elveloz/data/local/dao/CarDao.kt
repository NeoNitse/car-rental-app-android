package com.sv.elveloz.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sv.elveloz.data.local.entity.CarEntity
import com.sv.elveloz.domain.model.CarStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface CarDao {

    @Query("SELECT * FROM cars ORDER BY id ASC")
    fun getAllCars(): Flow<List<CarEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(cars: List<CarEntity>)

    @Update
    suspend fun updateCar(car: CarEntity)

    @Query("UPDATE cars SET status = :newStatus WHERE id = :carId")
    suspend fun updateStatus(carId: Int, newStatus: CarStatus)
}