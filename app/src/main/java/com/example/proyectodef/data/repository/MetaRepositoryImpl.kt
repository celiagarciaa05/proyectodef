package com.example.proyectodef.data.repository

import com.example.proyectodef.model.Meta
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class MetaRepositoryImpl : MetaRepository {
    private val db = Firebase.firestore

    override suspend fun addMeta(meta: Meta): Result<Unit> {
        return try {
            val docRef = db.collection("usuarios").document(meta.userId).collection("metas").document()
            val newMeta = meta.copy(id = docRef.id,
                fechaCreacion = System.currentTimeMillis()
            )
            docRef.set(newMeta).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMetas(userId: String): List<Meta> {
        return try {
            val snapshot = db.collection("usuarios").document(userId).collection("metas").get().await()
            snapshot.documents.mapNotNull { doc ->
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
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun deleteMeta(userId: String, metaId: String): Result<Unit> {
        return try {
            db.collection("usuarios").document(userId).collection("metas").document(metaId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateMetaEstado(userId: String, metaId: String, nuevoEstado: String): Result<Unit> {
        return try {
            val ref = Firebase.firestore.collection("usuarios").document(userId).collection("metas").document(metaId)
            ref.update("estado", nuevoEstado).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
