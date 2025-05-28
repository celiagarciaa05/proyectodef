package com.example.proyectodef.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectodef.data.repository.TransactionRepository
import com.example.proyectodef.model.Category
import com.example.proyectodef.model.Meta
import com.example.proyectodef.model.Transaction
import com.google.firebase.firestore.ktx.firestore

import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class TransactionViewModel(private val repo: TransactionRepository) : ViewModel() {
    private val _todasTransacciones = MutableStateFlow<List<Transaction>>(emptyList())
    val todasTransacciones: StateFlow<List<Transaction>> = _todasTransacciones

    private val _categorias = MutableStateFlow<List<Category>>(emptyList())
    val categorias: StateFlow<List<Category>> = _categorias

    private val _transaccionesPorCategoria = MutableStateFlow<List<CategoriaConTotales>>(emptyList())
    val transaccionesPorCategoria: StateFlow<List<CategoriaConTotales>> = _transaccionesPorCategoria

    private val _transaccionesFiltradas = MutableStateFlow<List<Transaction>>(emptyList())
    val transaccionesFiltradas: StateFlow<List<Transaction>> = _transaccionesFiltradas

    fun cargarCategorias(userId: String) {
        viewModelScope.launch {
            _categorias.value = repo.getUserCategories(userId)
        }
    }

    fun agregarCategoria(userId: String, nombre: String) {
        viewModelScope.launch {
            repo.addCategory(Category(userId = userId, nombre = nombre))
            cargarCategorias(userId)
        }
    }

    fun agregarTransaccion(transaction: Transaction) {
        viewModelScope.launch {
            val result = repo.addTransaction(transaction)
            if (result.isSuccess) {
                // ✅ Refresca todo tras añadir una nueva
                cargarTransaccionesPorTipo(transaction.userId, transaction.tipo)
                cargarTransaccionesPorCategoria(transaction.userId)
            } else {
                println("Error al agregar transacción: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun cargarTransaccionesPorCategoria(userId: String) {
        viewModelScope.launch {
            try {
                val snapshot = Firebase.firestore
                    .collection("usuarios")
                    .document(userId)
                    .collection("transacciones")
                    .get()
                    .await()

                val transacciones = snapshot.documents.mapNotNull {
                    it.toObject(Transaction::class.java)
                }

                val agrupadas = transacciones
                    .groupBy { it.categoria }
                    .map { (categoria, lista) ->
                        CategoriaConTotales(
                            nombre = categoria,
                            totalAhorros = lista.filter { it.tipo == "Ahorro" }.sumOf { it.cantidad },
                            totalGastos = lista.filter { it.tipo == "Gasto" }.sumOf { it.cantidad }
                        )
                    }

                _transaccionesPorCategoria.value = agrupadas
            } catch (e: Exception) {
                println("Error al cargar transacciones por categoría: ${e.message}")
                _transaccionesPorCategoria.value = emptyList()
            }
        }
    }

    fun cargarTransaccionesPorTipo(userId: String, tipo: String) {
        viewModelScope.launch {
            try {
                println("🔄 Cargando transacciones para userId=$userId tipo=$tipo")

                val snapshot = Firebase.firestore
                    .collection("usuarios")
                    .document(userId)
                    .collection("transacciones")
                    .get()
                    .await()

                println("📄 Documentos totales: ${snapshot.documents.size}")

                val transacciones = snapshot.documents.mapNotNull {
                    val obj = it.toObject(Transaction::class.java)
                    println("✅ Doc leído: ${obj?.titulo}, tipo: ${obj?.tipo}")
                    obj
                }.filter { it.tipo.trim().lowercase() == tipo.trim().lowercase() }

                println("✅ Transacciones filtradas: ${transacciones.size}")
                _transaccionesFiltradas.value = transacciones
            } catch (e: Exception) {
                println("❌ Error al cargar transacciones de tipo $tipo: ${e.message}")
                _transaccionesFiltradas.value = emptyList()
            }
        }
    }
// Dentro de TransactionViewModel

    fun calcularProgresoMeta(userId: String, meta: Meta): Float {
        val transaccionesUsuario = _transaccionesFiltradas.value
        val relevantes = transaccionesUsuario.filter {
            it.userId == userId &&
                    it.categoria == meta.categoria &&
                    it.tipo.equals(meta.tipo, ignoreCase = true)
        }
        val totalAcumulado = relevantes.sumOf { it.cantidad }
        val progreso = (totalAcumulado / meta.cantidad * 100).toFloat()
        return progreso.coerceIn(0f, 100f)
    }


    fun cargarTodasTransacciones(userId: String) {
        viewModelScope.launch {
            try {
                val snapshot = Firebase.firestore
                    .collection("usuarios")
                    .document(userId)
                    .collection("transacciones")
                    .get()
                    .await()

                val transacciones = snapshot.documents.mapNotNull {
                    it.toObject(Transaction::class.java)
                }

                _todasTransacciones.value = transacciones
            } catch (e: Exception) {
                println("Error cargando todas transacciones: ${e.message}")
                _todasTransacciones.value = emptyList()
            }
        }
    }

    fun eliminarTransaccion(userId: String, transaccion: Transaction, onDineroActualizado: (Double) -> Unit) {
        viewModelScope.launch {
            try {
                Firebase.firestore
                    .collection("usuarios")
                    .document(userId)
                    .collection("transacciones")
                    .document(transaccion.id)
                    .delete()
                    .await()

                val cambio = if (transaccion.tipo.trim().equals("Gasto", ignoreCase = true)) {
                    -transaccion.cantidad // sumar el gasto eliminado
                } else {
                    transaccion.cantidad // restar el ahorro eliminado
                }
                onDineroActualizado(cambio)

                cargarTransaccionesPorTipo(userId, transaccion.tipo)
                cargarTransaccionesPorCategoria(userId)

                println("✅ Transacción eliminada: ${transaccion.titulo}")
            } catch (e: Exception) {
                println("❌ Error al eliminar transacción: ${e.message}")
            }
        }
    }
}

// Clase auxiliar para el gráfico
data class CategoriaConTotales(
    val nombre: String,
    val totalAhorros: Double,
    val totalGastos: Double
)
