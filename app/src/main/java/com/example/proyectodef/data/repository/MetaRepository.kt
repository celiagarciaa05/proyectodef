package com.example.proyectodef.data.repository

import com.example.proyectodef.model.Meta

interface MetaRepository {
    suspend fun addMeta(meta: Meta): Result<Unit>
    suspend fun getMetas(userId: String): List<Meta>
    suspend fun deleteMeta(userId: String, metaId: String): Result<Unit>
    suspend fun updateMetaEstado(userId: String, metaId: String, nuevoEstado: String): Result<Unit>
}
