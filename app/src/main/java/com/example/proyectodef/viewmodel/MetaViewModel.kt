package com.example.proyectodef.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectodef.data.repository.MetaRepository
import com.example.proyectodef.model.Meta
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MetaViewModel(private val repo: MetaRepository) : ViewModel() {
    private val _metas = MutableStateFlow<List<Meta>>(emptyList())
    val metas: StateFlow<List<Meta>> = _metas

    fun cargarMetas(userId: String) {
        viewModelScope.launch {
            _metas.value = repo.getMetas(userId)
        }
    }

    fun agregarMeta(meta: Meta) {
        viewModelScope.launch {
            repo.addMeta(meta)
            cargarMetas(meta.userId)
        }
    }

    fun marcarMetaComoCompletada(userId: String, metaId: String) {
        viewModelScope.launch {
            val result = repo.updateMetaEstado(userId, metaId, "Completado")
            if (result.isSuccess) cargarMetas(userId)
        }
    }

    fun eliminarMeta(userId: String, metaId: String) {
        viewModelScope.launch {
            val result = repo.deleteMeta(userId, metaId)
            if (result.isSuccess) cargarMetas(userId)
        }
    }

    suspend fun calcularYActualizarProgresoMetas(userId: String, metas: List<Meta>) {
        val db = FirebaseFirestore.getInstance()
        val transRef = db.collection("usuarios")
            .document(userId)
            .collection("transacciones")

        val metasRef = db.collection("usuarios")
            .document(userId)
            .collection("metas")

        for (meta in metas) {
            if (meta.estado != "Completado" && meta.id.isNotBlank() && !meta.id.contains("/")) {
                try {
                    val transSnap = transRef
                        .whereEqualTo("tipo", meta.tipo)
                        .whereGreaterThanOrEqualTo("fecha", meta.fechaCreacion)
                        .whereLessThanOrEqualTo("fecha", meta.fechaLimite)
                        .get()
                        .await()

                    val suma = transSnap.documents.sumOf { it.getDouble("cantidad") ?: 0.0 }
                    val progreso = (suma / meta.cantidad).coerceAtMost(1.0)

                    val metaDocRef = metasRef.document(meta.id)

                    if (progreso >= 1.0) {
                        metaDocRef.update(mapOf(
                            "estado" to "Completado",
                            "progreso" to 1.0
                        )).await()
                    } else {
                        metaDocRef.update("progreso", progreso).await()
                    }

                } catch (_: Exception) {
                    // Manejo de errores omitido
                }
            }
        }
    }

    fun escucharCambiosMetas(userId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("usuarios")
            .document(userId)
            .collection("metas")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val metasActualizadas = snapshot.documents.mapNotNull { doc ->
                        val data = doc.data ?: return@mapNotNull null
                        Meta(
                            id = doc.id,
                            userId = data["userId"] as? String ?: "",
                            categoria = data["categoria"] as? String ?: "",
                            tipo = data["tipo"] as? String ?: "",
                            cantidad = (data["cantidad"] as? Number)?.toDouble() ?: 0.0,
                            fechaLimite = (data["fechaLimite"] as? Number)?.toLong() ?: 0L,
                            fechaCreacion = (data["fechaCreacion"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                            estado = data["estado"] as? String ?: "Proceso",
                            progreso = (data["progreso"] as? Number)?.toFloat() ?: 0f
                        )
                    }
                    _metas.value = metasActualizadas
                }
            }
    }
}