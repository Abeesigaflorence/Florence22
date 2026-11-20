package com.example.data.repo

import com.example.data.model.Flock
import com.example.data.model.ProductionRecord
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

class ProductionRepository(
    private val db: FirebaseFirestore
) {
    private val auth get() = Firebase.auth

    private fun requireUserId(): String =
        auth.currentUser?.uid ?: throw IllegalStateException("User must be signed in to log production")

    fun observeAllProduction(): Flow<List<ProductionRecord>> {
        return db.collection("productionRecords")
            .orderBy("date", Query.Direction.DESCENDING)
            .limit(300)
            .snapshots()
            .map { snap -> snap.toObjects(ProductionRecord::class.java) }
    }

    fun observeFlockProduction(flockId: String): Flow<List<ProductionRecord>> {
        return db.collection("productionRecords")
            .whereEqualTo("flockId", flockId)
            .orderBy("date", Query.Direction.DESCENDING)
            .limit(200)
            .snapshots()
            .map { snap -> snap.toObjects(ProductionRecord::class.java) }
    }

    suspend fun logProduction(
        flock: Flock,
        date: String,
        eggsCollected: Double,
        eggsBroken: Double,
        mortality: Double,
        feedUsedKg: Double,
        notes: String
    ): String {
        val uid = requireUserId()

        val payload = hashMapOf<String, Any>(
            "date" to date,
            "flockId" to flock.id,
            "flockCode" to flock.code,
            "eggsCollected" to eggsCollected,
            "eggsBroken" to eggsBroken,
            "mortality" to mortality,
            "feedUsedKg" to feedUsedKg,
            "notes" to notes.trim(),
            "createdAt" to FieldValue.serverTimestamp(),
            "createdBy" to uid
        )

        val docRef = db.collection("productionRecords").add(payload).await()

        // Side effect on flock per AGENTS.md §8.2:
        // if mortality > 0, update mortalityCount and currentCount
        if (mortality > 0) {
            val lost = mortality.toInt()
            val flockRef = db.collection("flocks").document(flock.id)
            val freshSnap = flockRef.get().await()
            val freshFlock = freshSnap.toObject(Flock::class.java)
            val baseMortality = freshFlock?.mortalityCount ?: flock.mortalityCount
            val baseCurrent = freshFlock?.currentCount ?: flock.currentCount

            flockRef.update(
                mapOf(
                    "mortalityCount" to (baseMortality + lost),
                    "currentCount" to max(0, baseCurrent - lost)
                )
            ).await()
        }

        return docRef.id
    }
}
