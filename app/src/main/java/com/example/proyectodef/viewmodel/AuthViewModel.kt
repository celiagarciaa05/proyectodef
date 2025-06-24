package com.example.proyectodef.viewmodel

import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectodef.data.repository.AuthRepository
import com.example.proyectodef.model.User
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepo: AuthRepository) : ViewModel() {

    val isLoggedIn: Boolean
        get() {
            return Firebase.auth.currentUser != null
        }

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage

    init {
        val firebaseUser = Firebase.auth.currentUser
        if (firebaseUser != null) {
            _user.value = User(
                userId = firebaseUser.uid,
                nombreUsuario = "",
                correo = firebaseUser.email ?: "",
                dineroTotal = 0.0
            )
            viewModelScope.launch {
                val userFirestore = authRepo.getUserFromFirestore(firebaseUser.uid)
                userFirestore?.let {
                    _user.value = it
                }
            }
        }
    }

    fun getSignInIntent(): Intent {
        return authRepo.getGoogleSignInIntent()
    }

    fun handleSignInResult(data: Intent?) {
        viewModelScope.launch {
            val result = authRepo.handleSignInResult(data)
            result.onSuccess {
                _user.value = it
                _successMessage.value = "Sesión iniciada con Google"
            }.onFailure {
                _errorMessage.value = it.message ?: "Error al iniciar sesión con Google"
            }
        }
    }

    fun loginWithEmail(email: String, password: String) {
        viewModelScope.launch {
            if (email.isBlank() || password.isBlank()) {
                _errorMessage.value = "Todos los campos son obligatorios"
                return@launch
            }

            val result: Result<User> = authRepo.loginWithEmail(email, password)

            result.onSuccess { user ->
                _user.value = user
                _successMessage.value = "Sesión iniciada correctamente"
            }.onFailure { e ->
                _errorMessage.value = e.message ?: "Error al iniciar sesión"
            }
        }
    }

    fun registerUser(nombreCompleto: String, nombreUsuario: String, correo: String, password: String) {
        viewModelScope.launch {
            val result = authRepo.registerUser(nombreCompleto, nombreUsuario, correo, password)
            result.onSuccess {
                _user.value = it
                _successMessage.value = "Registro exitoso"
            }.onFailure {
                _errorMessage.value = it.message ?: "Error al registrarse"
            }
        }
    }

    fun signOut() {
        authRepo.signOut()
        _user.value = null
    }

    fun updateUsernameAndPassword(username: String, password: String) {
        _user.value = _user.value?.copy(
            nombreUsuario = username,
            password = password
        )
    }

    fun updateDinero(dinero: Double) {
        val currentUser = _user.value
        if (currentUser != null) {
            val updatedUser = currentUser.copy(dineroTotal = dinero)
            _user.value = updatedUser

            viewModelScope.launch {
                try {
                    authRepo.actualizarDineroUsuario(currentUser.userId, dinero)
                } catch (e: Exception) {
                    _errorMessage.value = "Error al actualizar el dinero en Firebase"
                }
            }
        }
    }

    fun updateUserProfile(nombreCompleto: String, nombreUsuario: String) {
        val currentUser = _user.value
        if (currentUser != null) {
            val updatedUser = currentUser.copy(
                nombreCompleto = nombreCompleto,
                nombreUsuario = nombreUsuario
            )
            _user.value = updatedUser

            viewModelScope.launch {
                try {
                    authRepo.actualizarCampoUsuario(currentUser.userId, "nombreCompleto", nombreCompleto)
                    authRepo.actualizarCampoUsuario(currentUser.userId, "nombreUsuario", nombreUsuario)
                    _successMessage.value = "Perfil actualizado correctamente"
                } catch (e: Exception) {
                    _errorMessage.value = "Error al actualizar el perfil"
                }
            }
        }
    }

    fun setError(msg: String) {
        _errorMessage.value = msg
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            try {
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                _successMessage.value = "Correo de recuperación enviado"
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error desconocido"
            }
        }
    }

    fun sendFirebasePasswordReset(email: String) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
    }
}