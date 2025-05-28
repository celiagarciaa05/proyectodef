package com.example.proyectodef.utils

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.example.proyectodef.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun PasswordResetManager(
    show: Boolean,
    onDismiss: () -> Unit,
    authViewModel: AuthViewModel
) {

    if (!show) return

    var email by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Recuperar contraseña") },
        text = {
            Column {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Introduce tu correo") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (email.isBlank()) {
                    Toast.makeText(context, "El correo es obligatorio", Toast.LENGTH_SHORT).show()
                } else {
                    scope.launch {
                        authViewModel.resetPassword(email)
                        Toast.makeText(context, "Email de recuperación enviado", Toast.LENGTH_LONG).show()
                        onDismiss()
                    }
                }
            }) {
                Text("Enviar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
