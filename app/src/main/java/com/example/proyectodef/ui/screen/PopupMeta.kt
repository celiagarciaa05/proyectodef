package com.example.proyectodef.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.window.DialogProperties
import com.example.proyectodef.model.Meta
import com.example.proyectodef.viewmodel.MetaViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PopupMeta(
    userId: String,
    categorias: List<String>,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    viewModel: MetaViewModel
) {
    val neonCyan = Color(0xFF00FFFF)
    val neonBlue = Color(0xFF0099FF)
    val neonAqua = Color(0xFF00FFCC)
    val neonPurple = Color(0xFFCC00FF)
    val neonPink = Color(0xFFFF0080)
    val neonLightPink = Color(0xFFFF66B3)
    val darkBackground = Color(0xFF0A0A0F)
    val cardBackground = Color(0xFF1A1A2E)

    var categoriaSeleccionada by remember { mutableStateOf(categorias.firstOrNull() ?: "") }
    var tipo by remember { mutableStateOf("Gasto") }
    var cantidad by remember { mutableStateOf("") }
    var fechaTexto by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }

    val formatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val scrollState = rememberScrollState()
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .shadow(
                elevation = 25.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = neonCyan,
                spotColor = neonPurple
            ),
        confirmButton = {
            Button(
                onClick = {
                    val cantidadVal = cantidad.toDoubleOrNull()
                    val fechaDate = try {
                        formatter.parse(fechaTexto)
                    } catch (e: Exception) {
                        null
                    }

                    if (categoriaSeleccionada.isBlank() || cantidadVal == null || fechaDate == null) {
                        error = "Todos los campos deben estar completos y válidos."
                        return@Button
                    }

                    if (fechaDate.before(Date())) {
                        error = "La fecha debe ser futura."
                        return@Button
                    }

                    val meta = Meta(
                        id = "",
                        userId = userId,
                        categoria = categoriaSeleccionada,
                        tipo = tipo,
                        cantidad = cantidadVal,
                        fechaLimite = fechaDate.time,
                        fechaCreacion = System.currentTimeMillis(),
                        estado = "Proceso",
                        progreso = 0f
                    )


                    viewModel.agregarMeta(meta)
                    onConfirm()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = neonPink,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(12.dp),
                        ambientColor = neonPink,
                        spotColor = neonPink
                    )
                    .border(
                        width = 1.5.dp,
                        color = neonLightPink.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "✨ Guardar",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = neonCyan
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = Brush.horizontalGradient(
                        colors = listOf(neonCyan, neonAqua)
                    ),
                    width = 1.5.dp
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "❌ Cancelar",
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
            }
        },
        title = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                neonCyan.copy(alpha = 0.1f),
                                neonPurple.copy(alpha = 0.1f)
                            )
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = neonCyan.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "🎯 Nueva Meta",
                    color = neonCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxHeight(0.7f)
                    .verticalScroll(scrollState)
                    .padding(vertical = 8.dp)
            ) {

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = cardBackground.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = neonAqua.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.padding(4.dp)
                    ) {
                        OutlinedTextField(
                            value = categoriaSeleccionada,
                            onValueChange = {},
                            readOnly = true,
                            label = {
                                Text(
                                    "📂 Categoría",
                                    color = neonAqua
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = neonAqua,
                                unfocusedBorderColor = neonAqua.copy(alpha = 0.6f),
                                cursorColor = neonCyan
                            ),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier
                                .background(
                                    cardBackground.copy(alpha = 0.95f),
                                    RoundedCornerShape(12.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = neonCyan.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                        ) {
                            categorias.forEach { cat ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            cat,
                                            color = if (cat == categoriaSeleccionada) neonCyan else Color.White
                                        )
                                    },
                                    onClick = {
                                        categoriaSeleccionada = cat
                                        expanded = false
                                        error = null
                                    }
                                )
                            }
                        }
                    }
                }


                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = cardBackground.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = neonPurple.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "💰 Tipo de Meta",
                            color = neonPurple,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = { tipo = "Gasto" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (tipo == "Gasto") neonPink else Color.Transparent,
                                    contentColor = if (tipo == "Gasto") Color.White else neonPink
                                ),
                                border = if (tipo != "Gasto") {
                                    ButtonDefaults.outlinedButtonBorder.copy(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(neonPink, neonLightPink)
                                        ),
                                        width = 1.5.dp
                                    )
                                } else null,
                                modifier = Modifier
                                    .weight(1f)
                                    .shadow(
                                        elevation = if (tipo == "Gasto") 8.dp else 0.dp,
                                        shape = RoundedCornerShape(10.dp),
                                        ambientColor = neonPink
                                    ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    "🛒 Gasto",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Button(
                                onClick = { tipo = "Ahorro" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (tipo == "Ahorro") neonBlue else Color.Transparent,
                                    contentColor = if (tipo == "Ahorro") Color.White else neonBlue
                                ),
                                border = if (tipo != "Ahorro") {
                                    ButtonDefaults.outlinedButtonBorder.copy(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(neonBlue, neonCyan)
                                        ),
                                        width = 1.5.dp
                                    )
                                } else null,
                                modifier = Modifier
                                    .weight(1f)
                                    .shadow(
                                        elevation = if (tipo == "Ahorro") 8.dp else 0.dp,
                                        shape = RoundedCornerShape(10.dp),
                                        ambientColor = neonBlue
                                    ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    "🏦 Ahorro",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = neonCyan.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    neonCyan.copy(alpha = 0.6f),
                                    neonLightPink.copy(alpha = 0.6f)
                                )
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "🎯 Meta: No puedo ${if (tipo == "Ahorro") "ahorrar menos" else "gastar más"} de ${cantidad.ifBlank { "X" }}€ " +
                                "${if (tipo == "Ahorro") "antes de" else "hasta"} el ${fechaTexto.ifBlank { "XX-XX-XXXX" }}.",
                        fontSize = 15.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                OutlinedTextField(
                    value = cantidad,
                    onValueChange = {
                        cantidad = it
                        error = null
                    },
                    label = {
                        Text(
                            "💵 Cantidad (€)",
                            color = neonLightPink
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = neonLightPink,
                        unfocusedBorderColor = neonLightPink.copy(alpha = 0.6f),
                        cursorColor = neonCyan,
                        focusedLabelColor = neonLightPink,
                        unfocusedLabelColor = neonLightPink.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = fechaTexto,
                    onValueChange = {
                        fechaTexto = it
                        error = null
                    },
                    label = {
                        Text(
                            "📅 Fecha límite (yyyy-MM-dd)",
                            color = neonPurple
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = neonPurple,
                        unfocusedBorderColor = neonPurple.copy(alpha = 0.6f),
                        cursorColor = neonCyan,
                        focusedLabelColor = neonPurple,
                        unfocusedLabelColor = neonPurple.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )



                error?.let {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = neonPink.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier
                            .border(
                                width = 1.dp,
                                color = neonPink.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(8.dp)
                            ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "⚠️ $it",
                            style = MaterialTheme.typography.bodyMedium,
                            color = neonLightPink,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        },
        containerColor = cardBackground.copy(alpha = 0.95f),
        shape = RoundedCornerShape(24.dp)
    )
}