package com.example.proyectodef.data.repository

import com.example.proyectodef.model.Transaction
import com.example.proyectodef.model.Category

interface TransactionRepository {
    suspend fun addTransaction(transaction: Transaction): Result<Unit>
    suspend fun getUserCategories(userId: String): List<Category>
    suspend fun addCategory(category: Category): Result<Unit>
}
