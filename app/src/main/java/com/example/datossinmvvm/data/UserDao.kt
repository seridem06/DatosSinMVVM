package com.example.datossinmvvm.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("SELECT * FROM users")
    suspend fun getAll(): List<User>

    // Agregar esta función para el Ejercicio 1
    @Query("SELECT * FROM users ORDER BY uid DESC LIMIT 1")
    suspend fun getLastUser(): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)

    // Agregar esta función para el Ejercicio 1
    @Query("DELETE FROM users WHERE uid = (SELECT MAX(uid) FROM users)")
    suspend fun deleteLastUser()

    // Funciones adicionales para CRUD completo (Ejercicio 2)
    @Query("SELECT * FROM users WHERE uid = :userId")
    suspend fun getUserById(userId: Int): User?

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)

    @Query("DELETE FROM users")
    suspend fun deleteAll()
}