package com.example.proyectodef.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import com.example.proyectodef.model.User
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.SolidBorder
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.itextpdf.layout.properties.VerticalAlignment
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ResumenPdfGenerator {

    // Colores corporativos
    private val COLOR_PRIMARIO = DeviceRgb(41, 128, 185)  // Azul profesional
    private val COLOR_SECUNDARIO = DeviceRgb(52, 73, 94)  // Gris oscuro
    private val COLOR_EXITO = DeviceRgb(39, 174, 96)      // Verde
    private val COLOR_ERROR = DeviceRgb(231, 76, 60)      // Rojo
    private val COLOR_GRIS_CLARO = DeviceRgb(236, 240, 241)

    suspend fun generarYEnviar(
        context: Context,
        user: User,
        transacciones: List<DocumentSnapshot>,
        firestore: FirebaseFirestore
    ) {
        val ahora = System.currentTimeMillis()
        val unMesAtras = ahora - 30L * 24 * 60 * 60 * 1000

        val metas = firestore.collection("usuarios")
            .document(user.userId)
            .collection("metas")
            .get().await()
            .documents
            .filter { (it.getLong("fechaLimite") ?: 0L) in unMesAtras..ahora }

        val file = File(context.cacheDir, "Resumen_Financiero_${formatDateForFile(ahora)}.pdf")
        val pdfDoc = PdfDocument(PdfWriter(file))
        val doc = Document(pdfDoc)

        // Configurar márgenes
        doc.setMargins(50f, 50f, 50f, 50f)

        // Generar contenido del PDF
        agregarEncabezado(doc, ahora)
        agregarInformacionUsuario(doc, user)
        agregarResumenEjecutivo(doc, transacciones, user.dineroTotal)
        agregarSeccionMetas(doc, metas)
        agregarSeccionTransacciones(doc, transacciones)
        agregarPiePagina(doc)

        doc.close()

        enviarPorCorreo(context, file, user)
    }

    private fun agregarEncabezado(doc: Document, fechaGeneracion: Long) {
        // Título principal
        val titulo = Paragraph("REPORTE FINANCIERO MENSUAL")
            .setTextAlignment(TextAlignment.CENTER)
            .setFontSize(24f)
            .setBold()
            .setFontColor(COLOR_PRIMARIO)
            .setMarginBottom(10f)
        doc.add(titulo)

        // Línea separadora
        val lineaSeparadora = Table(1)
            .useAllAvailableWidth()
            .setBorder(SolidBorder(COLOR_PRIMARIO, 2f))
        .setMarginBottom(20f)
        lineaSeparadora.addCell(Cell().setBorder(null).setHeight(5f))
        doc.add(lineaSeparadora)

        // Fecha de generación
        val fechaInfo = Paragraph("Generado el: ${formatDateComplete(fechaGeneracion)}")
            .setTextAlignment(TextAlignment.RIGHT)
            .setFontSize(10f)
            .setFontColor(COLOR_SECUNDARIO)
            .setMarginBottom(20f)
        doc.add(fechaInfo)
    }

    private fun agregarInformacionUsuario(doc: Document, user: User) {
        val tituloSeccion = Paragraph("INFORMACIÓN DEL USUARIO")
            .setFontSize(16f)
            .setBold()
            .setFontColor(COLOR_SECUNDARIO)
            .setMarginBottom(10f)
        doc.add(tituloSeccion)

        val tablaUsuario = Table(UnitValue.createPercentArray(floatArrayOf(1f, 2f)))
            .useAllAvailableWidth()
            .setMarginBottom(20f)

        // Estilo para las celdas de encabezado
        val estiloEncabezado = { texto: String ->
            Cell().add(Paragraph(texto).setBold().setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(COLOR_PRIMARIO)
                .setPadding(8f)
        }

        // Estilo para las celdas de datos
        val estiloDato = { texto: String ->
            Cell().add(Paragraph(texto))
                .setPadding(8f)
                .setBackgroundColor(COLOR_GRIS_CLARO)
        }

        tablaUsuario.addCell(estiloEncabezado("Campo"))
        tablaUsuario.addCell(estiloEncabezado("Información"))

        tablaUsuario.addCell(Cell().add(Paragraph("Nombre Completo").setBold()).setPadding(8f))
        tablaUsuario.addCell(estiloDato(user.nombreCompleto))

        tablaUsuario.addCell(Cell().add(Paragraph("Usuario").setBold()).setPadding(8f))
        tablaUsuario.addCell(estiloDato(user.nombreUsuario))

        tablaUsuario.addCell(Cell().add(Paragraph("Correo Electrónico").setBold()).setPadding(8f))
        tablaUsuario.addCell(estiloDato(user.correo))

        tablaUsuario.addCell(Cell().add(Paragraph("Saldo Actual").setBold()).setPadding(8f))
        tablaUsuario.addCell(estiloDato(formatCurrency(user.dineroTotal)))

        doc.add(tablaUsuario)
    }

    private fun agregarResumenEjecutivo(doc: Document, transacciones: List<DocumentSnapshot>, saldoActual: Double) {
        val tituloSeccion = Paragraph("RESUMEN EJECUTIVO")
            .setFontSize(16f)
            .setBold()
            .setFontColor(COLOR_SECUNDARIO)
            .setMarginBottom(10f)
        doc.add(tituloSeccion)

        // Calcular métricas
        var totalIngresos = 0.0
        var totalGastos = 0.0
        var transaccionesIngresos = 0
        var transaccionesGastos = 0

        transacciones.forEach { transaccion ->
            val tipo = transaccion.getString("tipo") ?: "Otro"
            val cantidad = transaccion.getDouble("cantidad") ?: 0.0

            if (tipo.equals("Gasto", ignoreCase = true)) {
                totalGastos += cantidad
                transaccionesGastos++
            } else {
                totalIngresos += cantidad
                transaccionesIngresos++
            }
        }

        val balanceNeto = totalIngresos - totalGastos
        val promedioGastos = if (transaccionesGastos > 0) totalGastos / transaccionesGastos else 0.0
        val promedioIngresos = if (transaccionesIngresos > 0) totalIngresos / transaccionesIngresos else 0.0

        // Tabla de resumen
        val tablaResumen = Table(UnitValue.createPercentArray(floatArrayOf(2f, 1f, 1f)))
            .useAllAvailableWidth()
            .setMarginBottom(20f)

        // Encabezados
        tablaResumen.addHeaderCell(
            Cell().add(Paragraph("CONCEPTO").setBold().setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(COLOR_PRIMARIO)
                .setPadding(10f)
                .setTextAlignment(TextAlignment.CENTER)
        )
        tablaResumen.addHeaderCell(
            Cell().add(Paragraph("CANTIDAD").setBold().setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(COLOR_PRIMARIO)
                .setPadding(10f)
                .setTextAlignment(TextAlignment.CENTER)
        )
        tablaResumen.addHeaderCell(
            Cell().add(Paragraph("PROMEDIO").setBold().setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(COLOR_PRIMARIO)
                .setPadding(10f)
                .setTextAlignment(TextAlignment.CENTER)
        )

        // Datos de ingresos
        tablaResumen.addCell(
            Cell().add(Paragraph("Total Ingresos").setFontColor(COLOR_EXITO).setBold())
                .setPadding(8f)
        )
        tablaResumen.addCell(
            Cell().add(Paragraph(formatCurrency(totalIngresos)).setFontColor(COLOR_EXITO).setBold())
                .setPadding(8f)
                .setTextAlignment(TextAlignment.RIGHT)
        )
        tablaResumen.addCell(
            Cell().add(Paragraph(formatCurrency(promedioIngresos)))
                .setPadding(8f)
                .setTextAlignment(TextAlignment.RIGHT)
        )

        // Datos de gastos
        tablaResumen.addCell(
            Cell().add(Paragraph("Total Gastos").setFontColor(COLOR_ERROR).setBold())
                .setPadding(8f)
        )
        tablaResumen.addCell(
            Cell().add(Paragraph(formatCurrency(totalGastos)).setFontColor(COLOR_ERROR).setBold())
                .setPadding(8f)
                .setTextAlignment(TextAlignment.RIGHT)
        )
        tablaResumen.addCell(
            Cell().add(Paragraph(formatCurrency(promedioGastos)))
                .setPadding(8f)
                .setTextAlignment(TextAlignment.RIGHT)
        )

        // Balance neto
        val colorBalance = if (balanceNeto >= 0) COLOR_EXITO else COLOR_ERROR
        tablaResumen.addCell(
            Cell().add(Paragraph("Balance Neto").setBold())
                .setPadding(8f)
                .setBackgroundColor(COLOR_GRIS_CLARO)
        )
        tablaResumen.addCell(
            Cell().add(Paragraph(formatCurrency(balanceNeto)).setBold().setFontColor(colorBalance))
                .setPadding(8f)
                .setTextAlignment(TextAlignment.RIGHT)
                .setBackgroundColor(COLOR_GRIS_CLARO)
        )
        tablaResumen.addCell(
            Cell().add(Paragraph("-"))
                .setPadding(8f)
                .setTextAlignment(TextAlignment.CENTER)
                .setBackgroundColor(COLOR_GRIS_CLARO)
        )

        doc.add(tablaResumen)
    }

    private fun agregarSeccionMetas(doc: Document, metas: List<DocumentSnapshot>) {
        val tituloSeccion = Paragraph("METAS FINANCIERAS")
            .setFontSize(16f)
            .setBold()
            .setFontColor(COLOR_SECUNDARIO)
            .setMarginBottom(10f)
        doc.add(tituloSeccion)

        if (metas.isEmpty()) {
            val sinMetas = Paragraph("No se encontraron metas establecidas para el período consultado.")
                .setFontColor(COLOR_SECUNDARIO)
                .setItalic()
                .setMarginBottom(20f)
            doc.add(sinMetas)
        } else {
            val tablaMetas = Table(UnitValue.createPercentArray(floatArrayOf(2f, 1.5f, 1.5f, 1f, 1f)))
                .useAllAvailableWidth()
                .setMarginBottom(20f)

            // Encabezados
            val encabezados = listOf("CATEGORÍA", "TIPO", "OBJETIVO", "ESTADO", "FECHA LÍMITE")
            encabezados.forEach { encabezado ->
                tablaMetas.addHeaderCell(
                    Cell().add(Paragraph(encabezado).setBold().setFontColor(ColorConstants.WHITE))
                        .setBackgroundColor(COLOR_PRIMARIO)
                        .setPadding(8f)
                        .setTextAlignment(TextAlignment.CENTER)
                )
            }

            // Datos de metas
            metas.forEachIndexed { index, meta ->
                val esPar = index % 2 == 0
                val colorFondo = if (esPar) ColorConstants.WHITE else COLOR_GRIS_CLARO

                val categoria = meta.getString("categoria") ?: "No especificada"
                val tipo = meta.getString("tipo") ?: "No especificado"
                val cantidad = formatCurrency(meta.getDouble("cantidad") ?: 0.0)
                val estado = meta.getString("estado") ?: "Pendiente"
                val fechaLimite = formatDate(meta.getLong("fechaLimite") ?: 0L)

                val colorEstado = when (estado.lowercase()) {
                    "completada", "cumplida" -> COLOR_EXITO
                    "pendiente" -> COLOR_SECUNDARIO
                    else -> COLOR_ERROR
                }

                tablaMetas.addCell(Cell().add(Paragraph(categoria)).setPadding(8f).setBackgroundColor(colorFondo))
                tablaMetas.addCell(Cell().add(Paragraph(tipo)).setPadding(8f).setBackgroundColor(colorFondo))
                tablaMetas.addCell(Cell().add(Paragraph(cantidad)).setPadding(8f).setBackgroundColor(colorFondo).setTextAlignment(TextAlignment.RIGHT))
                tablaMetas.addCell(Cell().add(Paragraph(estado).setFontColor(colorEstado).setBold()).setPadding(8f).setBackgroundColor(colorFondo))
                tablaMetas.addCell(Cell().add(Paragraph(fechaLimite)).setPadding(8f).setBackgroundColor(colorFondo).setTextAlignment(TextAlignment.CENTER))
            }

            doc.add(tablaMetas)
        }
    }

    private fun agregarSeccionTransacciones(doc: Document, transacciones: List<DocumentSnapshot>) {
        val tituloSeccion = Paragraph("DETALLE DE TRANSACCIONES")
            .setFontSize(16f)
            .setBold()
            .setFontColor(COLOR_SECUNDARIO)
            .setMarginBottom(10f)
        doc.add(tituloSeccion)

        if (transacciones.isEmpty()) {
            val sinTransacciones = Paragraph("No se registraron transacciones en el período consultado.")
                .setFontColor(COLOR_SECUNDARIO)
                .setItalic()
                .setMarginBottom(20f)
            doc.add(sinTransacciones)
            return
        }

        val tablaTransacciones = Table(UnitValue.createPercentArray(floatArrayOf(1f, 2.5f, 1f, 1f)))
            .useAllAvailableWidth()
            .setMarginBottom(20f)

        // Encabezados
        val encabezados = listOf("FECHA", "DESCRIPCIÓN", "TIPO", "CANTIDAD")
        encabezados.forEach { encabezado ->
            tablaTransacciones.addHeaderCell(
                Cell().add(Paragraph(encabezado).setBold().setFontColor(ColorConstants.WHITE))
                    .setBackgroundColor(COLOR_PRIMARIO)
                    .setPadding(8f)
                    .setTextAlignment(TextAlignment.CENTER)
            )
        }

        // Ordenar transacciones por fecha (más recientes primero)
        val transaccionesOrdenadas = transacciones.sortedByDescending {
            it.getLong("fecha") ?: 0L
        }

        // Datos de transacciones
        transaccionesOrdenadas.forEachIndexed { index, transaccion ->
            val esPar = index % 2 == 0
            val colorFondo = if (esPar) ColorConstants.WHITE else COLOR_GRIS_CLARO

            val fecha = formatDate(transaccion.getLong("fecha") ?: 0L)
            val descripcion = transaccion.getString("descripcion") ?: "Sin descripción"
            val tipo = transaccion.getString("tipo") ?: "Otro"
            val cantidad = transaccion.getDouble("cantidad") ?: 0.0

            val colorTipo = if (tipo.equals("Gasto", ignoreCase = true)) COLOR_ERROR else COLOR_EXITO
            val signo = if (tipo.equals("Gasto", ignoreCase = true)) "-" else "+"

            tablaTransacciones.addCell(Cell().add(Paragraph(fecha)).setPadding(8f).setBackgroundColor(colorFondo).setTextAlignment(TextAlignment.CENTER))
            tablaTransacciones.addCell(Cell().add(Paragraph(descripcion)).setPadding(8f).setBackgroundColor(colorFondo))
            tablaTransacciones.addCell(Cell().add(Paragraph(tipo).setFontColor(colorTipo).setBold()).setPadding(8f).setBackgroundColor(colorFondo).setTextAlignment(TextAlignment.CENTER))
            tablaTransacciones.addCell(Cell().add(Paragraph("$signo${formatCurrency(cantidad)}").setFontColor(colorTipo).setBold()).setPadding(8f).setBackgroundColor(colorFondo).setTextAlignment(TextAlignment.RIGHT))
        }

        doc.add(tablaTransacciones)
    }

    private fun agregarPiePagina(doc: Document) {
        // Línea separadora
        val lineaSeparadora = Table(1)
            .useAllAvailableWidth()
            .setBorder(SolidBorder(COLOR_PRIMARIO, 1f))
        .setMarginTop(20f)
            .setMarginBottom(10f)
        lineaSeparadora.addCell(Cell().setBorder(null).setHeight(2f))
        doc.add(lineaSeparadora)

        // Información del pie
        val piePagina = Paragraph("Este reporte fue generado automáticamente por la aplicación de gestión financiera personal.")
            .setTextAlignment(TextAlignment.CENTER)
            .setFontSize(10f)
            .setFontColor(COLOR_SECUNDARIO)
            .setItalic()
        doc.add(piePagina)

        val confidencialidad = Paragraph("INFORMACIÓN CONFIDENCIAL - Para uso exclusivo del titular de la cuenta")
            .setTextAlignment(TextAlignment.CENTER)
            .setFontSize(8f)
            .setFontColor(COLOR_SECUNDARIO)
            .setBold()
        doc.add(confidencialidad)
    }

    private fun enviarPorCorreo(context: Context, file: File, user: User) {
        val uri = FileProvider.getUriForFile(context, context.packageName + ".provider", file)

        val asunto = "Reporte Financiero Mensual - ${user.nombreCompleto}"
        val cuerpoMensaje = """
            Estimado/a ${user.nombreCompleto},
            
            Adjunto encontrará su reporte financiero mensual generado automáticamente.
            
            Este documento contiene:
            • Resumen ejecutivo de sus finanzas
            • Detalle de ingresos y gastos
            • Estado de sus metas financieras
            • Análisis de transacciones del último mes
            
            Le recomendamos revisar periódicamente esta información para mantener un control efectivo de sus finanzas personales.
            
            Saludos cordiales,
            Equipo de Gestión Financiera
        """.trimIndent()

        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_SUBJECT, asunto)
            putExtra(Intent.EXTRA_TEXT, cuerpoMensaje)
            putExtra(Intent.EXTRA_EMAIL, arrayOf(user.correo))
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(emailIntent, "Enviar reporte financiero"))
    }

    // Funciones auxiliares de formato
    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    private fun formatDateComplete(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd 'de' MMMM 'de' yyyy 'a las' HH:mm", Locale("es", "ES"))
        return sdf.format(Date(timestamp))
    }

    private fun formatDateForFile(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    private fun formatCurrency(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
        return formatter.format(amount)
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }
}