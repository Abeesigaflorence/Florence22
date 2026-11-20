package com.example.data.repo

import com.example.data.model.Sale
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class SalesRepository(
    private val db: FirebaseFirestore
) {
    private val auth get() = Firebase.auth

    private fun requireUserId(): String =
        auth.currentUser?.uid ?: throw IllegalStateException("User must be signed in to record sales")

    fun observeSales(): Flow<List<Sale>> {
        return db.collection("sales")
            .orderBy("date", Query.Direction.DESCENDING)
            .limit(500)
            .snapshots()
            .map { snap -> snap.toObjects(Sale::class.java) }
    }

    suspend fun recordSale(
        date: String,
        product: String,
        description: String,
        quantity: Double,
        unit: String,
        unitPrice: Double,
        total: Double,
        customer: String,
        flockId: String,
        flockCode: String,
        paymentStatus: String
    ): String {
        val uid = requireUserId()
        val calculatedTotal = if (total > 0) total else quantity * unitPrice
        val custName = if (customer.isBlank()) "Walk-in customer" else customer.trim()

        val payload = hashMapOf<String, Any>(
            "date" to date,
            "product" to product,
            "description" to description.trim(),
            "quantity" to quantity,
            "unit" to unit,
            "unitPrice" to unitPrice,
            "total" to calculatedTotal,
            "customer" to custName,
            "flockId" to flockId,
            "flockCode" to flockCode,
            "paymentStatus" to paymentStatus,
            "createdAt" to FieldValue.serverTimestamp(),
            "createdBy" to uid
        )

        val docRef = db.collection("sales").add(payload).await()
        return docRef.id
    }
}
