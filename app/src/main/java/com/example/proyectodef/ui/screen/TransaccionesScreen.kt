package com.example.proyectodef.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.proyectodef.model.Transaction
import com.example.proyectodef.viewmodel.AuthViewModel
import com.example.proyectodef.viewmodel.TransactionViewModel
import com.example.proyectodef.ui.components.AppDrawer
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.text.DecimalFormat
import java.util.*

// Paleta de colores neón
object NeonColors {
    val CelesteFrio = Color(0xFF00F5FF)        // Celeste neón
    val RosaClaro = Color(0xFFFFB6C1)          // Rosa claro
    val RosaFuscia = Color(0xFFFF1493)         // Rosa fucsia
    val AzulElectrico = Color(0xFF0080FF)      // Azul eléctrico
    val VerdeAgua = Color(0xFF00FFFF)          // Verde agua/cyan
    val Lila = Color(0xFFDA70D6)               // Lila
    val FondoOscuro = Color(0xFF0A0A0F)        // Fondo muy oscuro
    val FondoTarjeta = Color(0xFF1A1A2E)       // Fondo de tarjetas
    val Blanco = Color(0xFFFFFFFF)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransaccionesScreen(
    tipo: String,
    authViewModel: AuthViewModel,
    transactionViewModel: TransactionViewModel,
    navController: NavHostController
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val user by authViewModel.user.collectAsState()
    val userId = user?.userId.orEmpty()
    val dineroTotal = user?.dineroTotal ?: 0.0
    val transacciones by transactionViewModel.transaccionesFiltradas.collectAsState()

    // Formateador para mostrar números con 2 decimales
    val decimalFormat = remember { DecimalFormat("#,##0.00") }

    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var showConfirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(userId, tipo) {
        if (userId.isNotBlank()) {
            transactionViewModel.cargarTransaccionesPorTipo(userId, tipo)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(drawerState = drawerState, scope = scope, navController = navController)
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            NeonColors.FondoOscuro,
                            Color(0xFF1A0B2E),
                            Color(0xFF2D1B69),
                            NeonColors.FondoOscuro
                        ),
                        radius = 1000f
                    )
                )
        ) {
            // Efectos de fondo con blur
            Box(
                modifier = Modifier
                    .offset(x = 100.dp, y = 150.dp)
                    .size(200.dp)
                    .background(
                        NeonColors.RosaFuscia.copy(alpha = 0.1f),
                        CircleShape
                    )
                    .blur(50.dp)
            )

            Box(
                modifier = Modifier
                    .offset(x = (-50).dp, y = 300.dp)
                    .size(150.dp)
                    .background(
                        NeonColors.CelesteFrio.copy(alpha = 0.1f),
                        CircleShape
                    )
                    .blur(40.dp)
            )

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                "Transacciones de $tipo",
                                color = NeonColors.Blanco,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = { scope.launch { drawerState.open() } },
                                modifier = Modifier
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                NeonColors.CelesteFrio.copy(alpha = 0.3f),
                                                NeonColors.AzulElectrico.copy(alpha = 0.3f)
                                            )
                                        ),
                                        shape = CircleShape
                                    )
                                    .border(1.dp, NeonColors.CelesteFrio.copy(alpha = 0.5f), CircleShape)
                            ) {
                                Icon(
                                    Icons.Default.Menu,
                                    contentDescription = "Abrir menú",
                                    tint = NeonColors.CelesteFrio
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                },
                containerColor = Color.Transparent,
                modifier = Modifier.fillMaxSize()
            ) { padding ->
                Column(modifier = Modifier.padding(padding)) {
                    if (transacciones.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                modifier = Modifier.padding(32.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = NeonColors.FondoTarjeta.copy(alpha = 0.8f)
                                ),
                                shape = RoundedCornerShape(20.dp),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            NeonColors.Lila.copy(alpha = 0.5f),
                                            NeonColors.RosaClaro.copy(alpha = 0.5f)
                                        )
                                    )
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.AttachMoney,
                                        contentDescription = null,
                                        tint = NeonColors.VerdeAgua,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        "No hay transacciones registradas",
                                        color = NeonColors.Blanco.copy(alpha = 0.9f),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(transacciones) { transaccion ->
                                var expanded by remember { mutableStateOf(false) }

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedTransaction = transaccion
                                            showDialog = true
                                        }
                                        .shadow(
                                            elevation = 12.dp,
                                            shape = RoundedCornerShape(16.dp),
                                            ambientColor = NeonColors.RosaFuscia.copy(alpha = 0.3f),
                                            spotColor = NeonColors.CelesteFrio.copy(alpha = 0.3f)
                                        ),
                                    colors = CardDefaults.cardColors(
                                        containerColor = NeonColors.FondoTarjeta.copy(alpha = 0.9f)
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    border = CardDefaults.outlinedCardBorder().copy(
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                NeonColors.CelesteFrio.copy(alpha = 0.4f),
                                                NeonColors.RosaFuscia.copy(alpha = 0.4f),
                                                NeonColors.VerdeAgua.copy(alpha = 0.4f)
                                            )
                                        )
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(20.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                transaccion.descripcion.takeIf { it.isNotBlank() }
                                                    ?: transaccion.categoria,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp,
                                                color = NeonColors.RosaClaro
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                "Categoría: ${transaccion.categoria}",
                                                color = NeonColors.Lila,
                                                fontSize = 14.sp
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    Icons.Default.AttachMoney,
                                                    contentDescription = null,
                                                    tint = NeonColors.VerdeAgua,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    "${decimalFormat.format(transaccion.cantidad)}€",
                                                    color = NeonColors.VerdeAgua,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 16.sp
                                                )
                                            }
                                        }
                                        Box {
                                            IconButton(
                                                onClick = { expanded = true },
                                                modifier = Modifier
                                                    .background(
                                                        brush = Brush.radialGradient(
                                                            colors = listOf(
                                                                NeonColors.RosaFuscia.copy(alpha = 0.2f),
                                                                Color.Transparent
                                                            )
                                                        ),
                                                        shape = CircleShape
                                                    )
                                                    .border(
                                                        1.dp,
                                                        NeonColors.RosaFuscia.copy(alpha = 0.5f),
                                                        CircleShape
                                                    )
                                            ) {
                                                Icon(
                                                    Icons.Default.MoreVert,
                                                    contentDescription = "Más opciones",
                                                    tint = NeonColors.RosaFuscia
                                                )
                                            }
                                            DropdownMenu(
                                                expanded = expanded,
                                                onDismissRequest = { expanded = false },
                                                modifier = Modifier
                                                    .background(
                                                        NeonColors.FondoTarjeta.copy(alpha = 0.95f),
                                                        RoundedCornerShape(12.dp)
                                                    )
                                                    .border(
                                                        1.dp,
                                                        NeonColors.RosaFuscia.copy(alpha = 0.3f),
                                                        RoundedCornerShape(12.dp)
                                                    )
                                            ) {
                                                DropdownMenuItem(
                                                    text = {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Icon(
                                                                Icons.Default.Delete,
                                                                contentDescription = null,
                                                                tint = NeonColors.RosaFuscia,
                                                                modifier = Modifier.size(18.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text(
                                                                "Eliminar transacción",
                                                                color = NeonColors.Blanco
                                                            )
                                                        }
                                                    },
                                                    onClick = {
                                                        selectedTransaction = transaccion
                                                        showConfirmDelete = true
                                                        expanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (showDialog && selectedTransaction != null) {
                        AlertDialog(
                            onDismissRequest = { showDialog = false },
                            confirmButton = {
                                TextButton(
                                    onClick = { showDialog = false },
                                    modifier = Modifier
                                        .background(
                                            brush = Brush.linearGradient(
                                                colors = listOf(
                                                    NeonColors.CelesteFrio.copy(alpha = 0.2f),
                                                    NeonColors.AzulElectrico.copy(alpha = 0.2f)
                                                )
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .border(
                                            1.dp,
                                            NeonColors.CelesteFrio.copy(alpha = 0.5f),
                                            RoundedCornerShape(8.dp)
                                        )
                                ) {
                                    Text("Cerrar", color = NeonColors.CelesteFrio)
                                }
                            },
                            title = {
                                Text(
                                    "Detalles de la transacción",
                                    color = NeonColors.RosaClaro,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            text = {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("Título: ${selectedTransaction!!.descripcion.takeIf { it.isNotBlank() } ?: selectedTransaction!!.categoria}", color = NeonColors.Blanco)
                                    Text("Descripción: ${selectedTransaction!!.descripcion}", color = NeonColors.Lila)
                                    Text("Categoría: ${selectedTransaction!!.categoria}", color = NeonColors.RosaClaro)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.AttachMoney,
                                            contentDescription = null,
                                            tint = NeonColors.VerdeAgua,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text("${decimalFormat.format(selectedTransaction!!.cantidad)}€", color = NeonColors.VerdeAgua, fontWeight = FontWeight.SemiBold)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.DateRange,
                                            contentDescription = null,
                                            tint = NeonColors.CelesteFrio,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(selectedTransaction!!.fecha)),
                                            color = NeonColors.CelesteFrio
                                        )
                                    }
                                }
                            },
                            containerColor = NeonColors.FondoTarjeta.copy(alpha = 0.95f),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }

                    if (showConfirmDelete && selectedTransaction != null) {
                        AlertDialog(
                            onDismissRequest = { showConfirmDelete = false },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        selectedTransaction?.let { transaccion ->
                                            transactionViewModel.eliminarTransaccion(
                                                userId,
                                                transaccion
                                            ) { cambio ->
                                                authViewModel.updateDinero(dineroTotal - cambio)
                                            }
                                        }
                                        showConfirmDelete = false
                                    },
                                    modifier = Modifier
                                        .background(
                                            brush = Brush.linearGradient(
                                                colors = listOf(
                                                    NeonColors.RosaFuscia.copy(alpha = 0.2f),
                                                    Color.Red.copy(alpha = 0.2f)
                                                )
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .border(
                                            1.dp,
                                            NeonColors.RosaFuscia.copy(alpha = 0.7f),
                                            RoundedCornerShape(8.dp)
                                        )
                                ) {
                                    Text("Eliminar", color = NeonColors.RosaFuscia, fontWeight = FontWeight.Bold)
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = { showConfirmDelete = false },
                                    modifier = Modifier
                                        .background(
                                            NeonColors.Lila.copy(alpha = 0.1f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .border(
                                            1.dp,
                                            NeonColors.Lila.copy(alpha = 0.5f),
                                            RoundedCornerShape(8.dp)
                                        )
                                ) {
                                    Text("Cancelar", color = NeonColors.Lila)
                                }
                            },
                            title = {
                                Text(
                                    "¿Eliminar transacción?",
                                    color = NeonColors.RosaFuscia,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            text = {
                                Text(
                                    "Esta acción no se puede deshacer.",
                                    color = NeonColors.Blanco.copy(alpha = 0.9f)
                                )
                            },
                            containerColor = NeonColors.FondoTarjeta.copy(alpha = 0.95f),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
            }
        }
    }
}