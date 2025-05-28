import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectodef.viewmodel.CategoriaConTotales

@Composable
fun BarraAgrupadaGrafico(
    datos: List<CategoriaConTotales>,
    modifier: Modifier = Modifier,
    alturaGrafico: Dp = 280.dp
) {
    // Colores pasteles minimalistas
    val colorAhorros = Color(0xFFB8E6FF) // Celeste pastel
    val colorGastos = Color(0xFFFFB8D6) // Rosa pastel
    val colorTexto = Color(0xFF6B7280) // Gris suave
    val colorFondo = Color(0xFFFAFBFC) // Blanco casi puro
    val colorLineas = Color(0xFFE5E7EB) // Gris muy claro

    Column(
        modifier = modifier
            .background(
                color = colorFondo,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(20.dp)
    ) {
        // Leyenda en la parte superior
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicador Ahorros
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(
                        color = colorAhorros.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(colorAhorros)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Ahorros",
                    color = colorTexto,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.width(16.dp))

            // Indicador Gastos
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(
                        color = colorGastos.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(colorGastos)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Gastos",
                    color = colorTexto,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Box(modifier = Modifier.height(alturaGrafico).fillMaxWidth()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Reservamos más espacio para las etiquetas del eje Y
                val margenIzquierdo = 50.dp.toPx()
                val anchoGrafico = size.width - margenIzquierdo
                val alturaTotal = size.height - 40.dp.toPx() // Espacio para etiquetas del eje X

                val maxValor = (datos.flatMap { listOf(it.totalAhorros, it.totalGastos) }
                    .maxOrNull() ?: 100.0).coerceAtLeast(100.0)

                val numCategorias = datos.size
                val espacioPorCategoria = anchoGrafico / numCategorias
                val anchoBarra = (espacioPorCategoria / 5).coerceAtLeast(12.dp.toPx())
                val espacioEntreBarras = 4.dp.toPx()

                // Líneas de fondo más sutiles
                for (i in 1..4) {
                    val y = alturaTotal - (i * alturaTotal / 5)
                    drawLine(
                        color = colorLineas,
                        start = Offset(margenIzquierdo, y),
                        end = Offset(size.width, y),
                        strokeWidth = 0.5.dp.toPx()
                    )
                }

                datos.forEachIndexed { index, categoria ->
                    val xCentroCategoria = margenIzquierdo + espacioPorCategoria * index + espacioPorCategoria / 2

                    val alturaAhorros = (categoria.totalAhorros / maxValor * alturaTotal).toFloat()
                    val alturaGastos = (categoria.totalGastos / maxValor * alturaTotal).toFloat()

                    val xAhorros = xCentroCategoria - (anchoBarra + espacioEntreBarras / 2)
                    val xGastos = xCentroCategoria + espacioEntreBarras / 2

                    // Barras con esquinas redondeadas y gradientes sutiles
                    if (alturaAhorros > 0f) {
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    colorAhorros.copy(alpha = 0.9f),
                                    colorAhorros.copy(alpha = 0.7f)
                                )
                            ),
                            topLeft = Offset(xAhorros, alturaTotal - alturaAhorros),
                            size = Size(anchoBarra, alturaAhorros),
                            cornerRadius = CornerRadius(6.dp.toPx())
                        )
                    }

                    if (alturaGastos > 0f) {
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    colorGastos.copy(alpha = 0.9f),
                                    colorGastos.copy(alpha = 0.7f)
                                )
                            ),
                            topLeft = Offset(xGastos, alturaTotal - alturaGastos),
                            size = Size(anchoBarra, alturaGastos),
                            cornerRadius = CornerRadius(6.dp.toPx())
                        )
                    }

                    // Etiquetas de categorías
                    drawContext.canvas.nativeCanvas.apply {
                        val paint = android.graphics.Paint().apply {
                            color = android.graphics.Color.parseColor("#6B7280")
                            textSize = 11.sp.toPx()
                            textAlign = android.graphics.Paint.Align.CENTER
                            isAntiAlias = true
                        }
                        drawText(
                            categoria.nombre,
                            xCentroCategoria,
                            alturaTotal + 20.dp.toPx(),
                            paint
                        )
                    }
                }

                // Etiquetas del eje Y mejoradas con más espacio
                for (i in 0..5) {
                    val y = alturaTotal - (i * alturaTotal / 5)
                    val valor = (maxValor * i / 5).toInt()

                    drawContext.canvas.nativeCanvas.apply {
                        val paint = android.graphics.Paint().apply {
                            color = android.graphics.Color.parseColor("#9CA3AF")
                            textSize = 11.sp.toPx()
                            textAlign = android.graphics.Paint.Align.RIGHT
                            isAntiAlias = true
                        }

                        // Posicionamos el texto con más margen desde el borde izquierdo
                        drawText(
                            "${valor}€",
                            margenIzquierdo - 8.dp.toPx(),
                            y + 4.dp.toPx(),
                            paint
                        )
                    }

                    // Línea horizontal de referencia
                    if (i > 0) {
                        drawLine(
                            color = colorLineas,
                            start = Offset(margenIzquierdo - 4.dp.toPx(), y),
                            end = Offset(margenIzquierdo, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                }

                // Eje Y principal
                drawLine(
                    color = colorLineas,
                    start = Offset(margenIzquierdo, 0f),
                    end = Offset(margenIzquierdo, alturaTotal),
                    strokeWidth = 1.dp.toPx()
                )

                // Eje X principal
                drawLine(
                    color = colorLineas,
                    start = Offset(margenIzquierdo, alturaTotal),
                    end = Offset(size.width, alturaTotal),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }
    }
}