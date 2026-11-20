package com.example.data.repo

import com.example.data.model.Flock
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FlockRepository(
    private val db: FirebaseFirestore
) {
    private val auth get() = Firebase.auth

    private fun requireUserId(): String =
        auth.currentUser?.uid ?: throw IllegalStateException("User must be signed in to manage flocks")

    fun observeFlocks(): Flow<List<Flock>> {
        return db.collection("flocks")
            .orderBy("placementDate", Query.Direction.DESCENDING)
            .limit(200)
            .snapshots()
            .map { snap ->
                val list = snap.toObjects(Flock::class.java)
                // Client-sort active first
                list.sortedWith(compareBy({ it.status != "active" }, { it.placementDate }))
                    .reversed()
            }
    }

    suspend fun getFlock(id: String): Flock? {
        val snap = db.collection("flocks").document(id).get().await()
        return if (snap.exists()) snap.toObject(Flock::class.java) else null
    }

    suspend fun createFlock(
        code: String,
        breed: String,
        type: String,
        house: String,
        placementDate: String,
        initialCount: Int,
        notes: String
    ): String {
        val uid = requireUserId()
        val payload = hashMapOf<String, Any>(
            "code" to code.trim(),
            "breed" to breed.trim(),
            "type" to type,
            "house" to house.trim(),
            "placementDate" to placementDate,
            "initialCount" to initialCount,
            "mortalityCount" to 0,
            "currentCount" to initialCount,
            "status" to "active",
            "notes" to notes.trim(),
            "createdAt" to FieldValue.serverTimestamp(),
            "createdBy" to uid
        )

        val docRef = db.collection("flocks").add(payload).await()
        return docRef.id
    }
}
