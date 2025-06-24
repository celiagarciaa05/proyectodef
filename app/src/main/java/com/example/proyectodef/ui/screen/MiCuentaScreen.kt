package com.example.proyectodef.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.proyectodef.ui.components.AppDrawer
import com.example.proyectodef.utils.ResumenPdfGenerator
import com.example.proyectodef.viewmodel.AuthViewModel
import com.example.proyectodef.viewmodel.TransactionViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiCuentaScreen(
    authViewModel: AuthViewModel,
    transactionViewModel: TransactionViewModel,
    navController: NavController
) {
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val user by authViewModel.user.collectAsState()

    val scrollState = rememberScrollState()

    var nombreCompleto by remember { mutableStateOf(user?.nombreCompleto ?: "") }
    var nombreUsuario by remember { mutableStateOf(user?.nombreUsuario ?: "") }
    val correo = user?.correo ?: ""
    var dineroTotalField by remember { mutableStateOf(user?.dineroTotal?.toString() ?: "0.0") }
    var nuevaContraseña by remember { mutableStateOf("") }
    val neonPink = Color(0xFFFF007F)
    val neonPurple = Color(0xFFBF00FF)
    val neonCyan = Color(0xFF00FFFF)
    val neonAqua = Color(0xFF00FFB3)
    val neonBlue = Color(0xFF0080FF)
    val neonLilac = Color(0xFFE066FF)
    val darkBg = Color(0xFF0A0A0F)
    val darkerBg = Color(0xFF050508)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                drawerState = drawerState,
                scope = scope,
                navController = navController as NavHostController
            )
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1A0033),
                            Color(0xFF330066),
                            Color(0xFF4D0080),
                            Color(0xFF0066CC),
                            Color(0xFF003D66)

                        )
                    )
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                neonPink.copy(alpha = 0.1f),
                                Color.Transparent,
                                neonCyan.copy(alpha = 0.15f),
                                Color.Transparent,
                                neonAqua.copy(alpha = 0.1f)
                            )
                        )
                    )
            ) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    "Mi Cuenta",
                                    color = neonCyan,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            navigationIcon = {
                                IconButton(
                                    onClick = { scope.launch { drawerState.open() } },
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            brush = Brush.linearGradient(
                                                colors = listOf(
                                                    neonPink.copy(alpha = 0.3f),
                                                    neonPurple.copy(alpha = 0.3f)
                                                )
                                            )
                                        )
                                ) {
                                    Icon(
                                        Icons.Default.Menu,
                                        contentDescription = "Abrir menú",
                                        tint = neonCyan
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                        )
                    },
                    containerColor = Color.Transparent,
                    modifier = Modifier.fillMaxSize()
                ) { padding ->
                    Column(
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val neonTextFieldColors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = neonCyan,
                            unfocusedBorderColor = neonLilac.copy(alpha = 0.6f),
                            focusedLabelColor = neonCyan,
                            unfocusedLabelColor = neonLilac.copy(alpha = 0.8f),
                            cursorColor = neonPink,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = neonLilac.copy(alpha = 0.9f),

                            )
                        val correoTextFieldColors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = neonAqua,
                            unfocusedBorderColor = neonAqua.copy(alpha = 0.5f),
                            focusedLabelColor = neonAqua,
                            unfocusedLabelColor = neonAqua.copy(alpha = 0.8f),
                            cursorColor = neonPink,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = neonLilac.copy(alpha = 0.9f),

                            )
                        val dineroTextFieldColors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = neonAqua,
                            unfocusedBorderColor = neonAqua.copy(alpha = 0.6f),
                            focusedLabelColor = neonAqua,
                            unfocusedLabelColor = neonAqua.copy(alpha = 0.8f),
                            cursorColor = neonPink,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = neonLilac.copy(alpha = 0.9f),

                            )
                        val passwordTextFieldColors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = neonPink,
                            unfocusedBorderColor = neonPink.copy(alpha = 0.6f),
                            focusedLabelColor = neonPink,
                            unfocusedLabelColor = neonPink.copy(alpha = 0.8f),
                            cursorColor = neonPink,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = neonLilac.copy(alpha = 0.9f),

                            )

                        OutlinedTextField(
                            value = nombreCompleto,
                            onValueChange = { nombreCompleto = it },
                            label = { Text("Nombre completo", fontWeight = FontWeight.Medium) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp)),
                            colors = neonTextFieldColors,
                            shape = RoundedCornerShape(16.dp)
                        )

                        OutlinedTextField(
                            value = nombreUsuario,
                            onValueChange = { nombreUsuario = it },
                            label = { Text("Nombre de usuario", fontWeight = FontWeight.Medium) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp)),
                            colors = neonTextFieldColors,
                            shape = RoundedCornerShape(16.dp)
                        )

                        OutlinedTextField(
                            value = correo,
                            onValueChange = {},
                            label = { Text("Correo electrónico", fontWeight = FontWeight.Medium) },
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp)),
                            colors = correoTextFieldColors,
                            shape = RoundedCornerShape(16.dp)
                        )

                        Button(
                            onClick = {
                                user?.let {
                                    authViewModel.updateUserProfile(nombreCompleto, nombreUsuario)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(neonPink, neonPurple, neonLilac)
                                        ),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Guardar cambios",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            neonCyan.copy(alpha = 0.5f),
                                            neonAqua.copy(alpha = 0.5f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        OutlinedTextField(
                            value = dineroTotalField,
                            onValueChange = { dineroTotalField = it },
                            label = { Text("Dinero total", fontWeight = FontWeight.Medium) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp)),
                            colors = dineroTextFieldColors,
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp)
                        )

                        Button(
                            onClick = {
                                user?.let {
                                    val dinero = dineroTotalField.toDoubleOrNull() ?: 0.0
                                    authViewModel.updateDinero(dinero)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(neonAqua, neonCyan, neonBlue)
                                        ),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Actualizar dinero",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            neonPink.copy(alpha = 0.5f),
                                            neonPurple.copy(alpha = 0.5f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        OutlinedTextField(
                            value = nuevaContraseña,
                            onValueChange = { nuevaContraseña = it },
                            label = { Text("Nueva contraseña", fontWeight = FontWeight.Medium) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp)),
                            colors = passwordTextFieldColors,
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            shape = RoundedCornerShape(16.dp)
                        )

                        Button(
                            onClick = {
                                if (nuevaContraseña.isNotBlank()) {
                                    Firebase.auth.currentUser?.updatePassword(nuevaContraseña)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(neonPink, neonLilac, neonPurple)
                                        ),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Actualizar contraseña",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                user?.let { u ->
                                    scope.launch {
                                        val db = Firebase.firestore
                                        val transacciones = db.collection("usuarios")
                                            .document(u.userId)
                                            .collection("transacciones")
                                            .get()
                                            .await()
                                            .documents

                                        val unMesAtras =
                                            System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
                                        val transaccionesMes = transacciones.filter {
                                            (it.getLong("fecha") ?: 0L) >= unMesAtras
                                        }

                                        if (transaccionesMes.isEmpty()) {
                                            println("No hay transacciones para generar el resumen.")
                                        } else {
                                            ResumenPdfGenerator.generarYEnviar(
                                                context = navController.context,
                                                user = u,
                                                transacciones = transaccionesMes,
                                                firestore = db
                                            )
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(neonBlue, neonCyan, neonAqua)
                                        ),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Crear PDF resumen",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }


                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            neonBlue.copy(alpha = 0.5f),
                                            neonAqua.copy(alpha = 0.5f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        Button(
                            onClick = {
                                val db = Firebase.firestore
                                user?.let {
                                    scope.launch {
                                        db.collection("usuarios").document(it.userId)
                                            .update("dineroTotal", 0).await()

                                        db.collection("usuarios").document(it.userId)
                                            .collection("categorias").get()
                                            .await().documents.forEach { doc -> doc.reference.delete() }

                                        db.collection("usuarios").document(it.userId)
                                            .collection("transacciones").get()
                                            .await().documents.forEach { doc -> doc.reference.delete() }

                                        db.collection("usuarios").document(it.userId)
                                            .collection("metas").get()
                                            .await().documents.forEach { doc -> doc.reference.delete() }

                                        authViewModel.updateDinero(0.0)
                                        transactionViewModel.cargarCategorias(it.userId)

                                        navController.navigate("miCuenta") {
                                            popUpTo("miCuenta") { inclusive = true }
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0xFFFF6B35),
                                                Color(0xFFFF3D71),
                                                neonPink
                                            )
                                        ),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Resetear cuenta",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                authViewModel.signOut()
                                navController.navigate("login") {
                                    popUpTo("home") { inclusive = true }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0xFF7C4DFF),
                                                neonPurple,
                                                neonLilac
                                            )
                                        ),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Cerrar sesión",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                val currentUser = Firebase.auth.currentUser
                                scope.launch {
                                    currentUser?.delete()
                                    Firebase.firestore.collection("usuarios")
                                        .document(currentUser?.uid.orEmpty()).delete()
                                    authViewModel.signOut()
                                    navController.navigate("login") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0xFFFF1744),
                                                Color(0xFFE91E63),
                                                Color(0xFFFF4081)
                                            )
                                        ),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Eliminar cuenta",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}