package com.sv.elveloz.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sv.elveloz.domain.model.RolUsuario

@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val correo: String,
    val contrasena: String,
    val rol: RolUsuario
)