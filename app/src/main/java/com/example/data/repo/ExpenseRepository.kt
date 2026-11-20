package com.example.data.repo

import com.example.data.model.Expense
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class ExpenseRepository(
    private val db: FirebaseFirestore
) {
    private val auth get() = Firebase.auth

    private fun requireUserId(): String =
        auth.currentUser?.uid ?: throw IllegalStateException("User must be signed in to record expenses")

    fun observeExpenses(): Flow<List<Expense>> {
        return db.collection("expenses")
            .orderBy("date", Query.Direction.DESCENDING)
            .limit(500)
            .snapshots()
            .map { snap -> snap.toObjects(Expense::class.java) }
    }

    suspend fun recordExpense(
        date: String,
        category: String,
        description: String,
        amount: Double,
        vendor: String,
        flockId: String,
        flockCode: String
    ): String {
        val uid = requireUserId()
        val payload = hashMapOf<String, Any>(
            "date" to date,
            "category" to category,
            "description" to description.trim(),
            "amount" to amount,
            "vendor" to vendor.trim(),
            "flockId" to flockId,
            "flockCode" to flockCode,
            "createdAt" to FieldValue.serverTimestamp(),
            "createdBy" to uid
        )

        val docRef = db.collection("expenses").add(payload).await()
        return docRef.id
    }
}
