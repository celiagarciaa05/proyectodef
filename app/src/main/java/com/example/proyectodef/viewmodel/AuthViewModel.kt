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
            val logged = Firebase.auth.currentUser != null
            Log.d("Celia", "Celia dice: isLoggedIn chequeado: $logged")
            return logged
        }

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage

    init {
        val firebaseUser = Firebase.auth.currentUser
        Log.d("Celia", "Celia dice: Firebase user on init: $firebaseUser")
        if (firebaseUser != null) {
            _user.value = User(
                userId = firebaseUser.uid,
                nombreUsuario = "", // provisional
                correo = firebaseUser.email ?: "",
                dineroTotal = 0.0
            )
            Log.d("Celia", "Celia dice: Usuario provisional seteado: ${_user.value}")
            viewModelScope.launch {
                val userFirestore = authRepo.getUserFromFirestore(firebaseUser.uid)
                Log.d("Celia", "Celia dice: Usuario Firestore obtenido: $userFirestore")
                userFirestore?.let {
                    _user.value = it
                    Log.d("Celia", "Celia dice: Usuario actualizado con Firestore: $it")
                }
            }
        }
    }

    fun getSignInIntent(): Intent {
        val intent = authRepo.getGoogleSignInIntent()
        Log.d("Celia", "Celia dice: getSignInIntent llamado, intent obtenido")
        return intent
    }

    fun handleSignInResult(data: Intent?) {
        viewModelScope.launch {
            Log.d("Celia", "Celia dice: handleSignInResult iniciado")
            val result = authRepo.handleSignInResult(data)
            result.onSuccess {
                _user.value = it
                Log.d("Celia", "Celia dice: Login Google exitoso: $it")
                Log.d("Celia", "Celia dice: Usuario Firebase tras login: ${Firebase.auth.currentUser}")
                _successMessage.value = "Sesión iniciada con Google"
            }
                .onFailure {
                    _errorMessage.value = it.message ?: "Error al iniciar sesión con Google"
                    Log.d("Celia", "Celia dice: Error login Google: ${it.message}")
                }
        }
    }

    fun loginWithEmail(email: String, password: String) {
        viewModelScope.launch {
            Log.d("Celia", "Celia dice: loginWithEmail llamado con email=$email")
            if (email.isBlank() || password.isBlank()) {
                _errorMessage.value = "Todos los campos son obligatorios"
                Log.d("Celia", "Celia dice: Campos vacíos en login con email")
                return@launch
            }

            val result: Result<User> = authRepo.loginWithEmail(email, password)

            result.onSuccess { user ->
                _user.value = user
                Log.d("Celia", "Celia dice: Login email exitoso: $user")
                Log.d("Celia", "Celia dice: Usuario Firebase tras login: ${Firebase.auth.currentUser}")
                _successMessage.value = "Sesión iniciada correctamente"
            }
                .onFailure { e ->
                    _errorMessage.value = e.message ?: "Error al iniciar sesión"
                    Log.d("Celia", "Celia dice: Error login email: ${e.message}")
                }
        }
    }

    fun registerUser(nombreCompleto: String, nombreUsuario: String, correo: String, password: String) {
        viewModelScope.launch {
            Log.d("Celia", "Celia dice: registerUser llamado con usuario: $nombreUsuario")
            val result = authRepo.registerUser(nombreCompleto, nombreUsuario, correo, password)
            result.onSuccess {
                _user.value = it
                Log.d("Celia", "Celia dice: Registro exitoso: $it")
                _successMessage.value = "Registro exitoso"
            }
                .onFailure {
                    _errorMessage.value = it.message ?: "Error al registrarse"
                    Log.d("Celia", "Celia dice: Error registro: ${it.message}")
                }
        }
    }

    fun signOut() {
        Log.d("Celia", "Celia dice: signOut llamado")
        authRepo.signOut()
        _user.value = null
        Log.d("Celia", "Celia dice: Usuario desconectado (signOut llamado)")
    }

    fun updateUsernameAndPassword(username: String, password: String) {
        _user.value = _user.value?.copy(
            nombreUsuario = username,
            password = password
        )
        Log.d("Celia", "Celia dice: Usuario actualizado username y password: ${_user.value}")
    }

    fun updateDinero(dinero: Double) {
        val currentUser = _user.value
        if (currentUser != null) {
            val updatedUser = currentUser.copy(dineroTotal = dinero)
            _user.value = updatedUser
            Log.d("Celia", "Celia dice: updateDinero llamado con cantidad: $dinero")

            viewModelScope.launch {
                try {
                    authRepo.actualizarDineroUsuario(currentUser.userId, dinero)
                    Log.d("Celia", "Celia dice: Dinero actualizado en Firebase: $dinero")
                } catch (e: Exception) {
                    _errorMessage.value = "Error al actualizar el dinero en Firebase"
                    Log.d("Celia", "Celia dice: Error actualizar dinero: ${e.message}")
                }
            }
        } else {
            Log.d("Celia", "Celia dice: updateDinero ignorado porque currentUser es null")
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
            Log.d("Celia", "Celia dice: updateUserProfile llamado para $nombreUsuario")

            viewModelScope.launch {
                try {
                    authRepo.actualizarCampoUsuario(currentUser.userId, "nombreCompleto", nombreCompleto)
                    authRepo.actualizarCampoUsuario(currentUser.userId, "nombreUsuario", nombreUsuario)
                    _successMessage.value = "Perfil actualizado correctamente"
                    Log.d("Celia", "Celia dice: Perfil actualizado correctamente")
                } catch (e: Exception) {
                    _errorMessage.value = "Error al actualizar el perfil"
                    Log.d("Celia", "Celia dice: Error actualizar perfil: ${e.message}")
                }
            }
        } else {
            Log.d("Celia", "Celia dice: updateUserProfile ignorado porque currentUser es null")
        }
    }

    fun setError(msg: String) {
        _errorMessage.value = msg
        Log.d("Celia", "Celia dice: Error seteado: $msg")
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
        Log.d("Celia", "Celia dice: Mensajes limpiados")
    }

    fun resetPassword(email: String) {
        Log.d("Celia", "Celia dice: resetPassword llamado para $email")
        viewModelScope.launch {
            try {
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                _successMessage.value = "Correo de recuperación enviado"
                Log.d("Celia", "Celia dice: Correo de recuperación enviado a $email")
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error desconocido"
                Log.d("Celia", "Celia dice: Error reset password: ${e.message}")
            }
        }
    }

    fun sendFirebasePasswordReset(email: String) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
        Log.d("Celia", "Celia dice: Enviado email reset password a $email")
    }
}
