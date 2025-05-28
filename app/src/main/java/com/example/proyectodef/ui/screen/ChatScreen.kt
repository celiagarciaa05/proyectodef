package com.example.proyectodef.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.proyectodef.ui.components.AppDrawer
import com.example.proyectodef.viewmodel.ChatViewModel
import com.example.proyectodef.viewmodel.AuthViewModel
import com.example.proyectodef.viewmodel.TransactionViewModel
import com.example.proyectodef.viewmodel.MetaViewModel
import com.example.proyectodef.utils.UserDataProvider
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatViewModel: ChatViewModel,
    authViewModel: AuthViewModel,
    transactionViewModel: TransactionViewModel,
    metaViewModel: MetaViewModel,
    navController: NavHostController
) {
    val user by authViewModel.user.collectAsState()
    val transacciones = transactionViewModel.transaccionesFiltradas.collectAsState().value
    val categorias = transactionViewModel.categorias.collectAsState().value
    val metas = metaViewModel.metas.collectAsState().value

    val userContext = remember(user, transacciones, categorias, metas) {
        UserDataProvider.generarResumenFinanciero(
            nombre = user?.nombreUsuario ?: "Desconocido",
            correo = user?.correo ?: "desconocido@email.com",
            dineroTotal = user?.dineroTotal ?: 0.0,
            transacciones = transacciones,
            categorias = categorias,
            metas = metas
        )
    }

    var input by remember { mutableStateOf("") }
    val history = chatViewModel.chatHistory
    val isCargando = chatViewModel.cargando

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

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
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF2C003E),
                            Color(0xFF7B1FA2),
                            Color(0xFF03DAC5),
                            Color(0xFFBB86FC),
                            Color(0xFF001F3F)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(1000f, 1500f)
                    )
                )
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Asistente Financiero", color = Color.White) },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menú", tint = Color.White)
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
                        .padding(16.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(history) { (q, a) ->
                            if (q.isNotBlank()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .widthIn(max = 280.dp)
                                            .background(
                                                color = Color(0xFF03DAC5).copy(alpha = 0.8f),
                                                shape = MaterialTheme.shapes.medium
                                            )
                                            .padding(12.dp)
                                    ) {
                                        Text("Tú", fontWeight = FontWeight.Bold, color = Color.White)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(q, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                                    }
                                }
                            }

                            if (a.isNotBlank()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .widthIn(max = 280.dp)
                                            .background(
                                                color = Color(0xFFBB86FC).copy(alpha = 0.85f),
                                                shape = MaterialTheme.shapes.medium
                                            )
                                            .padding(12.dp)
                                    ) {
                                        Text("Bot", fontWeight = FontWeight.Bold, color = Color.White)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(a, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                                    }
                                }
                            }
                        }

                        if (isCargando) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Text(
                                        "Bot está escribiendo...",
                                        fontStyle = FontStyle.Italic,
                                        color = Color.White.copy(alpha = 0.7f),
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        label = { Text("Escribe tu duda financiera...", color = Color.White) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF03DAC5),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
                            focusedLabelColor = Color(0xFF03DAC5),
                            unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                            cursorColor = Color(0xFF03DAC5),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (user != null) {
                                chatViewModel.enviarPreguntaConContextoTotal(
                                    pregunta = input,
                                    userId = user!!.userId,
                                    onRespuesta = {}
                                )
                                input = ""
                            }
                        },
                        enabled = input.isNotBlank(),
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBB86FC))
                    ) {
                        Text("Enviar", color = Color.White)
                    }

                }
            }
        }
    }
}
