package com.example.proyectodef.ui.screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.proyectodef.ui.components.AppDrawer
import com.example.proyectodef.viewmodel.AuthViewModel
import com.example.proyectodef.viewmodel.MetaViewModel
import com.example.proyectodef.viewmodel.TransactionViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetasScreen(
    authViewModel: AuthViewModel,
    metaViewModel: MetaViewModel,
    transactionViewModel: TransactionViewModel,
    navController: NavHostController
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showPopup by remember { mutableStateOf(false) }

    val user by authViewModel.user.collectAsState()
    val userId = user?.userId.orEmpty()
    val categorias = transactionViewModel.categorias.collectAsState().value.map { it.nombre }
    val metas by metaViewModel.metas.collectAsState()
    val metasEnProceso = metas.filter { it.estado == "Proceso" }

    LaunchedEffect(userId) {
        if (userId.isNotBlank()) {
            metaViewModel.cargarMetas(userId)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                drawerState = drawerState,
                scope = scope,
                navController = navController
            )
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF0A0A0F),
                            Color(0xFF16213E).copy(alpha = 0.8f),
                            Color(0xFF0099FF).copy(alpha = 0.3f),
                            Color(0xFFCC00FF).copy(alpha = 0.2f)
                        ),
                        radius = 1200f
                    )
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xFF00FFFF).copy(alpha = 0.1f),
                                Color(0xFF00FFCC).copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    )
            ) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    "Mis Metas",
                                    color = Color(0xFF00FFFF),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                            },
                            navigationIcon = {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(
                                        Icons.Default.Menu,
                                        contentDescription = "Menú",
                                        tint = Color(0xFFFF66B3)
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
                            onClick = {
                                showPopup = true
                            },
                            containerColor = Color(0xFFFF0080),
                            contentColor = Color.White,
                            modifier = Modifier
                                .shadow(
                                    elevation = 20.dp,
                                    shape = RoundedCornerShape(16.dp),
                                    ambientColor = Color(0xFFFF0080),
                                    spotColor = Color(0xFFFF0080)
                                )
                                .border(
                                    width = 2.dp,
                                    color = Color(0xFFFF66B3).copy(alpha = 0.7f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Añadir Meta")
                        }
                    },
                    containerColor = Color.Transparent,
                    modifier = Modifier.fillMaxSize()
                ) { padding ->
                    Column(
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(metasEnProceso) { meta ->
                                var expandedMenu by remember { mutableStateOf(false) }

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFF1A1A2E).copy(alpha = 0.9f)
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(20.dp)) {
                                            Text("🎯 ${meta.categoria}", fontWeight = FontWeight.Bold, color = Color(0xFF00FFFF), fontSize = 18.sp)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text("Tipo: ${meta.tipo}", color = Color(0xFF00FFCC), fontSize = 15.sp)
                                            Text("💰 %.2f €".format(meta.cantidad), color = Color(0xFFFF66B3), fontSize = 16.sp)

                                            val fechaFormatted = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(meta.fechaLimite))
                                            Text("Fecha límite: $fechaFormatted", color = Color(0xFFCC00FF), fontSize = 14.sp)

                                            Text("🚀 ${meta.estado}", color = Color.White, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
                                        }

                                        Box(modifier = Modifier.align(Alignment.TopEnd)) {
                                            IconButton(onClick = { expandedMenu = true }) {
                                                Icon(Icons.Default.MoreVert, contentDescription = "Opciones", tint = Color(0xFFFF66B3))
                                            }
                                            DropdownMenu(
                                                expanded = expandedMenu,
                                                onDismissRequest = { expandedMenu = false }
                                            ) {
                                                DropdownMenuItem(
                                                    text = { Text("Marcar como completada", color = Color(0xFF00FFCC)) },
                                                    onClick = {
                                                        metaViewModel.marcarMetaComoCompletada(userId, meta.id)
                                                        expandedMenu = false
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text(" Eliminar", color = Color(0xFFFF0080)) },
                                                    onClick = {
                                                        metaViewModel.eliminarMeta(userId, meta.id)
                                                        expandedMenu = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (showPopup && userId.isNotBlank()) {
                            PopupMeta(
                                userId = userId,
                                categorias = categorias,
                                onDismiss = { showPopup = false },
                                onConfirm = {
                                    showPopup = false
                                },
                                viewModel = metaViewModel
                            )
                        }
                    }
                }
            }
        }
    }
}
