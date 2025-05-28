package com.example.proyectodef.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch
import com.example.proyectodef.model.Meta

class ProgresoViewModel : ViewModel() {

    fun calcularYActualizarProgresoMetas(userId: String, metas: List<Meta>) {
        viewModelScope.launch {
            val db = FirebaseFirestore.getInstance()
            val transRef = db.collection("usuarios")
                .document(userId)
                .collection("transacciones")

            val metasRef = db.collection("usuarios")
                .document(userId)
                .collection("metas")

            for (meta in metas) {
                Log.d("deiviProgresoViewModel", "Procesando meta ${meta.id} (${meta.categoria})")
                if (meta.estado != "Completado" && meta.id.isNotBlank() && !meta.id.contains("/")) {
                    try {
                        val transSnap = transRef
                            .whereEqualTo("tipo", meta.tipo)
                            .whereGreaterThanOrEqualTo("fecha", meta.fechaCreacion)
                            .whereLessThanOrEqualTo("fecha", meta.fechaLimite)
                            .get()
                            .await()

                        Log.d("deiviProgresoViewModel", "Transacciones encontradas: ${transSnap.size()} para meta ${meta.id}")

                        val suma = transSnap.documents.sumOf { it.getDouble("cantidad") ?: 0.0 }
                        Log.d("deiviProgresoViewModel", "Suma total: $suma / Objetivo: ${meta.cantidad}")

                        val progreso = (suma / meta.cantidad).coerceAtMost(1.0)
                        val metaDocRef = metasRef.document(meta.id)

                        if (progreso >= 1.0) {
                            metaDocRef.update(
                                mapOf(
                                    "estado" to "Completado",
                                    "progreso" to 1.0
                                )
                            ).await()
                            Log.d("deiviProgresoViewModel", "Meta ${meta.id} marcada como completada.")
                        } else {
                            metaDocRef.update("progreso", progreso).await()
                            Log.d("deiviProgresoViewModel", "Meta ${meta.id} actualizada con progreso ${(progreso * 100).toInt()}%.")
                        }
                    } catch (e: Exception) {
                        Log.e("deiviProgresoViewModel", "Error al actualizar meta ${meta.id}: ${e.message}", e)
                    }
                } else if (meta.id.isBlank()) {
                    Log.e("deiviProgresoViewModel", "Meta con ID vacío, no se puede actualizar. Meta: $meta")
                }
            }
        }
    }
}
