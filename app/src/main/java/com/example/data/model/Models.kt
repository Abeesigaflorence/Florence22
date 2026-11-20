package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class FlockType {
    LAYER, BROILER
}

enum class FlockStatus {
    ACTIVE, CLOSED
}

enum class FarmRole {
    ADMIN, USER
}

@IgnoreExtraProperties
data class Flock(
    @DocumentId val id: String = "",
    val code: String = "",
    val breed: String = "",
    val type: String = "layer",
    val house: String = "",
    val placementDate: String = "",
    val initialCount: Int = 0,
    val mortalityCount: Int = 0,
    val currentCount: Int = 0,
    val status: String = "active",
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    val createdBy: String = ""
)

@IgnoreExtraProperties
data class ProductionRecord(
    @DocumentId val id: String = "",
    val date: String = "",
    val flockId: String = "",
    val flockCode: String = "",
    val eggsCollected: Double = 0.0,
    val eggsBroken: Double = 0.0,
    val mortality: Double = 0.0,
    val feedUsedKg: Double = 0.0,
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    val createdBy: String = ""
)

@IgnoreExtraProperties
data class Sale(
    @DocumentId val id: String = "",
    val date: String = "",
    val product: String = "eggs",
    val description: String = "",
    val quantity: Double = 0.0,
    val unit: String = "tray",
    val unitPrice: Double = 0.0,
    val total: Double = 0.0,
    val customer: String = "",
    val flockId: String = "",
    val flockCode: String = "",
    val paymentStatus: String = "paid",
    @ServerTimestamp val createdAt: Timestamp? = null,
    val createdBy: String = ""
)

@IgnoreExtraProperties
data class Expense(
    @DocumentId val id: String = "",
    val date: String = "",
    val category: String = "feed",
    val description: String = "",
    val amount: Double = 0.0,
    val vendor: String = "",
    val flockId: String = "",
    val flockCode: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    val createdBy: String = ""
)

@IgnoreExtraProperties
data class InventoryItem(
    @DocumentId val id: String = "",
    val name: String = "",
    val category: String = "feed",
    val unit: String = "bag",
    val quantity: Double = 0.0,
    val reorderLevel: Double = 0.0,
    val unitCost: Double = 0.0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    val createdBy: String = ""
)

@IgnoreExtraProperties
data class StockMovement(
    @DocumentId val id: String = "",
    val itemId: String = "",
    val itemName: String = "",
    val type: String = "out",
    val quantity: Double = 0.0,
    val date: String = "",
    val note: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    val createdBy: String = ""
)

@IgnoreExtraProperties
data class AppUser(
    @DocumentId val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val role: String = "USER",
    @ServerTimestamp val createdAt: Timestamp? = null
)
