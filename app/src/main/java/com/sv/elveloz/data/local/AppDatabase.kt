package com.sv.elveloz.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sv.elveloz.data.local.dao.CarDao
import com.sv.elveloz.data.local.dao.RentalDao
import com.sv.elveloz.data.local.dao.UsuarioDao
import com.sv.elveloz.data.local.entity.CarEntity
import com.sv.elveloz.data.local.entity.RentalEntity
import com.sv.elveloz.data.local.entity.UsuarioEntity
import com.sv.elveloz.domain.model.CarStatus
import com.sv.elveloz.domain.model.RolUsuario
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [CarEntity::class, RentalEntity::class, UsuarioEntity::class],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun carDao(): CarDao
    abstract fun rentalDao(): RentalDao
    abstract fun usuarioDao(): UsuarioDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "el_veloz_db"
                ).fallbackToDestructiveMigration()
                    .addCallback(PreseedCallback(context)).build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class PreseedCallback(private val context: Context) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                val dbInstance = getInstance(context)
                val carDao = dbInstance.carDao()
                val usuarioDao = dbInstance.usuarioDao()

                val initialCars = listOf(
                    CarEntity(brand = "Toyota", model = "Corolla 2023", pricePerDay = 45.0, status = CarStatus.DISPONIBLE, imageResName = "car_corolla"),
                    CarEntity(brand = "Nissan", model = "Sentra 2022", pricePerDay = 40.0, status = CarStatus.DISPONIBLE, imageResName = "car_sentra"),
                    CarEntity(brand = "Honda", model = "CR-V 2024", pricePerDay = 65.0, status = CarStatus.DISPONIBLE, imageResName = "car_crv"),
                    CarEntity(brand = "Hyundai", model = "Tucson 2023", pricePerDay = 60.0, status = CarStatus.DISPONIBLE, imageResName = "car_tucson"),
                    CarEntity(brand = "Kia", model = "Picanto 2022", pricePerDay = 30.0, status = CarStatus.DISPONIBLE, imageResName = "car_picanto"),
                    CarEntity(brand = "Mazda", model = "CX-5 2023", pricePerDay = 70.0, status = CarStatus.DISPONIBLE, imageResName = "car_cx5"),
                    CarEntity(brand = "Chevrolet", model = "Aveo 2021", pricePerDay = 35.0, status = CarStatus.DISPONIBLE, imageResName = "car_aveo"),
                    CarEntity(brand = "Ford", model = "Ranger 2023", pricePerDay = 85.0, status = CarStatus.DISPONIBLE, imageResName = "car_ranger")
                )
                carDao.insertAll(initialCars)

                val defaultUsers = listOf(
                    UsuarioEntity(nombre = "Admin Recepción", correo = "admin@elveloz.com", contrasena = "1234", rol = RolUsuario.RECEPCIONISTA),
                    UsuarioEntity(nombre = "Juan Cliente", correo = "cliente@gmail.com", contrasena = "1234", rol = RolUsuario.CLIENTE)
                )
                defaultUsers.forEach { usuarioDao.insertar(it) }
            }
        }
    }
}
