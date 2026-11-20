package com.example.domain

const val CURRENCY = "UGX"

val FLOCK_TYPES = listOf(
    "layer" to "Layers · table eggs",
    "broiler" to "Broilers · meat"
)

val PRODUCTS = listOf(
    "eggs" to "Eggs",
    "live_birds" to "Live birds",
    "manure" to "Manure",
    "other" to "Other"
)

val EXPENSE_CATEGORIES = listOf(
    "feed" to "Feed",
    "medication" to "Medication & vaccines",
    "labour" to "Labour",
    "utilities" to "Utilities",
    "equipment" to "Equipment",
    "transport" to "Transport",
    "other" to "Other"
)

val INVENTORY_CATEGORIES = listOf(
    "feed" to "Feed",
    "medication" to "Medication",
    "equipment" to "Equipment",
    "other" to "Other"
)

val UNITS = listOf("tray", "egg", "bird", "kg", "bag", "vial", "litre", "piece")

fun labelOf(options: List<Pair<String, String>>, value: String?): String =
    options.firstOrNull { it.first == value }?.second ?: value?.replace('_', ' ')?.replaceFirstChar { it.uppercase() } ?: "—"
