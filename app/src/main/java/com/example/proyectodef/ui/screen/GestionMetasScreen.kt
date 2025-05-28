package com.example.proyectodef.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.proyectodef.model.Meta
import com.example.proyectodef.ui.components.AppDrawer
import com.example.proyectodef.viewmodel.AuthViewModel
import com.example.proyectodef.viewmodel.MetaViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionMetasScreen(
    authViewModel: AuthViewModel,
    metaViewModel: MetaViewModel,
    navController: NavHostController
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val user by authViewModel.user.collectAsState()
    val userId = user?.userId.orEmpty()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Completadas", "Expiradas")

    val metas by metaViewModel.metas.collectAsState()
    val metasFiltradas = when (selectedTab) {
        0 -> metas.filter { it.estado == "Completado" }
        1 -> metas.filter { it.estado == "Expirado" }
        else -> emptyList()
    }

    LaunchedEffect(userId) {
        if (userId.isNotBlank()) {
            metaViewModel.cargarMetas(userId)
        }
    }

    // Colores neón vibrantes
    val neonColors = listOf(
        Color(0xFF00FFFF), // Celeste neón
        Color(0xFFFFB3E6), // Rosa claro
        Color(0xFFFF1493), // Rosa fucsia
        Color(0xFF00BFFF), // Azul neón
        Color(0xFF00FFCC), // Verde agua
        Color(0xFFDA70D6)  // Lila
    )

    // Fondo animado con degradado neón
    val infiniteTransition = rememberInfiniteTransition()
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2000f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val backgroundBrush = Brush.radialGradient(
        colors = neonColors.map { it.copy(alpha = 0.3f) },
        center = Offset(offset * 0.5f, offset * 0.3f),
        radius = maxOf(offset * 0.8f, 1f) // ← protege contra radius = 0
    )


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
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0A0A0A),
                            Color(0xFF1A1A2E),
                            Color(0xFF16213E)
                        )
                    )
                )
        ) {
            // Fondo animado superpuesto
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundBrush)
            )

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFF00FFFF),
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "Gestión de Metas",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = { scope.launch { drawerState.open() } },
                                modifier = Modifier
                                    .padding(8.dp)
                                    .background(
                                        Color(0xFF00FFFF).copy(alpha = 0.2f),
                                        CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menú",
                                    tint = Color(0xFF00FFFF)
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                },
                containerColor = Color.Transparent,
                modifier = Modifier.fillMaxSize()
            ) { padding ->
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Tabs con diseño neón
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Black.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = Color.Transparent,
                            contentColor = Color.White,
                            indicator = { tabPositions ->
                                Box(
                                    modifier = Modifier
                                        .tabIndicatorOffset(tabPositions[selectedTab])
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(
                                                    Color(0xFF00FFFF),
                                                    Color(0xFFFF1493)
                                                )
                                            )
                                        )
                                )
                            },
                            modifier = Modifier.padding(8.dp)
                        ) {
                            tabs.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedTab == index,
                                    onClick = { selectedTab = index },
                                    modifier = Modifier
                                        .padding(vertical = 8.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                ) {
                                    Text(
                                        text = title,
                                        color = if (selectedTab == index)
                                            Color(0xFF00FFFF)
                                        else
                                            Color.White.copy(alpha = 0.6f),
                                        fontWeight = if (selectedTab == index)
                                            FontWeight.Bold
                                        else
                                            FontWeight.Medium,
                                        fontSize = 16.sp,
                                        modifier = Modifier.padding(vertical = 12.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (metasFiltradas.isEmpty()) {
                        // Estado vacío con diseño atractivo
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.Black.copy(alpha = 0.3f)
                                ),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(48.dp)
                                ) {
                                    Icon(
                                        imageVector = if (selectedTab == 0) Icons.Default.CheckCircle else Icons.Default.Error,
                                        contentDescription = null,
                                        tint = if (selectedTab == 0) Color(0xFF00FFCC) else Color(0xFFFF1493),
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        "No hay metas ${tabs[selectedTab].lowercase()}",
                                        color = Color.White,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        if (selectedTab == 0)
                                            "¡Sigue trabajando en tus objetivos!"
                                        else
                                            "Mantén el enfoque en tus metas actuales",
                                        color = Color.White.copy(alpha = 0.7f),
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(metasFiltradas) { meta ->
                                MetaCard(meta = meta, isCompleted = selectedTab == 0)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetaCard(meta: Meta, isCompleted: Boolean) {
    val borderColor = if (isCompleted) Color(0xFF00FFCC) else Color(0xFFFF1493)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = borderColor.copy(alpha = 0.3f),
                spotColor = borderColor.copy(alpha = 0.3f)
            )
            .border(
                width = 1.dp,
                color = borderColor.copy(alpha = 0.5f),
                shape = RoundedCornerShape(20.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        if (isCompleted) Color(0xFF00FFCC).copy(alpha = 0.2f)
                        else Color(0xFFFF1493).copy(alpha = 0.2f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (isCompleted) Color(0xFF00FFCC) else Color(0xFFFF1493),
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(Color.Transparent) // ← transparencia explícita
            ) {
                Text(
                    text = meta.categoria,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00FFFF),
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFB3E6), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tipo: ${meta.tipo}", fontSize = 14.sp, color = Color(0xFFFFB3E6))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AttachMoney,
                        contentDescription = null,
                        tint = Color(0xFF00FFCC),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${String.format("%.2f", meta.cantidad)} €",
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, null, tint = Color(0xFFDA70D6), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    val fechaFormatted = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(meta.fechaLimite))
                    Text(fechaFormatted, fontSize = 14.sp, color = Color(0xFFDA70D6))
                }
            }

        }
    }
}
