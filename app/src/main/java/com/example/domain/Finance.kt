package com.example.domain

import com.example.data.model.Expense
import com.example.data.model.Sale
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

object Finance {
    const val WINDOW = 6

    fun monthKey(iso: String?): String = iso?.take(7) ?: ""

    data class MonthPoint(
        val key: String,
        val label: String,
        val full: String,
        val revenue: Double,
        val expenses: Double
    ) {
        val profit get() = revenue - expenses
    }

    fun windowMonths(
        months: Int = WINDOW,
        today: YearMonth = YearMonth.now(ZoneOffset.UTC)
    ): List<YearMonth> =
        (months - 1 downTo 0).map { today.minusMonths(it.toLong()) }

    fun monthlySeries(
        sales: List<Sale>,
        expenses: List<Expense>,
        months: Int = WINDOW,
        today: YearMonth = YearMonth.now(ZoneOffset.UTC)
    ): List<MonthPoint> {
        val byRev = sales.groupBy { monthKey(it.date) }
            .mapValues { entry -> entry.value.sumOf { it.total } }
        val byExp = expenses.groupBy { monthKey(it.date) }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        return windowMonths(months, today).map { ym ->
            val key = ym.toString() // yyyy-MM
            MonthPoint(
                key = key,
                label = ym.format(DateTimeFormatter.ofPattern("MMM", Locale.UK)),
                full = ym.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.UK)),
                revenue = byRev[key] ?: 0.0,
                expenses = byExp[key] ?: 0.0
            )
        }
    }
}

data class Fit(val slope: Double, val intercept: Double, val r2: Double)

fun fit(ys: List<Double>): Fit {
    val n = ys.size
    if (n < 2) return Fit(0.0, ys.firstOrNull() ?: 0.0, 0.0)
    val xs = List(n) { it.toDouble() }
    val xb = xs.average()
    val yb = ys.average()
    val den = xs.sumOf { (it - xb) * (it - xb) }
    val slope = if (den == 0.0) 0.0 else xs.zip(ys).sumOf { (x, y) -> (x - xb) * (y - yb) } / den
    val icpt = yb - slope * xb
    val ssRes = xs.zip(ys).sumOf { (x, y) ->
        val e = y - (icpt + slope * x)
        e * e
    }
    val ssTot = ys.sumOf {
        val d = it - yb
        d * d
    }
    val r2 = if (ssTot > 0) (1 - ssRes / ssTot).coerceAtLeast(0.0) else 0.0
    return Fit(slope, icpt, r2)
}

data class ProjectedMonth(
    val key: String,
    val label: String,
    val full: String,
    val revenue: Double,
    val expenses: Double
) {
    val profit get() = revenue - expenses
}

data class ForecastReport(
    val projected: List<ProjectedMonth>,
    val confidence: Double,
    val direction: String,
    val months: Int = Finance.WINDOW
) {
    val next get() = projected.first()
    val projectedProfit get() = next.profit
    val projectedRevenue get() = next.revenue
    val projectedExpenses get() = next.expenses
    val nextLabel get() = projected.firstOrNull()?.let { it.full.substringBefore(" ") } ?: "Next month"
}

fun forecast(
    series: List<Finance.MonthPoint>,
    horizon: Int = 1,
    today: YearMonth = YearMonth.now(ZoneOffset.UTC)
): ForecastReport {
    val fr = fit(series.map { it.revenue })
    val fe = fit(series.map { it.expenses })
    val n = series.size
    val projected = (0 until horizon).map { i ->
        val ym = today.plusMonths(i + 1L)
        val step = (n + i).toDouble()
        ProjectedMonth(
            key = ym.toString(),
            label = ym.format(DateTimeFormatter.ofPattern("MMM", Locale.UK)),
            full = ym.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.UK)),
            revenue = (fr.intercept + fr.slope * step).coerceAtLeast(0.0),
            expenses = (fe.intercept + fe.slope * step).coerceAtLeast(0.0)
        )
    }
    val last = series.lastOrNull()?.profit ?: 0.0
    val first = projected.first().profit
    val delta = first - last
    val direction = when {
        abs(delta) < maxOf(20_000.0, abs(last) * 0.05) -> "flat"
        first > last -> "up"
        else -> "down"
    }
    return ForecastReport(projected, minOf(fr.r2, fe.r2), direction)
}
