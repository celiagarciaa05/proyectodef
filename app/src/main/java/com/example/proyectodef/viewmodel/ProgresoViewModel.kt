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
                            metaDocRef.update(
                                mapOf(
                                    "estado" to "Completado",
                                    "progreso" to 1.0
                                )
                            ).await()
                        } else {
                            metaDocRef.update("progreso", progreso).await()
                        }
                    } catch (_: Exception) {
                    }
                }
            }
        }
    }
}