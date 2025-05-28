package com.example.proyectodef.ui.screen

import BarraAgrupadaGrafico
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
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
import com.example.proyectodef.model.Meta
import com.example.proyectodef.ui.components.AppDrawer
import com.example.proyectodef.viewmodel.AuthViewModel
import com.example.proyectodef.viewmodel.CategoriaConTotales
import com.example.proyectodef.viewmodel.MetaViewModel
import com.example.proyectodef.viewmodel.TransactionViewModel
import kotlinx.coroutines.launch
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.example.proyectodef.viewmodel.ProgresoViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    nombreUsuario: String = "Usuario",
    authViewModel: AuthViewModel,
    transactionViewModel: TransactionViewModel,
    metaViewModel: MetaViewModel,
    progresoViewModel: ProgresoViewModel,
    navController: NavHostController
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }

    // Estados y datos
    val firebaseUserId = Firebase.auth.currentUser?.uid.orEmpty()
    val user by authViewModel.user.collectAsState()
    val userId = user?.userId ?: firebaseUserId
    val dineroTotal = user?.dineroTotal ?: 0.0

    val categorias = transactionViewModel.categorias.collectAsState().value.map { it.nombre }
    val datosGrafico = transactionViewModel.transaccionesPorCategoria.collectAsState().value
    val transacciones by transactionViewModel.transaccionesPorCategoria.collectAsState()
    val metas by metaViewModel.metas.collectAsState()
    val metasAhorroEnProceso = metas.filter { it.estado == "Proceso" && it.tipo == "Ahorro" }

    // Animaciones
    val balanceAnimation by animateFloatAsState(
        targetValue = dineroTotal.toFloat(),
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        label = "balance"
    )

    // Efectos
    LaunchedEffect(userId, transacciones) {
        if (userId.isNotBlank()) {
            transactionViewModel.cargarCategorias(userId)
            transactionViewModel.cargarTransaccionesPorCategoria(userId)
            transactionViewModel.cargarTodasTransacciones(userId)
            metaViewModel.cargarMetas(userId)
            metaViewModel.escucharCambiosMetas(userId)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(drawerState, scope, navController)
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFBB86FC),
                            Color(0xFF5FFFFF),
                            Color(0xFF0F3460)
                        )
                    )
                )
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                "Home",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = { scope.launch { drawerState.open() } },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.1f))
                            ) {
                                Icon(
                                    Icons.Default.Menu,
                                    "Abrir menú",
                                    tint = Color.White
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { showDialog = true },
                        containerColor = Color(0xFF00E5FF),
                        contentColor = Color.White,
                        modifier = Modifier
                            .shadow(
                                elevation = 12.dp,
                                shape = CircleShape,
                                ambientColor = Color(0xFF00E5FF).copy(alpha = 0.3f),
                                spotColor = Color(0xFF00E5FF).copy(alpha = 0.3f)
                            )
                    ) {
                        Icon(
                            Icons.Default.Add,
                            "Añadir transacción",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                containerColor = Color.Transparent,
                modifier = Modifier.fillMaxSize()
            ) { padding ->
                LazyColumn(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Sección de bienvenida y balance
                    item {
                        AnimatedVisibility(
                            visible = true,
                            enter = slideInVertically(
                                initialOffsetY = { -40 },
                                animationSpec = tween(800)
                            ) + fadeIn(tween(800))
                        ) {
                            WelcomeSection(
                                nombreUsuario = user?.nombreUsuario ?: nombreUsuario,
                                balance = balanceAnimation,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        }
                    }

                    // Estadísticas rápidas
                    item {
                        AnimatedVisibility(
                            visible = true,
                            enter = slideInVertically(
                                initialOffsetY = { 40 },
                                animationSpec = tween(800, delayMillis = 200)
                            ) + fadeIn(tween(800, delayMillis = 200))
                        ) {
                            QuickStatsSection(
                                totalTransacciones = transacciones.size,
                                metasActivas = metasAhorroEnProceso.size
                            )
                        }
                    }

                    // Gráfico
                    item {
                        AnimatedVisibility(
                            visible = datosGrafico.isNotEmpty(),
                            enter = slideInVertically(
                                initialOffsetY = { 60 },
                                animationSpec = tween(800, delayMillis = 400)
                            ) + fadeIn(tween(800, delayMillis = 400))
                        ) {
                            ChartSection(
                                datosGrafico = datosGrafico,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Metas en proceso
                    if (metasAhorroEnProceso.isNotEmpty()) {
                        item {
                            AnimatedVisibility(
                                visible = true,
                                enter = slideInVertically(
                                    initialOffsetY = { 60 },
                                    animationSpec = tween(800, delayMillis = 600)
                                ) + fadeIn(tween(800, delayMillis = 600))
                            ) {
                                SectionHeader(
                                    title = "Metas en Proceso",
                                    subtitle = "${metasAhorroEnProceso.size} metas activas",
                                    icon = Icons.Default.TrendingUp
                                )
                            }
                        }

                        items(metasAhorroEnProceso.take(3)) { meta ->
                            AnimatedVisibility(
                                visible = true,
                                enter = slideInHorizontally(
                                    initialOffsetX = { 300 },
                                    animationSpec = tween(600, delayMillis = 100)
                                ) + fadeIn(tween(600, delayMillis = 100))
                            ) {
                                GoalCard(meta = meta)
                            }
                        }
                    }

                    // Espacio final
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }

                // Dialog de transacciones
                if (showDialog && userId.isNotBlank() && categorias.isNotEmpty()) {
                    PopupTransaccion(
                        userId = userId,
                        categorias = categorias,
                        onDismiss = { showDialog = false },
                        onConfirm = { showDialog = false },
                        viewModel = transactionViewModel,
                        onUpdateDinero = { cantidad, tipo ->
                            val nuevoTotal = if (tipo == "Ahorro") {
                                dineroTotal + cantidad
                            } else {
                                dineroTotal - cantidad
                            }
                            authViewModel.updateDinero(nuevoTotal)
                            transactionViewModel.cargarTransaccionesPorCategoria(userId)
                            transactionViewModel.cargarTodasTransacciones(userId)
                        },
                        metaViewModel = metaViewModel, // ✅
                        progresoViewModel = progresoViewModel // ✅
                    )

                }
            }
        }
    }
}

@Composable
private fun WelcomeSection(
    nombreUsuario: String,
    balance: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = Color(0xFF00E5FF).copy(alpha = 0.2f)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E2E).copy(alpha = 0.9f)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF2A2A3A),
                            Color(0xFF1E1E2E)
                        ),
                        start = Offset.Zero,
                        end = Offset.Infinite
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "¡Bienvenido!",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Light
                    ),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = nombreUsuario,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(vertical = 4.dp),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Balance Total",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF00E5FF),
                        fontWeight = FontWeight.Medium
                    ),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "€${String.format("%.2f", balance)}",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = 42.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun QuickStatsSection(
    totalTransacciones: Int,
    metasActivas: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            title = "Transacciones",
            value = totalTransacciones.toString(),
            icon = Icons.Default.AccountBalance,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Metas Activas",
            value = metasActivas.toString(),
            icon = Icons.Default.TrendingUp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color(0xFF00E5FF).copy(alpha = 0.1f)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A2A3A).copy(alpha = 0.9f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF2A2A3A),
                            Color(0xFF1E1E2E)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.7f)
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ChartSection(
    datosGrafico: List<Any>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color(0xFF00E5FF).copy(alpha = 0.1f)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A2A3A).copy(alpha = 0.9f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF2A2A3A),
                            Color(0xFF1E1E2E)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                SectionHeader(
                    title = "Gastos por Categoría",
                    subtitle = "Distribución mensual",
                    textColor = Color.White
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Contenedor con scroll horizontal para el gráfico
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 8.dp)
                    ) {
                        BarraAgrupadaGrafico(
                            datos = datosGrafico as List<CategoriaConTotales>,
                            modifier = Modifier
                                .width(maxOf(300.dp, 80.dp * datosGrafico.size
                                )) // Ancho dinámico
                                .height(280.dp)
                        )
                    }
                }

                // Indicador de scroll
                if (datosGrafico.size > 4) {
                    Text(
                        text = "← Desliza para ver más →",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF00E5FF).copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    textColor: Color = Color.White
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                tint = Color(0xFF00E5FF),
                modifier = Modifier
                    .size(28.dp)
                    .padding(end = 8.dp)
            )
        }
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = textColor.copy(alpha = 0.7f)
                    )
                )
            }
        }
    }
}

@Composable
private fun GoalCard(meta: Meta) {
    val progreso = meta.progreso ?: 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color(0xFF00E5FF).copy(alpha = 0.1f)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A2A3A).copy(alpha = 0.9f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF2A2A3A),
                            Color(0xFF1E1E2E)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = meta.categoria,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E5FF)
                            )
                        )
                        Text(
                            text = "${meta.cantidad} €",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF00E5FF).copy(alpha = 0.3f),
                                        Color(0xFF00E5FF).copy(alpha = 0.1f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${(progreso * 100).toInt()}%",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = Color(0xFF00E5FF),
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val fechaFormatted = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    .format(Date(meta.fechaLimite))
                Text(
                    text = "Vence: $fechaFormatted",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.6f)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = progreso,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFF00E5FF),
                    trackColor = Color.White.copy(alpha = 0.2f)
                )
            }
        }
    }
}