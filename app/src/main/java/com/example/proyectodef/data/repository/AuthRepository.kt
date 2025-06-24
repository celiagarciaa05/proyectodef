package com.example.proyectodef.data.repository

import android.content.Intent
import com.example.proyectodef.model.User

interface AuthRepository {
    fun getGoogleSignInIntent(): Intent
    suspend fun handleSignInResult(data: Intent?): Result<User>
    suspend fun loginWithEmail(email: String, password: String): Result<User>
    suspend fun registerUser(nombreCompleto: String, nombreUsuario: String, correo: String, password: String): Result<User>
    suspend fun actualizarDineroUsuario(userId: String, nuevoDinero: Double)
    suspend fun getUserFromFirestore(userId: String): User?
    suspend fun actualizarCampoUsuario(userId: String, campo: String, valor: Any)
    suspend fun resetearCuentaUsuario(userId: String)
    suspend fun eliminarCuentaFirebase(userId: String)
    fun signOut()
}

