package com.example.domain

import com.example.data.model.Expense
import com.example.data.model.InventoryItem
import com.example.data.model.Sale
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

fun survivalPct(initial: Double, current: Double): Double =
    if (initial > 0) (current / initial) * 100.0 else 0.0

fun birdAgeDays(placementDate: String?, today: LocalDate = LocalDate.now(ZoneOffset.UTC)): Int =
    runCatching {
        if (placementDate.isNullOrBlank()) 0
        else ChronoUnit.DAYS.between(LocalDate.parse(placementDate), today).toInt()
    }.getOrDefault(0).coerceAtLeast(0)

fun isLowStock(item: InventoryItem): Boolean = item.quantity <= item.reorderLevel

data class FlockMoney(val revenue: Double, val cost: Double) {
    val profit get() = revenue - cost
}

fun flockMoney(flockId: String, sales: List<Sale>, expenses: List<Expense>): FlockMoney = FlockMoney(
    revenue = sales.filter { it.flockId == flockId }.sumOf { it.total },
    cost = expenses.filter { it.flockId == flockId }.sumOf { it.amount }
)
