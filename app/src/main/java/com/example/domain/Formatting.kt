package com.example.domain

import java.text.NumberFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

private val nf0 = NumberFormat.getNumberInstance(Locale.US).apply {
    maximumFractionDigits = 0
    isGroupingUsed = true
}

private val nfD = NumberFormat.getNumberInstance(Locale.US).apply {
    isGroupingUsed = true
}

fun money(v: Double, compact: Boolean = false): String {
    return if (!compact) {
        "UGX ${nf0.format(v)}"
    } else {
        when {
            abs(v) >= 10_000_000 -> "UGX ${nfD.apply { maximumFractionDigits = 1 }.format(v / 1_000_000)}M"
            abs(v) >= 1_000_000  -> "UGX ${nfD.apply { maximumFractionDigits = 2 }.format(v / 1_000_000)}M"
            abs(v) >= 1_000      -> "UGX ${nf0.format(v / 1_000)}K"
            else                 -> "UGX ${nf0.format(v)}"
        }
    }
}

fun num(v: Double, digits: Int = 0): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
        maximumFractionDigits = digits
        minimumFractionDigits = if (digits > 0 && v % 1.0 != 0.0) digits else 0
        isGroupingUsed = true
    }
    return formatter.format(v)
}

fun pct(v: Double, digits: Int = 1): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
        maximumFractionDigits = digits
    }
    return "${formatter.format(v)}%"
}

fun todayISO(): String = LocalDate.now(ZoneOffset.UTC).toString()

fun fmtDate(iso: String?): String =
    runCatching {
        if (iso.isNullOrBlank()) "—"
        else LocalDate.parse(iso).format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.UK))
    }.getOrDefault("—")

fun fmtDateShort(iso: String?): String =
    runCatching {
        if (iso.isNullOrBlank()) "—"
        else LocalDate.parse(iso).format(DateTimeFormatter.ofPattern("dd MMM", Locale.UK))
    }.getOrDefault("—")

fun humanDate(iso: String?): String {
    if (iso.isNullOrBlank()) return "—"
    return runCatching {
        val parsed = LocalDate.parse(iso)
        val today = LocalDate.now(ZoneOffset.UTC)
        when (parsed) {
            today -> "Today"
            today.minusDays(1) -> "Yesterday"
            else -> parsed.format(DateTimeFormatter.ofPattern("dd MMM", Locale.UK))
        }
    }.getOrDefault("—")
}

fun timeOfDayGreeting(firstName: String?): String {
    val hour = LocalTime.now().hour
    val base = when {
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        else -> "Good evening"
    }
    return if (firstName.isNullOrBlank()) base else "$base, $firstName"
}
