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

    val colorAhorros = Color(0xFFB8E6FF)
    val colorGastos = Color(0xFFFFB8D6)
    val colorTexto = Color(0xFF6B7280)
    val colorFondo = Color(0xFFFAFBFC)
    val colorLineas = Color(0xFFE5E7EB)

    Column(
        modifier = modifier
            .background(
                color = colorFondo,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                val margenIzquierdo = 50.dp.toPx()
                val anchoGrafico = size.width - margenIzquierdo
                val alturaTotal = size.height - 40.dp.toPx()
                val maxValor = (datos.flatMap { listOf(it.totalAhorros, it.totalGastos) }
                    .maxOrNull() ?: 100.0).coerceAtLeast(100.0)
                val numCategorias = datos.size
                val espacioPorCategoria = anchoGrafico / numCategorias
                val anchoBarra = (espacioPorCategoria / 5).coerceAtLeast(12.dp.toPx())
                val espacioEntreBarras = 4.dp.toPx()

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
                        drawText(
                            "${valor}€",
                            margenIzquierdo - 8.dp.toPx(),
                            y + 4.dp.toPx(),
                            paint
                        )
                    }
                    if (i > 0) {
                        drawLine(
                            color = colorLineas,
                            start = Offset(margenIzquierdo - 4.dp.toPx(), y),
                            end = Offset(margenIzquierdo, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                }
                drawLine(
                    color = colorLineas,
                    start = Offset(margenIzquierdo, 0f),
                    end = Offset(margenIzquierdo, alturaTotal),
                    strokeWidth = 1.dp.toPx()
                )
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