package com.example.proyectodef.utils

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

object CargaMasivaFirestore {

    private val categorias = listOf("Comida", "Salud", "Trabajo", "Ocio")

    suspend fun insertarDatos(userId: String) {
        val db = FirebaseFirestore.getInstance()
        val batch = db.batch()

        val transRef = db.collection("usuarios").document(userId).collection("transacciones")
        val metasRef = db.collection("usuarios").document(userId).collection("metas")
        val categoriasRef = db.collection("usuarios").document(userId).collection("categorias")
        val flagRef = db.collection("usuarios").document(userId).collection("banderas").document("datosPrueba")

        val flagSnapshot = flagRef.get().await()
        if (flagSnapshot.exists() && flagSnapshot.getBoolean("datosCargados") == true) {
            return
        }

        val ahora = System.currentTimeMillis()
        val hace30Dias = ahora - 30L * 24 * 60 * 60 * 1000

        repeat(6) { i ->
            val fecha = if (i < 5) randomFechaReciente(hace30Dias, ahora) else randomFechaAntigua()
            val data = mapOf(
                "descripcion" to "Ingreso $i",
                "cantidad" to Random.Default.nextDouble(50.0, 300.0),
                "categoria" to categorias.random(),
                "tipo" to "Ahorro",
                "fecha" to fecha
            )
            batch.set(transRef.document(), data)
        }

        repeat(9) { i ->
            val fecha = if (i < 6) randomFechaReciente(hace30Dias, ahora) else randomFechaAntigua()
            val data = mapOf(
                "descripcion" to "Gasto $i",
                "cantidad" to Random.Default.nextDouble(10.0, 150.0),
                "categoria" to categorias.random(),
                "tipo" to "Gasto",
                "fecha" to fecha
            )
            batch.set(transRef.document(), data)
        }

        repeat(9) { i ->
            val tipo = if (i < 6) "Ahorro" else "Gasto"
            val estado = when {
                i in 0..2 -> "Completado"
                i == 3 -> "Expirado"
                else -> "Proceso"
            }
            val data = mapOf(
                "categoria" to categorias.random(),
                "tipo" to tipo,
                "cantidad" to Random.Default.nextDouble(100.0, 1000.0),
                "fechaLimite" to randomFechaReciente(hace30Dias, ahora),
                "estado" to estado,
                "userId" to userId
            )
            batch.set(metasRef.document(), data)
        }

        categorias.forEach { nombre ->
            val data = mapOf(
                "nombre" to nombre,
                "presupuesto" to Random.Default.nextDouble(200.0, 800.0)
            )
            batch.set(categoriasRef.document(), data)
        }

        batch.set(flagRef, mapOf("datosCargados" to true))

        try {
            batch.commit().await()
        } catch (e: Exception) {
        }
    }

    private fun randomFechaReciente(inicio: Long, fin: Long): Long {
        return (inicio..fin).random()
    }

    private fun randomFechaAntigua(): Long {
        val ahora = System.currentTimeMillis()
        return ahora - Random.Default.nextLong(60L * 24 * 60 * 60 * 1000, 180L * 24 * 60 * 60 * 1000)
    }
}
