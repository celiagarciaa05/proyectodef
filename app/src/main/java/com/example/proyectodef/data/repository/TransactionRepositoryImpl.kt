package com.example.proyectodef.data.repository

import com.example.proyectodef.model.Category
import com.example.proyectodef.model.Transaction
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class TransactionRepositoryImpl : TransactionRepository {

    private val db = Firebase.firestore

    override suspend fun addTransaction(transaction: Transaction): Result<Unit> {
        return try {
            val docRef = db.collection("usuarios").document(transaction.userId).collection("transacciones").document()
            val newTransaction = transaction.copy(id = docRef.id)
            docRef.set(newTransaction).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserCategories(userId: String): List<Category> {
        return try {
            val snapshot = db.collection("usuarios").document(userId).collection("categorias").get().await()
            snapshot.documents.mapNotNull { it.toObject(Category::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun addCategory(category: Category): Result<Unit> {
        return try {
            val docRef = db.collection("usuarios").document(category.userId).collection("categorias").document()
            val newCategory = category.copy(id = docRef.id)
            docRef.set(newCategory).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
