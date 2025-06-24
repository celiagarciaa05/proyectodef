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
import com.example.proyectodef.model.Transaction
import com.example.proyectodef.viewmodel.MetaViewModel
import com.example.proyectodef.viewmodel.ProgresoViewModel
import com.example.proyectodef.viewmodel.TransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PopupTransaccion(
    userId: String,
    categorias: List<String>,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    viewModel: TransactionViewModel,
    onUpdateDinero: (Double, String) -> Unit,
    metaViewModel: MetaViewModel,
    progresoViewModel: ProgresoViewModel
)

{
    val neonCyan = Color(0xFF00FFFF)
    val neonBlue = Color(0xFF0099FF)
    val neonAqua = Color(0xFF00FFCC)
    val neonPurple = Color(0xFFCC00FF)
    val neonPink = Color(0xFFFF0080)
    val neonLightPink = Color(0xFFFF66B3)
    val neonYellow = Color(0xFFFFFF00)
    val darkBackground = Color(0xFF0A0A0F)
    val cardBackground = Color(0xFF1A1A2E)

    var tipo by remember { mutableStateOf("Ahorro") }
    var fecha by remember { mutableStateOf("") }
    var titulo by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf("") }

    var expandedCategoria by remember { mutableStateOf(false) }
    var nuevaCategoria by remember { mutableStateOf("") }

    val showNewCategoryField = categoriaSeleccionada == "Añadir nueva categoría"
    var error by remember { mutableStateOf<String?>(null) }

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
                    if (
                        fecha.isBlank() || titulo.isBlank() ||
                        cantidad.toDoubleOrNull() == null || descripcion.isBlank() ||
                        (showNewCategoryField && nuevaCategoria.isBlank()) ||
                        (!showNewCategoryField && categoriaSeleccionada.isBlank())
                    ) {
                        error = "Todos los campos deben estar completos y válidos"
                        return@Button
                    }

                    val categoriaFinal = if (showNewCategoryField) nuevaCategoria else categoriaSeleccionada

                    val transaction = Transaction(
                        id = "",
                        userId = userId,
                        tipo = tipo,
                        fecha = System.currentTimeMillis(),
                        titulo = titulo,
                        cantidad = cantidad.toDouble(),
                        descripcion = descripcion,
                        categoria = categoriaFinal
                    )

                    if (showNewCategoryField) {
                        viewModel.agregarCategoria(userId, nuevaCategoria)
                    }

                    viewModel.agregarTransaccion(transaction)
                    onUpdateDinero(transaction.cantidad, transaction.tipo)

                    metaViewModel.cargarMetas(userId)
                    metaViewModel.metas.value.let { metas ->
                        progresoViewModel.calcularYActualizarProgresoMetas(userId, metas)
                    }

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
                    "✨ Aceptar",
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
                    "💳 Nueva Transacción",
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
                    .padding(vertical = 8.dp)
                    .fillMaxHeight(0.85f)
                    .verticalScroll(rememberScrollState())
            ) {

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = cardBackground.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = neonBlue.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "💰 Tipo de Transacción",
                            color = neonBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                RadioButton(
                                    selected = tipo == "Ahorro",
                                    onClick = { tipo = "Ahorro" },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = neonAqua,
                                        unselectedColor = neonAqua.copy(alpha = 0.6f)
                                    )
                                )
                                Text(
                                    "🏦 Ahorro",
                                    color = if (tipo == "Ahorro") neonAqua else Color.White.copy(alpha = 0.7f),
                                    fontWeight = if (tipo == "Ahorro") FontWeight.Bold else FontWeight.Normal
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                RadioButton(
                                    selected = tipo == "Gasto",
                                    onClick = { tipo = "Gasto" },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = neonPink,
                                        unselectedColor = neonPink.copy(alpha = 0.6f)
                                    )
                                )
                                Text(
                                    "🛒 Gasto",
                                    color = if (tipo == "Gasto") neonPink else Color.White.copy(alpha = 0.7f),
                                    fontWeight = if (tipo == "Gasto") FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = fecha,
                    onValueChange = { fecha = it; error = null },
                    label = {
                        Text(
                            "📅 Fecha (yyyy-mm-dd)",
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

                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it; error = null },
                    label = {
                        Text(
                            "📝 Título",
                            color = neonCyan
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = neonCyan,
                        unfocusedBorderColor = neonCyan.copy(alpha = 0.6f),
                        cursorColor = neonCyan,
                        focusedLabelColor = neonCyan,
                        unfocusedLabelColor = neonCyan.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = cantidad,
                    onValueChange = { cantidad = it; error = null },
                    label = {
                        Text(
                            "💵 Cantidad",
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
                    value = descripcion,
                    onValueChange = { descripcion = it; error = null },
                    label = {
                        Text(
                            "📋 Descripción",
                            color = neonYellow
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = neonYellow,
                        unfocusedBorderColor = neonYellow.copy(alpha = 0.6f),
                        cursorColor = neonCyan,
                        focusedLabelColor = neonYellow,
                        unfocusedLabelColor = neonYellow.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

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
                        expanded = expandedCategoria,
                        onExpandedChange = { expandedCategoria = !expandedCategoria },
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
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedCategoria,
                            onDismissRequest = { expandedCategoria = false },
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
                            (categorias + "Añadir nueva categoría").forEach { cat ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            if (cat == "Añadir nueva categoría") "➕ $cat" else cat,
                                            color = if (cat == categoriaSeleccionada) neonCyan
                                            else if (cat == "Añadir nueva categoría") neonLightPink
                                            else Color.White
                                        )
                                    },
                                    onClick = {
                                        categoriaSeleccionada = cat
                                        expandedCategoria = false
                                        error = null
                                    }
                                )
                            }
                        }
                    }
                }

                if (showNewCategoryField) {
                    OutlinedTextField(
                        value = nuevaCategoria,
                        onValueChange = { nuevaCategoria = it; error = null },
                        label = {
                            Text(
                                "✨ Nueva categoría",
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
                }

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