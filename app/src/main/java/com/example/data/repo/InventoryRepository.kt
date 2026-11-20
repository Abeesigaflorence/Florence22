package com.example.data.repo

import com.example.data.model.InventoryItem
import com.example.data.model.StockMovement
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlin.math.max

class InventoryRepository(
    private val db: FirebaseFirestore
) {
    private val auth get() = Firebase.auth

    private fun requireUserId(): String =
        auth.currentUser?.uid ?: throw IllegalStateException("User must be signed in to manage inventory")

    fun observeItems(): Flow<List<InventoryItem>> {
        return db.collection("inventoryItems")
            .orderBy("name", Query.Direction.ASCENDING)
            .limit(200)
            .snapshots()
            .map { snap -> snap.toObjects(InventoryItem::class.java) }
    }

    fun observeMovements(): Flow<List<StockMovement>> {
        return db.collection("stockMovements")
            .orderBy("date", Query.Direction.DESCENDING)
            .limit(40)
            .snapshots()
            .map { snap -> snap.toObjects(StockMovement::class.java) }
    }

    suspend fun addItem(
        name: String,
        category: String,
        unit: String,
        quantity: Double,
        reorderLevel: Double,
        unitCost: Double
    ): String {
        val uid = requireUserId()
        val payload = hashMapOf<String, Any>(
            "name" to name.trim(),
            "category" to category,
            "unit" to unit.trim(),
            "quantity" to quantity,
            "reorderLevel" to reorderLevel,
            "unitCost" to unitCost,
            "createdAt" to FieldValue.serverTimestamp(),
            "createdBy" to uid
        )

        val docRef = db.collection("inventoryItems").add(payload).await()
        return docRef.id
    }

    suspend fun recordMovement(
        item: InventoryItem,
        type: String,
        quantity: Double,
        date: String,
        note: String
    ) {
        val uid = requireUserId()
        db.runTransaction { tx ->
            val ref = db.collection("inventoryItems").document(item.id)
            val freshSnap = tx.get(ref)
            val fresh = freshSnap.toObject(InventoryItem::class.java) ?: item
            val next = max(0.0, fresh.quantity + if (type == "in") quantity else -quantity)

            tx.update(ref, "quantity", next)

            val movementRef = db.collection("stockMovements").document()
            tx.set(
                movementRef,
                mapOf(
                    "itemId" to item.id,
                    "itemName" to fresh.name,
                    "type" to type,
                    "quantity" to quantity,
                    "date" to date,
                    "note" to note.trim(),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "createdBy" to uid
                )
            )
            null
        }.await()
    }
}
