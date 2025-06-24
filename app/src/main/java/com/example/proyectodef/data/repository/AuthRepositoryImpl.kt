package com.example.proyectodef.data.repository

import android.app.Activity
import android.content.Intent
import com.example.proyectodef.model.User
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.ktx.firestore

class AuthRepositoryImpl(private val activity: Activity) : AuthRepository {

    private val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken("269475319839-08uvmsjumi285adl45jnpoh8sbi99m7q.apps.googleusercontent.com")
        .requestEmail()
        .build()

    private val googleSignInClient = GoogleSignIn.getClient(activity, gso)

    override fun getGoogleSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    override suspend fun handleSignInResult(data: Intent?): Result<User> {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        return try {
            val account = task.getResult(ApiException::class.java)
            val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(account.idToken, null)
            val authResult = Firebase.auth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user
            if (firebaseUser == null) {
                return Result.failure(Exception("Error al autenticar con Firebase"))
            }

            val userId = firebaseUser.uid
            val db = Firebase.firestore
            val docRef = db.collection("usuarios").document(userId)
            val snapshot = docRef.get().await()
            val userFromFirestore = snapshot.toObject(User::class.java)

            return if (userFromFirestore != null) {
                Result.success(userFromFirestore)
            } else {
                val newUser = User(
                    userId = userId,
                    nombreCompleto = account.displayName ?: "",
                    nombreUsuario = account.email?.substringBefore("@") ?: "usuario",
                    correo = account.email ?: "",
                    dineroTotal = 0.0,
                    password = "",
                    photoUrl = account.photoUrl?.toString()
                )
                docRef.set(newUser).await()
                Result.success(newUser)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loginWithEmail(email: String, password: String): Result<User> {
        return try {
            val result = Firebase.auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                val snapshot = Firebase.firestore.collection("usuarios").document(user.uid).get().await()
                val userFromFirestore = snapshot.toObject(User::class.java)
                if (userFromFirestore != null) {
                    Result.success(userFromFirestore)
                } else {
                    Result.failure(Exception("Usuario no encontrado en Firestore"))
                }
            } else {
                Result.failure(Exception("Usuario no encontrado"))
            }
        } catch (e: FirebaseAuthException) {
            val message = when (e.errorCode) {
                "ERROR_USER_NOT_FOUND" -> "Usuario no encontrado"
                "ERROR_WRONG_PASSWORD" -> "Contraseña incorrecta"
                else -> e.localizedMessage ?: "Error desconocido"
            }
            Result.failure(Exception(message))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun registerUser(
        nombreCompleto: String,
        nombreUsuario: String,
        correo: String,
        password: String
    ): Result<User> {
        return try {
            val result = Firebase.auth.createUserWithEmailAndPassword(correo, password).await()
            val user = result.user
            if (user != null) {
                val newUser = User(
                    userId = user.uid,
                    nombreCompleto = nombreCompleto,
                    nombreUsuario = if (nombreUsuario.isNotBlank()) nombreUsuario else correo.substringBefore("@"),
                    correo = user.email ?: "",
                    dineroTotal = 0.0,
                    password = "",
                    photoUrl = user.photoUrl?.toString()
                )

                val db = Firebase.firestore
                db.collection("usuarios").document(user.uid).set(newUser).await()
                Result.success(newUser)
            } else {
                Result.failure(Exception("Error al registrar el usuario"))
            }
        } catch (e: FirebaseAuthException) {
            val message = when (e.errorCode) {
                "ERROR_EMAIL_ALREADY_IN_USE" -> "El correo ya está en uso"
                "ERROR_WEAK_PASSWORD" -> "La contraseña es demasiado débil"
                else -> e.localizedMessage ?: "Error desconocido"
            }
            Result.failure(Exception(message))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserFromFirestore(userId: String): User? {
        val snapshot = Firebase.firestore.collection("usuarios").document(userId).get().await()
        return snapshot.toObject(User::class.java)
    }

    override suspend fun actualizarCampoUsuario(userId: String, campo: String, valor: Any) {
        Firebase.firestore.collection("usuarios").document(userId).update(campo, valor).await()
    }

    override suspend fun resetearCuentaUsuario(userId: String) {
        val db = Firebase.firestore
        db.collection("usuarios").document(userId).update("dineroTotal", 0.0).await()
        db.collection("usuarios").document(userId).collection("transacciones").get().await().documents.forEach {
                it.reference.delete().await()
            }
        db.collection("usuarios").document(userId).collection("categorias").get().await().documents.forEach {
                it.reference.delete().await()
            }
    }

    override suspend fun eliminarCuentaFirebase(userId: String) {
        Firebase.firestore.collection("usuarios").document(userId).delete().await()
    }

    override fun signOut() {
        Firebase.auth.signOut()
        googleSignInClient.signOut()
    }

    override suspend fun actualizarDineroUsuario(userId: String, nuevoDinero: Double) {
        Firebase.firestore.collection("usuarios").document(userId).update("dineroTotal", nuevoDinero).await()
    }
}
