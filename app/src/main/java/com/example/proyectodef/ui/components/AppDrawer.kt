package com.example.proyectodef.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

data class DrawerItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun AppDrawer(
    drawerState: DrawerState,
    scope: CoroutineScope,
    navController: NavHostController
) {
    val opciones = listOf(
        DrawerItem("Panel Principal", Icons.Default.Home, "home"),
        DrawerItem("Configuración de Cuenta", Icons.Default.AccountCircle, "miCuenta"),
        DrawerItem("Metas Financieras", Icons.Default.Star, "metas"),
        DrawerItem("Administrar Metas", Icons.Default.Settings, "gestionMetas"),
        DrawerItem("Historial de Gastos", Icons.Default.List, "transacciones/Gasto"),
        DrawerItem("Historial de Ahorros", Icons.Default.AttachMoney, "transacciones/Ahorro"),
        DrawerItem("Asistente Virtual", Icons.Default.Message, "chatbot")
    )

    // Animación de fondo más suave
    val infiniteTransition = rememberInfiniteTransition()
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 800f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val backgroundBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFFBB86FC).copy(alpha = 0.9f),
            Color(0xFF03DAC5).copy(alpha = 0.8f),
            Color(0xFFE91E63).copy(alpha = 0.7f)
        ),
        start = Offset.Zero,
        end = Offset(x = offset, y = offset * 0.7f)
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(brush = backgroundBrush)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 60.dp, start = 20.dp, end = 20.dp, bottom = 20.dp)
                ) {
                    // Header del drawer
                    // Header del drawer (versión más compacta)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp), // reducido
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp), // antes era 24.dp
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.AttachMoney,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(36.dp) // antes 48.dp
                            )
                            Spacer(modifier = Modifier.height(8.dp)) // antes 12.dp
                            Text(
                                text = "Mi Finanzas",
                                color = Color.White,
                                fontSize = 18.sp, // antes 22.sp
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Gestiona tu dinero",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp // antes 14.sp
                            )
                        }
                    }


                    // Lista de opciones
                    opciones.forEach { item ->
                        DrawerMenuItem(
                            item = item,
                            onClick = {
                                scope.launch { drawerState.close() }
                                when (item.route) {
                                    "home" -> navController.navigate("home") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                    else -> navController.navigate(item.route)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        },
        scrimColor = Color.Black.copy(alpha = 0.5f)
    ) {
        // Contenido principal de la pantalla
    }
}

@Composable
private fun DrawerMenuItem(
    item: DrawerItem,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isPressed) 2.dp else 6.dp,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable {
                isPressed = true
                onClick()
            },
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = if (isPressed) 0.9f else 0.85f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = Color(0xFF6200EE),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = item.title,
                color = Color(0xFF2D2D2D),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFF6200EE).copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}